package com.icapps.background_location_tracker.ext

import android.app.NotificationManager
import android.content.Context

fun Context.getAppName(): String {
    val stringId = applicationInfo.labelRes
    if (stringId == 0) {
        return applicationInfo.nonLocalizedLabel.toString()
    }
    return getString(stringId)
}

fun Context.getAppIcon(): Int {
    val applicationInfo = applicationInfo
    return applicationInfo.icon
}

fun Context.notificationManager(): NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager