package com.icapps.background_location_tracker.utils

import android.content.Context

object SharedPrefsUtil {

    private const val SHARED_PREFS_FILE_NAME = "background_location_tracker"
    private const val CALLBACK_DISPATCHER_HANDLE_KEY = "com.icapps.background.location.tracker.manager.CALLBACK_DISPATCHER_HANDLE_KEY"
    private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates"
    private fun Context.prefs() = getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE)

    fun saveCallbackDispatcherHandleKey(ctx: Context, callbackHandle: Long) {
        ctx.prefs()
                .edit()
                .putLong(CALLBACK_DISPATCHER_HANDLE_KEY, callbackHandle)
                .apply()
    }

    fun getCallbackHandle(ctx: Context): Long = ctx.prefs().getLong(CALLBACK_DISPATCHER_HANDLE_KEY, -1L)

    fun hasCallbackHandle(ctx: Context) = ctx.prefs().contains(CALLBACK_DISPATCHER_HANDLE_KEY)

    fun saveIsTracking(ctx: Context, isTracking: Boolean) {
        ctx.prefs()
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, isTracking)
                .apply()
    }

    fun isTracking(ctx: Context): Boolean = ctx.prefs().getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
}