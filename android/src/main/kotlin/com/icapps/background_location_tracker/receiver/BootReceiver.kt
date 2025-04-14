package com.icapps.background_location_tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.icapps.background_location_tracker.service.LocationUpdatesService
import com.icapps.background_location_tracker.utils.Logger
import com.icapps.background_location_tracker.utils.SharedPrefsUtil

/**
 * Boot receiver to restart location tracking after device reboot if it was enabled
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Logger.debug("BootReceiver", "Device booted, checking if tracking was enabled")
            if (SharedPrefsUtil.isTracking(context)) {
                Logger.debug("BootReceiver", "Tracking was enabled, restarting service")
                // Start the location service
                val serviceIntent = Intent(context, LocationUpdatesService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
} 