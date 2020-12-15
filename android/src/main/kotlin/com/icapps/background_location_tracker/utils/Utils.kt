package com.icapps.background_location_tracker.utils

import android.content.Context
import android.location.Location
import android.preference.PreferenceManager
import java.text.DateFormat
import java.util.Date

internal object Utils {
    private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun requestingLocationUpdates(context: Context?): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun setRequestingLocationUpdates(context: Context?, requestingLocationUpdates: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply()
    }

    /**
     * Returns the `location` object as a human readable string.
     * @param location  The [Location].
     */
    fun getLocationText(location: Location?): String = if (location == null) "Unknown location" else "(" + location.latitude + ", " + location.longitude + ")"

    fun getLocationTitle(context: Context): String = String.format("Location Update: %s", DateFormat.getDateTimeInstance().format(Date()))
}