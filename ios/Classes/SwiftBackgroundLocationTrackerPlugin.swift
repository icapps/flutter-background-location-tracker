import Flutter
import UIKit
import CoreLocation

public class SwiftBackgroundLocationTrackerPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "background_location_tracker", binaryMessenger: registrar.messenger())
    let instance = SwiftBackgroundLocationTrackerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}



fileprivate enum BackgroundMethods: String {
    case onLocationUpdate = "onLocationUpdate"
}

class BackgroundChannel: NSObject, FlutterPlugin {
    
    private static let BACKGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/background_channel"
    
    private let locationManager = LocationManager.shared()
    
    static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: BACKGROUND_CHANNEL_NAME, binaryMessenger: registrar.messenger())
        let instance = BackgroundChannel()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        locationManager.delegate = self
        switch call.method {
        case BackgroundMethods.onLocationUpdate.rawValue:
            break
        default:
            result(FlutterMethodNotImplemented)
        }
    }
}

extension BackgroundChannel: CLLocationManagerDelegate {
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let location = locations.last else { return }
        
    }
}
