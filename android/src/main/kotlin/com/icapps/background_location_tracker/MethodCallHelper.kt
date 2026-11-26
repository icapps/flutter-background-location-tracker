package com.icapps.background_location_tracker

import android.content.Context
import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.icapps.background_location_tracker.ext.checkRequiredFields
import com.icapps.background_location_tracker.flutter.FlutterBackgroundManager
import com.icapps.background_location_tracker.service.LocationServiceConnection
import com.icapps.background_location_tracker.service.LocationUpdateListener
import com.icapps.background_location_tracker.service.LocationUpdatesService
import com.icapps.background_location_tracker.utils.Logger
import com.icapps.background_location_tracker.utils.NotificationUtil
import com.icapps.background_location_tracker.utils.SharedPrefsUtil
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.Collections
import java.util.WeakHashMap

internal class MethodCallHelper(private val ctx: Context) : MethodChannel.MethodCallHandler, LifecycleObserver, LocationUpdateListener {

    private var serviceConnection = LocationServiceConnection(this)

    /**
     * When navigating between activities, the next one is started before the previous one is stopped.
     */
    private val startedOwners: MutableSet<LifecycleOwner> = Collections.newSetFromMap(WeakHashMap())

    /**
     * The service connection only has a service once an activity is bound, and can refer to a
     * destroyed service after an activity is unbound.
     */
    private val service: LocationUpdatesService?
        get() = LocationUpdatesService.runningService ?: serviceConnection.service

    fun handle(call: MethodCall, result: MethodChannel.Result) = when (call.method) {
        "initialize" -> initialize(ctx, call, result)
        "isTracking" -> isTracking(ctx, call, result)
        "startTracking" -> startTracking(ctx, call, result)
        "stopTracking" -> stopTracking(ctx, call, result)
        "updateTrackingInterval" -> updateTrackingInterval(ctx, call, result)
        else -> result.error("404", "${call.method} is not supported", null)
    }

    private fun initialize(ctx: Context, call: MethodCall, result: MethodChannel.Result) {
        val callbackHandleKey = "callback_handle"
        val loggingEnabledKey = "logging_enabled"
        val trackingIntervalKey = "android_update_interval_msec"
        val channelNameKey = "android_config_channel_name"
        val notificationBodyKey = "android_config_notification_body"
        val notificationIconKey = "android_config_notification_icon"
        val enableNotificationLocationUpdatesKey = "android_config_enable_notification_location_updates"
        val enableCancelTrackingActionKey = "android_config_enable_cancel_tracking_action"
        val cancelTrackingActionTextKey = "android_config_cancel_tracking_action_text"
        val distanceFilterKey = "android_distance_filter"
        val keys = listOf(
                callbackHandleKey,
                loggingEnabledKey,
                channelNameKey,
                notificationBodyKey,
                enableNotificationLocationUpdatesKey,
                cancelTrackingActionTextKey,
                enableCancelTrackingActionKey,
                trackingIntervalKey
        )
        if (!call.checkRequiredFields(keys, result)) return
        val callbackHandle = getLongArgumentByKey(call, callbackHandleKey)!!
        val loggingEnabled = call.argument<Boolean>(loggingEnabledKey)!!
        val channelName = call.argument<String>(channelNameKey)!!
        val notificationBody = call.argument<String>(notificationBodyKey)!!
        val notificationIcon = call.argument<String>(notificationIconKey)
        val enableNotificationLocationUpdates = call.argument<Boolean>(enableNotificationLocationUpdatesKey)!!
        val cancelTrackingActionText = call.argument<String>(cancelTrackingActionTextKey)!!
        val enableCancelTrackingAction = call.argument<Boolean>(enableCancelTrackingActionKey)!!
        val trackingInterval = getLongArgumentByKey(call, trackingIntervalKey)!!
        val distanceFilter = (call.argument<Double>(distanceFilterKey) ?: 0.0).toFloat()
        SharedPrefsUtil.saveLoggingEnabled(ctx, loggingEnabled)
        SharedPrefsUtil.saveTrackingInterval(ctx, trackingInterval)
        SharedPrefsUtil.saveDistanceFilter(ctx, distanceFilter)
        Logger.enabled = loggingEnabled
        NotificationUtil.createNotificationChannels(ctx, channelName)
        SharedPrefsUtil.saveCallbackDispatcherHandleKey(ctx, callbackHandle)
        SharedPrefsUtil.saveNotificationConfig(ctx, notificationBody, notificationIcon, cancelTrackingActionText, enableNotificationLocationUpdates, enableCancelTrackingAction)
        if (SharedPrefsUtil.isTracking(ctx)) {
            LocationUpdatesService.runningService?.applySavedConfig()
        }
        result.success(true)
    }

    private fun getLongArgumentByKey(call: MethodCall, key: String): Long? {
        return try {
            call.argument<Number>(key)!!.toLong()
        } catch (exception: ClassCastException) {
            call.argument(key)
        }
    }

    private fun isTracking(ctx: Context, call: MethodCall, result: MethodChannel.Result) = result.success(SharedPrefsUtil.isTracking(ctx))

    private fun startTracking(ctx: Context, call: MethodCall, result: MethodChannel.Result) {
        val notificationBodyKey = "android_config_notification_body"
        val notificationIconKey = "android_config_notification_icon"
        val enableNotificationLocationUpdatesKey = "android_config_enable_notification_location_updates"
        val enableCancelTrackingActionKey = "android_config_enable_cancel_tracking_action"
        val cancelTrackingActionTextKey = "android_config_cancel_tracking_action_text"

        val notificationBody = call.argument<String>(notificationBodyKey)
        val notificationIcon = call.argument<String>(notificationIconKey)
        val enableNotificationLocationUpdates = call.argument<Boolean>(enableNotificationLocationUpdatesKey)
        val cancelTrackingActionText = call.argument<String>(cancelTrackingActionTextKey)
        val enableCancelTrackingAction = call.argument<Boolean>(enableCancelTrackingActionKey)
        if (notificationBody != null || notificationIcon != null || cancelTrackingActionText != null
                || enableNotificationLocationUpdates != null || enableCancelTrackingAction != null) {
            SharedPrefsUtil.saveNotificationConfig(ctx, notificationBody ?: SharedPrefsUtil.getNotificationBody(ctx),
                                                   notificationIcon ?: SharedPrefsUtil.getNotificationIcon(ctx),
                                                   cancelTrackingActionText ?: SharedPrefsUtil.getCancelTrackingActionText(ctx),
                                                   enableNotificationLocationUpdates ?: SharedPrefsUtil.isNotificationLocationUpdatesEnabled(ctx),
                                                   enableCancelTrackingAction ?: SharedPrefsUtil.isCancelTrackingActionEnabled(ctx))
        }
        service?.startTracking()
        result.success(true)
    }

    private fun stopTracking(ctx: Context, call: MethodCall, result: MethodChannel.Result) {
        service?.stopTracking()
        FlutterBackgroundManager.cleanup()
        result.success(true)
    }

    private fun updateTrackingInterval(ctx: Context, call: MethodCall, result: MethodChannel.Result) {
        val trackingIntervalKey = "android_update_interval_msec"
        if (!call.checkRequiredFields(listOf(trackingIntervalKey), result)) return
        val trackingInterval = getLongArgumentByKey(call, trackingIntervalKey)!!
        if (trackingInterval <= 0) {
            result.error("invalid_interval", "The tracking interval should be at least 1 millisecond, but was $trackingInterval", null)
            return
        }

        val runningService = LocationUpdatesService.runningService
        if (runningService == null || !SharedPrefsUtil.isTracking(ctx)) {
            // Will be used when tracking (re)starts
            SharedPrefsUtil.saveTrackingInterval(ctx, trackingInterval)
            result.success(true)
            return
        }
        runningService.updateTrackingInterval(trackingInterval) { error ->
            when (error) {
                null -> result.success(true)
                is SecurityException -> result.error("permission_denied", "Lost location permission. Could not update the tracking interval", error.message)
                else -> result.error("update_failed", "Could not update the tracking interval", error.message)
            }
        }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {}

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart(owner: LifecycleOwner) {
        if (startedOwners.isEmpty()) {
            serviceConnection.registerReceiver(ctx)
        }
        startedOwners.add(owner)
        serviceConnection.bound(ctx)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop(owner: LifecycleOwner) {
        startedOwners.remove(owner)
        if (startedOwners.isEmpty()) {
            serviceConnection.unregisterReceiver(ctx)
        }
        serviceConnection.onStop(ctx)
    }

    override fun onLocationUpdate(location: Location) = FlutterBackgroundManager.sendLocation(ctx, location)

    companion object {
        private val TAG = MethodCallHelper::class.java.simpleName

        private var instance: MethodCallHelper? = null

        fun getInstance(ctx: Context): MethodCallHelper? {
            if (instance == null) {
                instance = MethodCallHelper(ctx)
            }
            return instance
        }
    }
}
