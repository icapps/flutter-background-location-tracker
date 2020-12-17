//
//  SharedPrefsUtil.swift
//  background_location_tracker
//
//  Created by Dimmy Maenhout on 17/12/2020.
//

import Foundation

class SharedPrefsUtil {
    private let SHARED_PREFS_FILE_NAME = "background_location_tracker"
    
    private let KEY_CALBACK_HANDLER = "background.location.tracker.manager.CALLBACK_DISPATCHER_HANDLE_KEY"
    private let KEY_IS_TRACKING = "background.location.tracker.manager.IS_TRACKING"
    private let KEY_LOGGING_ENABED = "background.location.tracker.manager.LOGGIN_ENABLED"
    
    // KeyChainWrapper gebruiken
    
    func saveCallBackDispatcherHandleKey(callBackHandle: Int64) {
        
    }
    
    func getCallbackHandle() -> Int64 {
        
    }
    
    func hasCallbackHandle() -> Bool {
    
    }
    
    func saveIsTracking(_ isTracking: Bool) {
        
    }
}
