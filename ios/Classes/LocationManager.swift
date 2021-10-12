//
//  LocationManager.swift
//  background_location_tracker
//
//  Created by Dimmy Maenhout on 16/12/2020.
//

import Foundation
import CoreLocation

class LocationManager {
    
    private static var sharedLocationManager: CLLocationManager = {
        let manager = CLLocationManager()
        manager.activityType = SharedPrefsUtil.activityType()
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = SharedPrefsUtil.distanceFilter()
        manager.pausesLocationUpdatesAutomatically = false
        if #available(iOS 11, *) {
            manager.showsBackgroundLocationIndicator = true
        }
        if #available(iOS 9.0, *) {
            manager.allowsBackgroundLocationUpdates = true
        }
        return manager
    }()
    
    class func shared() -> CLLocationManager {
        return sharedLocationManager
    }
}
