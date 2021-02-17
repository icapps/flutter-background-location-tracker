import Flutter
import UIKit
import CoreLocation

public class SwiftBackgroundLocationTrackerPlugin: FlutterPluginAppLifeCycleDelegate {
    
    static let identifier = "com.icapps.background_location_tracker"
    
    private let flutterThreadLabelPrefix = "\(identifier).BackgroundLocationTracker"
    
    private static var foregroundChannel: ForegroundChannel? = nil
    
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
}

fileprivate enum BackgroundMethods: String {
    case initialized = "initialized"
    case onLocationUpdate = "onLocationUpdate"
}

extension SwiftBackgroundLocationTrackerPlugin: CLLocationManagerDelegate {
    private static let BACKGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/background_channel"
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        do {
            guard let location = locations.last else {
                CustomLogger.log(message: "No location ...")
                return
            }
            
            CustomLogger.log(message: "NEW LOCATION: \(location.coordinate.latitude): \(location.coordinate.longitude)")
            
            guard let callbackHandle = SharedPrefsUtil.getCallbackHandle(),
                  let flutterCallbackInformation = FlutterCallbackCache.lookupCallbackInformation(callbackHandle)
            else { return }
            
            CustomLogger.log(message: "callbackHandle: \(callbackHandle)")
            CustomLogger.log(message: "flutterCallbackInformation: \(flutterCallbackInformation)")
            CustomLogger.log(message: "flutterCallbackInformation-name: \(String(describing: flutterCallbackInformation.callbackName))")
            CustomLogger.log(message: "flutterCallbackInformation-callbackLibraryPath: \(String(describing: flutterCallbackInformation.callbackLibraryPath))")
            
            let flutterEngine: FlutterEngine = FlutterEngine(name: flutterThreadLabelPrefix, project: nil, allowHeadlessExecution: true)
            flutterEngine.run(withEntrypoint: flutterCallbackInformation.callbackName, libraryURI: flutterCallbackInformation.callbackLibraryPath)
            SwiftBackgroundLocationTrackerPlugin.flutterPluginRegistrantCallback?(flutterEngine)
            
            let backgroundMethodChannel: FlutterMethodChannel = FlutterMethodChannel(name: SwiftBackgroundLocationTrackerPlugin.BACKGROUND_CHANNEL_NAME, binaryMessenger: flutterEngine.binaryMessenger)
            backgroundMethodChannel.setMethodCallHandler { (call, result) in
                switch call.method {
                case BackgroundMethods.initialized.rawValue:
                    result(true)
                    let locationData :[String: Any] = [
                        "lat": location.coordinate.latitude,
                        "lon": location.coordinate.longitude,
                        "logging_enabled": SharedPrefsUtil.isLoggingEnabled(),
                    ]
                    backgroundMethodChannel.invokeMethod(BackgroundMethods.onLocationUpdate.rawValue, arguments: locationData, result: { flutterResult in
                        flutterEngine.destroyContext()
                    })
                default:
                    result(FlutterMethodNotImplemented)
                    flutterEngine.destroyContext()
                }
            }
        } catch let error {
            CustomLogger.log(message: "CRASH!!! : \(error.localizedDescription)")
        }
    }
}
