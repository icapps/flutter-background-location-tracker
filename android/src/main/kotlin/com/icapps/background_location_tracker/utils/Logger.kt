package com.icapps.background_location_tracker.utils

import android.util.Log

object Logger {
    internal var enabled = false;

    fun debug(tag: String, s: String) {
        if (!enabled) return
        Log.d(tag, s)
    }

    fun error(tag: String, s: String) {
        Log.e(tag, s)
    }

    fun warning(tag: String, s: String) {
        if (!enabled) return
        Log.w(tag, s)
    }
}