//
//  SharedPrefsUtil.swift
//  background_location_tracker
//
//  Created by Dimmy Maenhout on 17/12/2020.
//

import Foundation

struct SharedPrefsUtil {
    
    private static let KEY_CALLBACK_HANDLER = "background.location.tracker.manager.CALLBACK_DISPATCHER_HANDLE_KEY"
    private static let KEY_IS_TRACKING = "background.location.tracker.manager.IS_TRACKING"
    private static let KEY_LOGGING_ENABLED = "background.location.tracker.manager.LOGGIN_ENABLED"
    
    private static let userDefaults = UserDefaults(suiteName: "\(SwiftBackgroundLocationTrackerPlugin.identifier).userDefaults")!
    
    // KeyChainWrapper gebruiken
    
    static func saveCallBackDispatcherHandleKey(callBackHandle: Int64) {
        store(callBackHandle, key: SharedPrefsUtil.KEY_CALLBACK_HANDLER)
    }
    
    static func getCallbackHandle() -> Int64? {
        return getValue(for: SharedPrefsUtil.KEY_CALLBACK_HANDLER)
    }
    
    static func hasCallbackHandle() -> Bool {
        getValue(for: SharedPrefsUtil.KEY_CALLBACK_HANDLER) ?? false
    }
    
    static func saveIsTracking(_ isTracking: Bool) {
        store(isTracking, key: SharedPrefsUtil.KEY_IS_TRACKING)
    }
    
    static func isTracking() -> Bool {
        return getValue(for: SharedPrefsUtil.KEY_IS_TRACKING) ?? false
    }
    
    static func saveLoggingEnabled(_ isLoggingEnabled: Bool) {
        store(isLoggingEnabled, key: SharedPrefsUtil.KEY_LOGGING_ENABLED)
    }
    
    static func isLoggingEnabled() -> Bool {
        return getValue(for: SharedPrefsUtil.KEY_LOGGING_ENABLED) ?? false
    }
    
    // MARK: - Helper methods
    
    private static func store<T>(_ value: T, key: String) {
        userDefaults.setValue(value, forKey: key)
    }
    
    private static func getValue<T>(for key: String) -> T? {
        return userDefaults.value(forKey: key) as? T
    }
}
