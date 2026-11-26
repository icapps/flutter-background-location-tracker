package com.icapps.background_location_tracker.service

import android.app.ActivityManager
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
import com.google.android.gms.tasks.Task
import com.icapps.background_location_tracker.flutter.FlutterBackgroundManager
import com.icapps.background_location_tracker.utils.ActivityCounter
import com.icapps.background_location_tracker.utils.Logger
import com.icapps.background_location_tracker.utils.NotificationUtil
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
     * The request that was last handed to the Fused Location Provider. Null when not tracking or
     * when the last request failed, in which case the next change always requests updates again.
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

    /**
     * The last location that was forwarded to flutter. Requesting updates again makes the Fused
     * Location Provider deliver its cached location, this is used to not forward it twice.
     */
    private var lastForwardedLocation: Location? = null

    override fun onCreate() {
        Logger.debug(TAG, "ON CREATE SERVICE")
        runningService = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocations(locationResult.locations)
            }
        }
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mijnmooiestraat:location_updates")
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
            if (!changingConfiguration && SharedPrefsUtil.isTracking(this)) {
                Logger.debug(TAG, "Starting foreground service")
                if (wakeLock?.isHeld != true) {
                    wakeLock?.acquire(timeOut)
                }
                NotificationUtil.startForeground(this, location)
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
        if (runningService === this) {
            runningService = null
        }
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        
        // Force clean up Flutter background resources when service is destroyed
        FlutterBackgroundManager.forceCleanup()
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun startTracking() {
        wakeLock?.acquire(timeOut)

        Logger.debug(TAG, "Requesting location updates")
        SharedPrefsUtil.saveIsTracking(this, true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ActivityCounter.isAppInBackground()) {
            startForegroundService(Intent(applicationContext, LocationUpdatesService::class.java))
            NotificationUtil.startForeground(this, location)
        } else {
            startService(Intent(applicationContext, LocationUpdatesService::class.java))
        }
        try {
            requestLocationUpdates(createLocationRequest())?.addOnFailureListener {
                Logger.error(TAG, "Could not request updates. $it")
            }
        } catch (unlikely: SecurityException) {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            SharedPrefsUtil.saveIsTracking(this, false)
            Logger.error(TAG, "Lost location permission. Could not request updates. $unlikely")
        }
    }

    /**
     * Saves the interval and applies it to the active location request. The callback stays
     * registered, so there is no gap in the updates.
     *
     * When the request fails, the previous interval is restored and [onComplete] receives the error.
     */
    fun updateTrackingInterval(interval: Long, onComplete: (Exception?) -> Unit) {
        val previousInterval = SharedPrefsUtil.trackingInterval(this)
        SharedPrefsUtil.saveTrackingInterval(this, interval)
        if (locationRequest?.intervalMillis == interval) {
            onComplete(null)
            return
        }

        Logger.debug(TAG, "Updating tracking interval to $interval ms")
        val request = createLocationRequest()
        fun onFailed(error: Exception) {
            Logger.error(TAG, "Could not update the tracking interval. $error")
            // Leave the saved interval alone when it was changed again in the meantime
            if (SharedPrefsUtil.trackingInterval(this) == interval) {
                SharedPrefsUtil.saveTrackingInterval(this, previousInterval)
            }
            onComplete(error)
        }
        try {
            val task = requestLocationUpdates(request)
            if (task == null) {
                onFailed(IllegalStateException("The location service is not ready"))
                return
            }
            task.addOnCompleteListener {
                if (it.isSuccessful) onComplete(null) else onFailed(it.exception ?: IllegalStateException("Location request was cancelled"))
            }
        } catch (unlikely: SecurityException) {
            onFailed(unlikely)
        }
    }

    /**
     * Applies the saved interval and distance filter if they differ from the active location request.
     */
    fun applySavedConfig() {
        val request = createLocationRequest()
        val current = locationRequest
        if (current != null && current.intervalMillis == request.intervalMillis
            && current.minUpdateDistanceMeters == request.minUpdateDistanceMeters) return

        Logger.debug(TAG, "Applying changed tracking config")
        try {
            requestLocationUpdates(request)?.addOnFailureListener {
                Logger.error(TAG, "Could not apply changed tracking config. $it")
            }
        } catch (unlikely: SecurityException) {
            Logger.error(TAG, "Lost location permission. Could not apply changed tracking config. $unlikely")
        }
    }

    /**
     * Requests location updates with the existing callback. An active request with that callback is
     * replaced. A lost permission is either thrown or reported by the returned task.
     */
    @Throws(SecurityException::class)
    private fun requestLocationUpdates(request: LocationRequest): Task<Void>? {
        val client = fusedLocationClient ?: return null
        val callback = locationCallback ?: return null
        locationRequest = request
        try {
            return client.requestLocationUpdates(request, callback, Looper.getMainLooper()).addOnFailureListener {
                if (locationRequest === request) locationRequest = null
            }
        } catch (e: SecurityException) {
            locationRequest = null
            throw e
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
            locationRequest = null
            lastForwardedLocation = null
            SharedPrefsUtil.saveIsTracking(this, false)
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

    /**
     * @param locations The new locations, ordered from oldest to newest.
     */
    private fun onNewLocations(locations: List<Location>) {
        val newLocations = locations.filterNot { isLastForwardedLocation(it) }
        val lastLocation = newLocations.lastOrNull() ?: return
        Logger.debug(TAG, "New locations: $newLocations")
        location = lastLocation
        lastForwardedLocation = lastLocation

        if (serviceIsRunningInForeground(this)) {
            if (SharedPrefsUtil.isNotificationLocationUpdatesEnabled(applicationContext)) {
                Logger.debug(TAG, "Service is running the foreground & notification updates are enabled. So we update the notification")
                NotificationUtil.showNotification(this, lastLocation)
            }
            newLocations.forEach { FlutterBackgroundManager.sendLocation(applicationContext, it) }
        } else {
            val broadcastManager = LocalBroadcastManager.getInstance(applicationContext)
            newLocations.forEach {
                val intent = Intent(ACTION_BROADCAST)
                intent.putExtra(EXTRA_LOCATION, it)
                broadcastManager.sendBroadcast(intent)
            }
        }
    }

    private fun isLastForwardedLocation(location: Location): Boolean {
        val last = lastForwardedLocation ?: return false
        return location.elapsedRealtimeNanos == last.elapsedRealtimeNanos && location.time == last.time
    }

    /**
     * Creates a location request with the saved interval and distance filter.
     */
    private fun createLocationRequest(): LocationRequest {
        val interval = SharedPrefsUtil.trackingInterval(this)
        val distanceFilter = SharedPrefsUtil.distanceFilter(this)
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(interval / 2)
            .setMinUpdateDistanceMeters(distanceFilter)
            .build()
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

    companion object {
        private const val PACKAGE_NAME = "com.icapps.background_location_tracker"
        private val TAG = LocationUpdatesService::class.java.simpleName

        const val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
        const val EXTRA_LOCATION = "$PACKAGE_NAME.location"
        const val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"

        /**
         * The running service, also when no activity is bound to it (e.g. after a process restart).
         * Set between [onCreate] and [onDestroy], only accessed on the main thread.
         */
        var runningService: LocationUpdatesService? = null
            private set
    }
}