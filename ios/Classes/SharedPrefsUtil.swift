//
//  SharedPrefsUtil.swift
//  background_location_tracker
//
//  Created by Dimmy Maenhout on 17/12/2020.
//

import Foundation

struct SharedPrefsUtil {
    
    private let KEY_CALBACK_HANDLER = "background.location.tracker.manager.CALLBACK_DISPATCHER_HANDLE_KEY"
    private let KEY_IS_TRACKING = "background.location.tracker.manager.IS_TRACKING"
    private let KEY_LOGGING_ENABED = "background.location.tracker.manager.LOGGIN_ENABLED"
    
    private static let userDefaults = UserDefaults(suiteName: "\(SwiftBackgroundLocationTrackerPlugin.identifier).userDefaults")!
    
    // KeyChainWrapper gebruiken
    
    static func saveCallBackDispatcherHandleKey(callBackHandle: Int64) {
        
    }
    
    static func getCallbackHandle() -> Int64? {
        
    }
    
    static func hasCallbackHandle() -> Bool {
    
    }
    
    static func saveIsTracking(_ isTracking: Bool) {
        
    }
    
    static func isTracking() -> Bool {
    
    }
    
    static func saveLoggingEnabled(_ isTracking: Bool) {
        
    }
    
    static func isLoggingEnabled() -> Bool {
    
    }
}
