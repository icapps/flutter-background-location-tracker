package com.icapps.background_location_tracker.utils

import android.content.Context

internal object SharedPrefsUtil {
    private const val SHARED_PREFS_FILE_NAME = "background_location_tracker"

    private const val KEY_CALBACK_HANDLER = "background.location.tracker.manager.CALLBACK_DISPATCHER_HANDLE_KEY"
    private const val KEY_IS_TRACKING = "background.location.tracker.manager.IS_TRACKING"
    private const val KEY_LOGGING_ENABED = "background.location.tracker.manager.LOGGIN_ENABLED"
    private const val KEY_TRACKING_INTERVAL = "background.location.tracker.manager.TRACKING_INTERVAL"
    private const val KEY_DISTANCE_FILTER = "background.location.tracker.manager.DISTANCE_FILTER"

    private const val KEY_NOTIFICATION_BODY = "background.location.tracker.manager.NOTIFICATION_BODY"
    private const val KEY_NOTIFICATION_ICON = "background.location.tracker.manager.NOTIFICATION_ICON"
    private const val KEY_NOTIFICATION_LOCATION_UPDATES_ENABLED = "background.location.tracker.manager.ENABLE_NOTIFICATION_LOCATION_UPDATES"
    private const val KEY_CANCEL_TRACKING_ACTION_TEXT = "background.location.tracker.manager.ENABLE_CANCEL_TRACKING_TEXT"
    private const val KEY_CANCEL_TRACKING_ACTION_ENABLED = "background.location.tracker.manager.ENABLE_CANCEL_TRACKING_ACTION"

    private fun Context.prefs() = getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)

    fun saveCallbackDispatcherHandleKey(ctx: Context, callbackHandle: Long) {
        ctx.prefs()
                .edit()
                .putLong(KEY_CALBACK_HANDLER, callbackHandle)
                .apply()
    }

    fun getCallbackHandle(ctx: Context): Long = ctx.prefs().getLong(KEY_CALBACK_HANDLER, -1L)

    fun hasCallbackHandle(ctx: Context) = ctx.prefs().contains(KEY_CALBACK_HANDLER)

    fun saveIsTracking(ctx: Context, isTracking: Boolean) {
        ctx.prefs()
                .edit()
                .putBoolean(KEY_IS_TRACKING, isTracking)
                .apply()
    }

    fun isTracking(ctx: Context): Boolean = ctx.prefs().getBoolean(KEY_IS_TRACKING, false)

    fun saveLoggingEnabled(ctx: Context, isTracking: Boolean) {
        ctx.prefs()
                .edit()
                .putBoolean(KEY_LOGGING_ENABED, isTracking)
                .apply()
    }

    fun saveTrackingInterval(ctx: Context, interval: Long) {
        ctx.prefs()
                .edit()
                .putLong(KEY_TRACKING_INTERVAL, interval)
                .apply()
    }

    fun saveDistanceFilter(ctx: Context, filter: Float) {
        ctx.prefs()
            .edit()
            .putFloat(KEY_DISTANCE_FILTER, filter)
            .apply()
    }

    fun isLoggingEnabled(ctx: Context): Boolean = ctx.prefs().getBoolean(KEY_LOGGING_ENABED, false)

    fun trackingInterval(ctx: Context): Long = ctx.prefs().getLong(KEY_TRACKING_INTERVAL, 10000)

    fun distanceFilter(ctx: Context) : Float = ctx.prefs().getFloat(KEY_DISTANCE_FILTER, 0.0f)

    //NotificationConfig
    fun saveNotificationConfig(ctx: Context, notificationBody: String, notificationIcon: String?, cancelTrackingActionText: String, enableNotificationLocationUpdates: Boolean, enableCancelTrackingAction: Boolean) {
        ctx.prefs()
                .edit()
                .putString(KEY_NOTIFICATION_BODY, notificationBody)
                .putString(KEY_NOTIFICATION_ICON, notificationIcon)
                .putString(KEY_CANCEL_TRACKING_ACTION_TEXT, cancelTrackingActionText)
                .putBoolean(KEY_NOTIFICATION_LOCATION_UPDATES_ENABLED, enableNotificationLocationUpdates)
                .putBoolean(KEY_CANCEL_TRACKING_ACTION_ENABLED, enableCancelTrackingAction)
                .apply()
    }

    fun getNotificationBody(ctx: Context): String = ctx.prefs().getString(KEY_NOTIFICATION_BODY, "Background tracking active. Tap to open.")!!

    fun getNotificationIcon(ctx: Context): String? = ctx.prefs().getString(KEY_NOTIFICATION_ICON, null)

    fun getCancelTrackingActionText(ctx: Context): String = ctx.prefs().getString(KEY_CANCEL_TRACKING_ACTION_TEXT, "Stop Tracking")!!

    fun isNotificationLocationUpdatesEnabled(ctx: Context): Boolean = ctx.prefs().getBoolean(KEY_NOTIFICATION_LOCATION_UPDATES_ENABLED, false)

    fun isCancelTrackingActionEnabled(ctx: Context): Boolean = ctx.prefs().getBoolean(KEY_CANCEL_TRACKING_ACTION_ENABLED, false)
}