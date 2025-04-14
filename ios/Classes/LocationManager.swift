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
        manager.headingFilter = 5.0
        manager.headingOrientation = .portrait
        if CLLocationManager.headingAvailable() {
            manager.startUpdatingHeading()
        }
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
    
    class func ensureCorrectPermissions() {
        let authorizationStatus: CLAuthorizationStatus
        if #available(iOS 14.0, *) {
            authorizationStatus = sharedLocationManager.authorizationStatus
        } else {
            authorizationStatus = CLLocationManager.authorizationStatus()
        }
        
        if authorizationStatus == .notDetermined {
            if #available(iOS 14.0, *) {
                sharedLocationManager.requestWhenInUseAuthorization()
            } else {
                sharedLocationManager.requestAlwaysAuthorization()
            }
        } else if authorizationStatus == .denied || authorizationStatus == .restricted {
            CustomLogger.log(message: "Location permissions denied or restricted")
        }
    }
}
