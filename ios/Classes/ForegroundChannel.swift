//
//  ForegroundChannel.swift
//  background_location_tracker
//
//  Created by Dimmy Maenhout on 17/12/2020.
//

import Foundation
import CoreLocation
import Flutter

fileprivate enum ForegroundMethods: String {
    case initialize = "initialize"
    case isTracking = "isTracking"
    case startTracking = "startTracking"
    case stopTracking = "stopTracking"
}

public class ForegroundChannel : NSObject {
    private static let FOREGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/foreground_channel"
    
    private let locationManager = LocationManager.shared()
    
    private var userLoc: CLLocation?
    
    private let userDefaults = UserDefaults.standard
    
    public static func getMethodChannel(with registrar: FlutterPluginRegistrar) -> FlutterMethodChannel {
        return FlutterMethodChannel(name: FOREGROUND_CHANNEL_NAME, binaryMessenger: registrar.messenger())
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        locationManager.delegate = self
        switch call.method {
        case ForegroundMethods.initialize.rawValue:
            initialize(call: call, result: result)
        case ForegroundMethods.isTracking.rawValue:
            isTracking(result)
        case ForegroundMethods.startTracking.rawValue:
            startTracking(result)
        case ForegroundMethods.stopTracking.rawValue:
            stopTracking(result)
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    // MARK: - private methods
    
    private func initialize(call: FlutterMethodCall, result: @escaping FlutterResult ) {
        let callBackHandleKey = "callback_handle"
        let channelNameKey = "iOS_config_channel_name"
        let keys = [callBackHandleKey, channelNameKey]
//        if !call.method
//        let callbackDispatcherHandleKey = call.
//        userDefaults.  
        result(true)
    }
    
    private func startTracking(_ result: @escaping FlutterResult) {
        locationManager.startUpdatingLocation()
        result(true)
    }
    
    private func stopTracking(_ result: @escaping FlutterResult) {
        result(locationManager.stopUpdatingLocation())
    }
    
    private func isTracking(_ result: @escaping FlutterResult) {
        guard let _ = locationManager.location else { return result(false) }
        return result(true)
    }
}

extension ForegroundChannel: CLLocationManagerDelegate {
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.last {
            userLoc = location
        } else {
            userLoc = nil
        }
    }
}
