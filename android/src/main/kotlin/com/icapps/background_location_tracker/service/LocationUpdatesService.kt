package com.icapps.background_location_tracker.service

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.icapps.background_location_tracker.flutter.FlutterBackgroundManager
import com.icapps.background_location_tracker.utils.ActivityCounter
import com.icapps.background_location_tracker.utils.Logger
import com.icapps.background_location_tracker.utils.NotificationUtil
import com.icapps.background_location_tracker.utils.NotificationUtil.NOTIFICATION_ID
import com.icapps.background_location_tracker.utils.SharedPrefsUtil
import java.io.PrintWriter
import java.io.StringWriter

private const val timeOut = 24 * 60 * 60 * 1000L /*24 hours max */

internal class LocationUpdatesService : Service() {
    private val binder: IBinder = LocalBinder()

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private var changingConfiguration = false

    /**
     * Contains parameters used by [com.google.android.gms.location.FusedLocationProviderApi].
     */
    private var locationRequest: LocationRequest? = null

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var fusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Callback for changes in location.
     */
    private var locationCallback: LocationCallback? = null

    private var wakeLock: PowerManager.WakeLock? = null

    /**
     * The current location.
     */
    private var location: Location? = null

    override fun onCreate() {
        Logger.debug(TAG, "ON CREATE SERVICE")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mijnmooiestraat:location_updates")
        createLocationRequest()
        getLastLocation()

        if (SharedPrefsUtil.isTracking(this)) {
            startTracking()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug(TAG, "Service started")
        val startedFromNotification = intent?.getBooleanExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            false
        ) ?: false

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            stopTracking()
            stopSelf()
            return START_NOT_STICKY // Don't restart the service if it's killed
        }

        // Immediately start as foreground service if tracking is enabled
        if (SharedPrefsUtil.isTracking(this) && !serviceIsRunningInForeground(this)) {
            Logger.debug(TAG, "Starting as foreground service from onStartCommand")
            if (wakeLock?.isHeld != true) {
                wakeLock?.acquire(timeOut)
            }
            NotificationUtil.startForeground(this, location)
        }

        // Tells the system to try to recreate the service after it has been killed.
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changingConfiguration = true
    }

    override fun onBind(intent: Intent): IBinder? {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Logger.debug(TAG, "OnBind")
        stopForegroundService()
        changingConfiguration = false
        return binder
    }

    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    override fun onRebind(intent: Intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Logger.debug(TAG, "OnRebind")
        stopForegroundService()
        changingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Logger.debug(TAG, "Last client unbound from service")

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.

        try {
            // Always maintain the service as foreground when tracking is enabled
            if (SharedPrefsUtil.isTracking(this)) {
                Logger.debug(TAG, "Starting foreground service in onUnbind")
                if (wakeLock?.isHeld != true) {
                    wakeLock?.acquire(timeOut)
                }
                NotificationUtil.startForeground(this, location)
                
                // Ensure location updates continue
                ensureLocationUpdatesActive()
            }
        } catch(e:Throwable) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            e.printStackTrace(pw)
            pw.flush()
            Logger.error(sw.toString(),"onUnbind failed to execute");
        }

        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        Logger.debug(TAG, "Destroy")
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        
        // Try to restart the service if tracking is still enabled
        if (SharedPrefsUtil.isTracking(this)) {
            Logger.debug(TAG, "Service destroyed but tracking still enabled - restarting service")
            val intent = Intent(applicationContext, LocationUpdatesService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun startTracking() {
        wakeLock?.acquire(timeOut)

        Logger.debug(TAG, "Requesting location updates")
        SharedPrefsUtil.saveIsTracking(this, true)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(applicationContext, LocationUpdatesService::class.java))
                NotificationUtil.startForeground(this, location)
            } else {
                startService(Intent(applicationContext, LocationUpdatesService::class.java))
                // Still make a foreground service to improve reliability on older Android versions
                NotificationUtil.startForeground(this, location)
            }
            
            ensureLocationUpdatesActive()
        } catch (unlikely: SecurityException) {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            SharedPrefsUtil.saveIsTracking(this, false)
            Logger.error(TAG, "Lost location permission. Could not request updates. $unlikely")
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun stopTracking() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release();
        }
        Logger.debug(TAG, "Removing location updates")
        val locationCallback = locationCallback ?: return
        try {
            fusedLocationClient?.removeLocationUpdates(locationCallback)
            isLocationUpdatesActive = false
            SharedPrefsUtil.saveIsTracking(this, false)
            
            // Stop foreground service and remove notification
            stopForegroundService()
            
            // Cancel the notification explicitly
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
            
            // Stop service
            stopSelf()
        } catch (unlikely: SecurityException) {
            SharedPrefsUtil.saveIsTracking(this, true)
            Logger.error(TAG, "Lost location permission. Could not remove updates. $unlikely")
        }
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient?.lastLocation?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    location = task.result
                } else {
                    Logger.warning(TAG, "Failed to get location.")
                }
            }
        } catch (unlikely: SecurityException) {
            Logger.error(TAG, "Lost location permission.$unlikely")
        }
    }

    private fun onNewLocation(location: Location?) {
        if (location == null) return;
        Logger.debug(TAG, "New location: $location")
        this.location = location

        // Update notification if needed
        if (serviceIsRunningInForeground(this) && 
            SharedPrefsUtil.isNotificationLocationUpdatesEnabled(applicationContext)) {
            Logger.debug(TAG, "Service is running in foreground & notification updates are enabled. So we update the notification")
            NotificationUtil.showNotification(this, location)
        }
        
        // Only send location via one path based on app state
        // If app is in foreground, send via broadcast - the Flutter app will handle it directly
        // If app is in background, send via FlutterBackgroundManager
        if (!ActivityCounter.isAppInBackground()) {
            Logger.debug(TAG, "App in foreground, sending location via broadcast")
            val intent = Intent(ACTION_BROADCAST)
            intent.putExtra(EXTRA_LOCATION, location)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        } else {
            Logger.debug(TAG, "App in background, sending location via background manager")
            FlutterBackgroundManager.sendLocation(applicationContext, location)
        }
    }

    /**
     * Sets the location request parameters.
     */
    private fun createLocationRequest() {
        val interval = SharedPrefsUtil.trackingInterval(this)
        val distanceFilter = SharedPrefsUtil.distanceFilter(this)
        locationRequest = LocationRequest.create()
            .setInterval(interval)
            .setFastestInterval(interval / 2)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setSmallestDisplacement(distanceFilter)
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        val service: LocationUpdatesService
            get() = this@LocationUpdatesService
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The [Context].
     */
    @Suppress("DEPRECATION")
    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    // Ensure location updates are active
    private fun ensureLocationUpdatesActive() {
        val locationRequest = locationRequest ?: return
        val locationCallback = locationCallback ?: return
        
        // Avoid creating duplicate location requests
        if (isLocationUpdatesActive) {
            Logger.debug(TAG, "Location updates already active, skipping")
            return
        }
        
        try {
            // First remove any existing updates to avoid duplicates
            fusedLocationClient?.removeLocationUpdates(locationCallback)
            
            // Then request new updates
            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper() ?: Looper.myLooper())
            isLocationUpdatesActive = true
            Logger.debug(TAG, "Location updates requested (or refreshed)")
        } catch (unlikely: SecurityException) {
            isLocationUpdatesActive = false
            Logger.error(TAG, "Lost location permission. Could not request updates. $unlikely")
        }
    }

    companion object {
        private const val PACKAGE_NAME = "com.icapps.background_location_tracker"
        private val TAG = LocationUpdatesService::class.java.simpleName

        const val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
        const val EXTRA_LOCATION = "$PACKAGE_NAME.location"
        const val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"
    }
    
    // Track if location updates are active to avoid duplicates
    private var isLocationUpdatesActive = false
}