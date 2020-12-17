package com.icapps.background_location_tracker.service

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.icapps.background_location_tracker.flutter.FlutterBackgroundManager
import com.icapps.background_location_tracker.utils.Logger
import com.icapps.background_location_tracker.utils.SharedPrefsUtil
import com.icapps.background_location_tracker.utils.NotificationUtil

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
    private var serviceHandler: Handler? = null

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
        createLocationRequest()
        getLastLocation()
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)

        if (SharedPrefsUtil.isTracking(this)) {
            startTracking()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.debug(TAG, "Service started")
        val startedFromNotification = intent?.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false) ?: false

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            stopTracking()
            stopSelf()
        }
        // Tells the system to not try to recreate the service after it has been killed.
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
        stopForeground(true)
        changingConfiguration = false
        return binder
    }

    override fun onRebind(intent: Intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Logger.debug(TAG, "OnRebind")
        stopForeground(true)
        changingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        Logger.debug(TAG, "Last client unbound from service")

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!changingConfiguration && SharedPrefsUtil.isTracking(this)) {
            Logger.debug(TAG, "Starting foreground service")
            NotificationUtil.startForeground(this, location)
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        Logger.debug(TAG, "Destroy")
        serviceHandler!!.removeCallbacksAndMessages(null)
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun startTracking() {
        Logger.debug(TAG, "Requesting location updates")
        SharedPrefsUtil.saveIsTracking(this, true)
        startService(Intent(applicationContext, LocationUpdatesService::class.java))
        try {
            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        } catch (unlikely: SecurityException) {
            SharedPrefsUtil.saveIsTracking(this, false)
            Logger.error(TAG, "Lost location permission. Could not request updates. $unlikely")
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun stopTracking() {
        Logger.debug(TAG, "Removing location updates")
        try {
            fusedLocationClient?.removeLocationUpdates(locationCallback)
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

    private fun onNewLocation(location: Location) {
        Logger.debug(TAG, "New location: $location")
        this.location = location

        if (serviceIsRunningInForeground(this)) {
            if (SharedPrefsUtil.isNotificationLocationUpdatesEnabled(applicationContext)) {
                Logger.debug(TAG, "Service is running the foreground & notification updates are enabled. So we update the notification")
                NotificationUtil.showNotification(this, location)
            }
            FlutterBackgroundManager.sendLocation(applicationContext, location)
        } else {
            val intent = Intent(ACTION_BROADCAST)
            intent.putExtra(EXTRA_LOCATION, location)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        }
    }

    /**
     * Sets the location request parameters.
     */
    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.let {
            it.interval = UPDATE_INTERVAL_IN_MILLISECONDS
            it.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
            it.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        }
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
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000

        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value.
         */
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }
}