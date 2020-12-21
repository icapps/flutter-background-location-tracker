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
    
    private var isTracking = false
    private static let FOREGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/foreground_channel"
    
    private let locationManager = LocationManager.shared()
    
    private let userDefaults = UserDefaults.standard
    
    public static func getMethodChannel(with registrar: FlutterPluginRegistrar) -> FlutterMethodChannel {
        return FlutterMethodChannel(name: FOREGROUND_CHANNEL_NAME, binaryMessenger: registrar.messenger())
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
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
        let map = call.arguments as? [String: Any]
        guard let callbackDispatcherHandle = map?[callBackHandleKey] else {
            result(false)
            return
        }
        SharedPrefsUtil.saveCallBackDispatcherHandleKey(callBackHandle: callbackDispatcherHandle as? Int64)
        SharedPrefsUtil.saveIsTracking(isTracking)
        result(true)
    }
    
    private func startTracking(_ result: @escaping FlutterResult) {
        locationManager.startUpdatingLocation()
        isTracking = true
        SharedPrefsUtil.saveIsTracking(isTracking)
        result(true)
    }
    
    private func stopTracking(_ result: @escaping FlutterResult) {
        locationManager.stopUpdatingLocation()
        isTracking = false
        SharedPrefsUtil.saveIsTracking(isTracking)
        result(true)
    }
    
    private func isTracking(_ result: @escaping FlutterResult) {
        SharedPrefsUtil.saveIsTracking(isTracking)
        return result(isTracking)
    }
}
