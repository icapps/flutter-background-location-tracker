package com.icapps.background_location_tracker.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import androidx.core.app.NotificationCompat
import com.icapps.background_location_tracker.ext.getAppIcon
import com.icapps.background_location_tracker.ext.getAppName
import com.icapps.background_location_tracker.ext.notificationManager
import com.icapps.background_location_tracker.service.LocationUpdatesService
import java.text.DateFormat
import java.util.Date

internal object NotificationUtil {

    /**
     * The name of the channel for notifications.
     */
    private const val CHANNEL_ID = "com_icapps_background_tracking_notification_channel"

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private const val NOTIFICATION_ID = 879848645

    /**
     * Android O requires a Notification Channel.
     * This will create a new notification channel for the foreground notification
     */
    fun createNotificationChannels(context: Context, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW)
            channel.enableVibration(false)
            channel.setSound(null, null)
            context.notificationManager().createNotificationChannel(channel)
        }
    }

    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private fun getNotification(context: Context, location: Location?): Notification {
        val intent = Intent(context, LocationUpdatesService::class.java)
        intent.putExtra(LocationUpdatesService.EXTRA_STARTED_FROM_NOTIFICATION, true)
        val cancelTrackingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            @Suppress("UnspecifiedImmutableFlag")
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val clickPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(context, 0, context.packageManager.getLaunchIntentForPackage(context.packageName), PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(context, 0, context.packageManager.getLaunchIntentForPackage(context.packageName), 0)
        }

        val title = if (SharedPrefsUtil.isNotificationLocationUpdatesEnabled(context)) {
            String.format("Location Update: %s", DateFormat.getDateTimeInstance().format(Date()))
        } else {
            context.getAppName()
        }

        val text = if (SharedPrefsUtil.isNotificationLocationUpdatesEnabled(context)) {
            if (location == null) "Unknown location" else "(" + location.latitude + ", " + location.longitude + ")"
        } else {
            SharedPrefsUtil.getNotificationBody(context)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(clickPendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        if (SharedPrefsUtil.isCancelTrackingActionEnabled(context)) {
            builder.addAction(0, SharedPrefsUtil.getCancelTrackingActionText(context), cancelTrackingIntent)
        }
        val savedIconName = SharedPrefsUtil.getNotificationIcon(context);
        val icon = if (savedIconName == null) {
            context.getAppIcon()
        } else {
            context.resources.getIdentifier(savedIconName, "drawable", context.packageName)
        }

        builder.setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(icon)
                .setTicker(text)
                .setVibrate(null)
                .setDefaults(0)
                .setSound(null)
                .setWhen(System.currentTimeMillis())
        return builder.build()
    }

    fun showNotification(context: Context, location: Location?) {
        val notification = getNotification(context, location)
        context.notificationManager().notify(NOTIFICATION_ID, notification)
    }

    fun startForeground(service: LocationUpdatesService, location: Location?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            service.startForeground(NOTIFICATION_ID, getNotification(service, location), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            service.startForeground(NOTIFICATION_ID, getNotification(service, location))
        }
    }
}