package com.icapps.background_location_tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import com.icapps.background_location_tracker.service.LocationUpdatesService

internal abstract class LocationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        val location = intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
        if (location != null) onLocationUpdate(location)
    }

    abstract fun onLocationUpdate(location: Location)
}
