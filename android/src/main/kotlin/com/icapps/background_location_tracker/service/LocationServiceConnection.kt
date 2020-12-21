package com.icapps.background_location_tracker.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.location.Location
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.icapps.background_location_tracker.receiver.LocationReceiver

internal class LocationServiceConnection(private val listener: LocationUpdateListener) : ServiceConnection {
    var service: LocationUpdatesService? = null
    private var locationReceiver = MyLocationReceiver()
    private var bound = false

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        val localBinder = binder as LocationUpdatesService.LocalBinder
        service = localBinder.service
        bound = true
    }

    override fun onServiceDisconnected(name: ComponentName) {
        service = null
        bound = false
    }

    fun bound(ctx: Context) {
        if (!bound) {
            ctx.bindService(Intent(ctx, LocationUpdatesService::class.java), this, Context.BIND_AUTO_CREATE)
        }
    }

    fun onResume(ctx: Context) = LocalBroadcastManager.getInstance(ctx).registerReceiver(locationReceiver, IntentFilter(LocationUpdatesService.ACTION_BROADCAST))

    fun onPause(ctx: Context) = LocalBroadcastManager.getInstance(ctx).unregisterReceiver(locationReceiver)

    fun onStop(ctx: Context) {
        if (bound) {
            ctx.unbindService(this)
            bound = false
        }
    }

    inner class MyLocationReceiver : LocationReceiver() {
        override fun onLocationUpdate(location: Location) = listener.onLocationUpdate(location)
    }
}