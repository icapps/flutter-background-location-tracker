package com.icapps.background_location_tracker.utils

import android.content.Context

object SharedPrefsUtil {
    private const val SHARED_PREFS_FILE_NAME = "background_location_tracker"

    private const val KEY_CALBACK_HANDLER = "background.location.tracker.manager.CALLBACK_DISPATCHER_HANDLE_KEY"
    private const val KEY_IS_TRACKING = "background.location.tracker.manager.IS_TRACKING"
    private const val KEY_LOGGING_ENABED = "background.location.tracker.manager.LOGGIN_ENABLED"

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

    fun isLoggingEnabled(ctx: Context): Boolean = ctx.prefs().getBoolean(KEY_LOGGING_ENABED, false)
}