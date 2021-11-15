//
//  SharedPrefsUtil.swift
//  background_location_tracker
//
//  Created by Dimmy Maenhout on 17/12/2020.
//

import Foundation
import CoreLocation

struct SharedPrefsUtil {
    
    private static let KEY_CALLBACK_HANDLER = "background.location.tracker.manager.CALLBACK_DISPATCHER_HANDLE_KEY"
    private static let KEY_IS_TRACKING = "background.location.tracker.manager.IS_TRACKING"
    private static let KEY_IS_RESTART_AFTER_KILL = "background.location.tracker.manager.RESTART_AFTER_KILL"
    private static let KEY_LOGGING_ENABLED = "background.location.tracker.manager.LOGGIN_ENABLED"
    private static let KEY_DISTANCE_FILTER = "background.location.tracker.manager.DISTANCE_FILTER"
    private static let KEY_ACTIVITY_TYPE = "background.location.tracker.manager.KEY_ACTIVITY_TYPE"
    
    private static let userDefaults = UserDefaults(suiteName: "\(SwiftBackgroundLocationTrackerPlugin.identifier).userDefaults")!
    
    static func saveCallBackDispatcherHandleKey(callBackHandle: Int64?) {
        store(callBackHandle, key: SharedPrefsUtil.KEY_CALLBACK_HANDLER)
    }
    
    static func getCallbackHandle() -> Int64? {
        return getValue(for: SharedPrefsUtil.KEY_CALLBACK_HANDLER)
    }
    
    static func hasCallbackHandle() -> Bool {
        return getValue(for: SharedPrefsUtil.KEY_CALLBACK_HANDLER) ?? false
    }
    
    static func saveIsTracking(_ isTracking: Bool) {
        store(isTracking, key: SharedPrefsUtil.KEY_IS_TRACKING)
    }
    
    static func isTracking() -> Bool {
        return getValue(for: SharedPrefsUtil.KEY_IS_TRACKING) ?? false
    }
    
    static func restartAfterKill() -> Bool {
        return getValue(for: SharedPrefsUtil.KEY_IS_RESTART_AFTER_KILL) ?? false
    }
    
    static func saveRestartAfterKillEnabled(_ isRestartEnabled: Bool) {
        store(isRestartEnabled, key: SharedPrefsUtil.KEY_IS_RESTART_AFTER_KILL)
    }
    
    static func saveLoggingEnabled(_ isLoggingEnabled: Bool) {
        store(isLoggingEnabled, key: SharedPrefsUtil.KEY_LOGGING_ENABLED)
    }
    
    static func saveDistanceFilter(_ distanceFilter: CLLocationDistance) {
        store(distanceFilter, key: SharedPrefsUtil.KEY_DISTANCE_FILTER)
    }
    
    static func saveActivityType(_ activityType: CLActivityType) {
        store(activityType.rawValue, key: SharedPrefsUtil.KEY_ACTIVITY_TYPE)
    }
    
    static func isLoggingEnabled() -> Bool {
        return getValue(for: SharedPrefsUtil.KEY_LOGGING_ENABLED) ?? false
    }
    
    static func distanceFilter() -> CLLocationDistance {
        return getValue(for: SharedPrefsUtil.KEY_DISTANCE_FILTER) ?? kCLDistanceFilterNone
    }
    
    static func activityType() -> CLActivityType {
        guard let rawValue: Int = getValue(for: SharedPrefsUtil.KEY_ACTIVITY_TYPE) else { return CLActivityType.other }
        
        return CLActivityType.init(rawValue: rawValue) ?? CLActivityType.other
    }
    
    // MARK: - Helper methods
    
    private static func store<T>(_ value: T, key: String) {
        userDefaults.setValue(value, forKey: key)
    }
    
    private static func getValue<T>(for key: String) -> T? {
        return userDefaults.value(forKey: key) as? T
    }
}
