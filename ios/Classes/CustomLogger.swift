//
//  CustomLogger.swift
//  background_location_tracker
//
//  Created by Dimmy Maenhout on 21/12/2020.
//

import os
import Foundation
struct CustomLogger {
    
    static func log(message: String) {
        if #available(iOS 10.0, *) {
            let app = OSLog(subsystem: "com.waver.driver", category: "background tracker")
            os_log("🔥 background-location log: %{public}@", log: app, type: .error, message)
        }
        if SharedPrefsUtil.isLoggingEnabled() {
            print(message)
        }
    }
}
