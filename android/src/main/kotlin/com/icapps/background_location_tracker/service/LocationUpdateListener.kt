package com.icapps.background_location_tracker.service

import android.location.Location

internal interface LocationUpdateListener {
    fun onLocationUpdate(location: Location)
}