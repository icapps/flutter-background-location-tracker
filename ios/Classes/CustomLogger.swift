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
        if SharedPrefsUtil.isLoggingEnabled() {
            if #available(iOS 10.0, *) {
                let app = OSLog(subsystem: "com.icapps.background_location_tracker", category: "background tracker")
                os_log("🔥 background-location log: %{public}@", log: app, type: .error, message)
            }
            print(message)
        }
    }
}
