package com.icapps.background_location_tracker.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.icapps.background_location_tracker.flutter.FlutterBackgroundManager
import com.icapps.background_location_tracker.receiver.LocationReceiver
import com.icapps.background_location_tracker.utils.ActivityCounter
import com.icapps.background_location_tracker.utils.Logger
import com.icapps.background_location_tracker.utils.SharedPrefsUtil

internal class LocationServiceConnection(private val listener: LocationUpdateListener) : ServiceConnection {
    var service: LocationUpdatesService? = null
    private var locationReceiver = MyLocationReceiver()
    private var bound = false
    private var isBinding = false

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        val localBinder = binder as LocationUpdatesService.LocalBinder
        service = localBinder.service
        bound = true
        isBinding = false
        
        Logger.debug("LocationServiceConnection", "Service connected")
        
        // If tracking is enabled, ensure the service is properly started
        // But only do this if app is in foreground to prevent unnecessary setup
        if (!ActivityCounter.isAppInBackground()) {
            service?.let { 
                if (SharedPrefsUtil.isTracking(it)) {
                    Logger.debug("LocationServiceConnection", "Service connected and tracking is enabled - ensuring service is running")
                    it.startTracking()
                }
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Logger.debug("LocationServiceConnection", "Service disconnected")
        service = null
        bound = false
        isBinding = false
    }
    
    private fun ensureServiceIsRunning() {
        service?.let { 
            if (SharedPrefsUtil.isTracking(it)) {
                Logger.debug("LocationServiceConnection", "Tracking is enabled but service disconnected - restarting")
                it.startTracking()
            }
        }
    }

    fun bound(ctx: Context) {
        // Prevent multiple concurrent binding attempts
        if (bound || isBinding) {
            Logger.debug("LocationServiceConnection", "Already bound or binding to service")
            return
        }
        
        isBinding = true
        
        // Start and bind to service
        val intent = Intent(ctx, LocationUpdatesService::class.java)
        
        // First start the service to ensure it keeps running
        if (SharedPrefsUtil.isTracking(ctx)) {
            Logger.debug("LocationServiceConnection", "Starting service before binding")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        }
        
        // Then bind to it
        try {
            ctx.bindService(intent, this, Context.BIND_AUTO_CREATE)
            Logger.debug("LocationServiceConnection", "Binding to service")
        } catch (e: Exception) {
            Logger.error("LocationServiceConnection", "Error binding to service: ${e.message}")
            isBinding = false
        }
    }

    fun onResume(ctx: Context) {
        // Register receiver and ensure service is bound
        LocalBroadcastManager.getInstance(ctx).registerReceiver(
            locationReceiver, 
            IntentFilter(LocationUpdatesService.ACTION_BROADCAST)
        )
        
        // Release background resources when coming to foreground
        if (!ActivityCounter.isAppInBackground()) {
            Logger.debug("LocationServiceConnection", "App in foreground, releasing background resources")
            FlutterBackgroundManager.releaseResources()
        }
        
        // Ensure we're bound to the service on resume, but only if we need to be
        if (SharedPrefsUtil.isTracking(ctx)) {
            bound(ctx)
        }
    }

    fun onPause(ctx: Context) {
        try {
            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(locationReceiver)
        } catch (e: Exception) {
            Logger.error("LocationServiceConnection", "Error unregistering receiver: ${e.message}")
        }
    }

    fun onStop(ctx: Context) {
        if (bound) {
            // When stopping, do NOT unbind if tracking is enabled
            if (!SharedPrefsUtil.isTracking(ctx)) {
                try {
                    ctx.unbindService(this)
                    bound = false
                    Logger.debug("LocationServiceConnection", "Unbinding from service as tracking is disabled")
                } catch (e: Exception) {
                    Logger.error("LocationServiceConnection", "Error unbinding from service: ${e.message}")
                }
            } else {
                Logger.debug("LocationServiceConnection", "Not unbinding from service as tracking is enabled")
            }
        }
    }

    inner class MyLocationReceiver : LocationReceiver() {
        override fun onLocationUpdate(location: Location) = listener.onLocationUpdate(location)
    }
}