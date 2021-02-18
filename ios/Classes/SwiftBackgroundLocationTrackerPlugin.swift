import Flutter
import UIKit
import CoreLocation

public class SwiftBackgroundLocationTrackerPlugin: FlutterPluginAppLifeCycleDelegate {
    
    static let identifier = "com.icapps.background_location_tracker"
    
    private static let flutterThreadLabelPrefix = "\(identifier).BackgroundLocationTracker"
    
    private static var foregroundChannel: ForegroundChannel? = nil
    
    private static var flutterEngine: FlutterEngine? = nil
    private static var backgroundMethodChannel: FlutterMethodChannel? = nil
    
    
    private static var flutterPluginRegistrantCallback: FlutterPluginRegistrantCallback?
    
    private let locationManager = LocationManager.shared()
    
}

extension SwiftBackgroundLocationTrackerPlugin: FlutterPlugin {
    
    @objc
    public static func setPluginRegistrantCallback(_ callback: @escaping FlutterPluginRegistrantCallback) {
        flutterPluginRegistrantCallback = callback
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        foregroundChannel = ForegroundChannel()
        let methodChannel = ForegroundChannel.getMethodChannel(with: registrar)
        let instance = SwiftBackgroundLocationTrackerPlugin()
        registrar.addMethodCallDelegate(instance, channel: methodChannel)
        registrar.addApplicationDelegate(instance)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        locationManager.delegate = self
        SwiftBackgroundLocationTrackerPlugin.foregroundChannel?.handle(call, result: result)
    }
    
    public static func getFlutterEngine()-> FlutterEngine? {
        if (flutterEngine == nil){
            guard let callbackHandle = SharedPrefsUtil.getCallbackHandle(),
                  let flutterCallbackInformation = FlutterCallbackCache.lookupCallbackInformation(callbackHandle)
            else { return nil }
            
            flutterEngine = FlutterEngine(name: flutterThreadLabelPrefix, project: nil, allowHeadlessExecution: true)
            flutterEngine!.run(withEntrypoint: flutterCallbackInformation.callbackName, libraryURI: flutterCallbackInformation.callbackLibraryPath)
            SwiftBackgroundLocationTrackerPlugin.flutterPluginRegistrantCallback?(flutterEngine!)
        }
        
        return flutterEngine;
    }
    
    public static func getBackgroundMethodChannel(flutterEngine: FlutterEngine)-> FlutterMethodChannel? {
        backgroundMethodChannel = backgroundMethodChannel ?? FlutterMethodChannel(name: SwiftBackgroundLocationTrackerPlugin.BACKGROUND_CHANNEL_NAME, binaryMessenger: flutterEngine.binaryMessenger)
        return backgroundMethodChannel
    }
}

fileprivate enum BackgroundMethods: String {
    case initialized = "initialized"
    case onLocationUpdate = "onLocationUpdate"
}

extension SwiftBackgroundLocationTrackerPlugin: CLLocationManagerDelegate {
    private static let BACKGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/background_channel"
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else {
            CustomLogger.log(message: "No location ...")
            return
        }
        guard let flutterEngine = SwiftBackgroundLocationTrackerPlugin.getFlutterEngine() else {
            CustomLogger.log(message: "No flutter engine available ...")
            return
        }
        
        CustomLogger.log(message: "NEW LOCATION: \(location.coordinate.latitude): \(location.coordinate.longitude)")
        
        guard let backgroundMethodChannel = SwiftBackgroundLocationTrackerPlugin.getBackgroundMethodChannel(flutterEngine: flutterEngine) else {
            CustomLogger.log(message: "No flutter background channel method available ...")
            return
        }
        CustomLogger.log(message: "Ready to set background channel")
        backgroundMethodChannel.setMethodCallHandler { (call, result) in
            switch call.method {
            case BackgroundMethods.initialized.rawValue:
                result(true)
                let locationData :[String: Any] = [
                    "lat": location.coordinate.latitude,
                    "lon": location.coordinate.longitude,
                    "logging_enabled": SharedPrefsUtil.isLoggingEnabled(),
                ]
                
                CustomLogger.log(message: "INITIALIZED, ready to send location updates")
                backgroundMethodChannel.invokeMethod(BackgroundMethods.onLocationUpdate.rawValue, arguments: locationData, result: { flutterResult in
                    CustomLogger.log(message: "Received result: \(flutterResult.debugDescription)")
                })
            default:
                CustomLogger.log(message: "Not implemented method -> \(call.method)")
                result(FlutterMethodNotImplemented)
            }
        }
    }
}
