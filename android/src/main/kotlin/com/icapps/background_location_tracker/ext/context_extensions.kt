package com.icapps.background_location_tracker.ext

import android.content.Context

fun Context.getAppIcon(): Int {
    val applicationInfo = applicationInfo
    return applicationInfo.icon
}