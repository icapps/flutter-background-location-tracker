package com.icapps.background_location_tracker.service

import android.location.Location

interface LocationUpdateListener {
    fun onLocationUpdate(location: Location)
}