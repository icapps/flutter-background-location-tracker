package com.icapps.background_location_tracker.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.core.app.NotificationCompat
import com.icapps.background_location_tracker.ext.getAppIcon
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
    private const val NOTIFICATION_ID = 12345678

    /**
     * Android O requires a Notification Channel.
     * This will create a new notification channel for the foreground notification
     */
    fun createNotificationChannels(context: Context, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            getNotificationManager(context).createNotificationChannel(channel)
        }
    }

    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private fun getNotification(context: Context, location: Location?): Notification {
        val intent = Intent(context, LocationUpdatesService::class.java)
        val text = getLocationText(location)

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(LocationUpdatesService.EXTRA_STARTED_FROM_NOTIFICATION, true)

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        val servicePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // The PendingIntent to launch activity.
        val activityPendingIntent = PendingIntent.getActivity(context, 0, context.packageManager.getLaunchIntentForPackage(context.packageName), 0)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentText(text)
                .setContentTitle(getLocationTitle(context))
                .addAction(0, "Launch app",
                        activityPendingIntent)
                .addAction(0, "Remove location", servicePendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSmallIcon(context.getAppIcon())
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
        return builder.build()
    }

    fun showNotification(context: Context, location: Location?) {
        val notification = getNotification(context, location)
        getNotificationManager(context).notify(NOTIFICATION_ID, notification)
    }

    fun startForeground(service: LocationUpdatesService, location: Location?) {
        service.startForeground(NOTIFICATION_ID, getNotification(service, location))
    }

    private fun getNotificationManager(context: Context): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun getLocationText(location: Location?): String = if (location == null) "Unknown location" else "(" + location.latitude + ", " + location.longitude + ")"

    private fun getLocationTitle(context: Context): String = String.format("Location Update: %s", DateFormat.getDateTimeInstance().format(Date()))
}