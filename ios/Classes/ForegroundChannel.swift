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
        let loggingEnabledKey = "logging_enabled"
        let activityTypeKey = "ios_activity_type"
        let distanceFilterKey = "ios_distance_filter"
        let restartAfterKillKey = "ios_restart_after_kill"
        let map = call.arguments as? [String: Any]
        guard let callbackDispatcherHandle = map?[callBackHandleKey] else {
            result(false)
            return
        }
        
        
        let loggingEnabled: Bool = map?[loggingEnabledKey] as? Bool ?? false
        SharedPrefsUtil.saveLoggingEnabled(loggingEnabled)
        SharedPrefsUtil.saveRestartAfterKillEnabled(map?[restartAfterKillKey] as? Bool ?? false)
        
        let activityType: CLActivityType
        switch (map?[activityTypeKey] as? String ?? "AUTOMOTIVE") {
        case "OTHER":
            activityType = .other
        case "FITNESS":
            activityType = .fitness
        case "NAVIGATION":
            activityType = .otherNavigation
        case "AIRBORNE":
            if #available(iOS 12.0, *) {
                activityType = .airborne
            } else {
                activityType = .automotiveNavigation
            }
        case "AUTOMOTIVE":
            activityType = .automotiveNavigation
        default:
            activityType = .automotiveNavigation
        }
        
        SharedPrefsUtil.saveActivityType(activityType)
        SharedPrefsUtil.saveDistanceFilter(map?[distanceFilterKey] as? Double ?? kCLDistanceFilterNone)
        
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
