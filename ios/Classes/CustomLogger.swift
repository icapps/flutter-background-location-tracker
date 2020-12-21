//
//  CustomLogger.swift
//  background_location_tracker
//
//  Created by Dimmy Maenhout on 21/12/2020.
//

import Foundation
struct CustomLogger {
    
    static func log(message: String) {
        if SharedPrefsUtil.isLoggingEnabled() {
            print(message)
        }
    }
}
