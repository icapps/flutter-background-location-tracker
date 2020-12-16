//
//  BackgroundLocationTrackerPlugin.swift
//  Runner
//
//  Created by Dimmy Maenhout on 16/12/2020.
//

import Foundation
import CoreLocation

public class BackgroundLocationTrackingPlugin: NSObject, FlutterPlugin {
    private static let CHANNEL = "com.icapps.background_location_tracker/foreground_channel" //"com.icapps.background_location_tracker/background_channel"
    
    let locationManager: CLLocationManager = CLLocationManager()
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: CHANNEL, binaryMessenger: registrar.messenger())
        let instance = BackgroundLocationTrackingPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "initialized":
            // MARK: - TODO implement
        break
        default:
            result(FlutterMethodNotImplemented)
        }
    }
}
