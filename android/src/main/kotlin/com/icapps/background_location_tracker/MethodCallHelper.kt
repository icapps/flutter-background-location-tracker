package com.icapps.background_location_tracker

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.icapps.background_location_tracker.ext.checkRequiredFields
import com.icapps.background_location_tracker.flutter.FlutterBackgroundManager
import com.icapps.background_location_tracker.service.LocationServiceConnection
import com.icapps.background_location_tracker.service.LocationUpdateListener
import com.icapps.background_location_tracker.utils.NotificationUtil
import com.icapps.background_location_tracker.utils.SharedPrefsUtil
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MethodCallHelper(private val ctx: Context) : MethodChannel.MethodCallHandler, LifecycleObserver, LocationUpdateListener {

    private var serviceConnection = LocationServiceConnection(this)

    init {
        Log.i(TAG, "YET ANOTHER METHOD CALL HELPER $this")
    }

    fun handle(call: MethodCall, result: MethodChannel.Result) = when (call.method) {
        "initialize" -> initialize(ctx, call, result)
        "isTracking" -> isTracking(ctx, call, result)
        "startTracking" -> startTracking(ctx, call, result)
        "stopTracking" -> stopTracking(ctx, call, result)
        else -> result.error("404", "${call.method} is not supported", null)
    }

    private fun initialize(ctx: Context, call: MethodCall, result: MethodChannel.Result) {
        val callbackHandleKey = "callback_handle"
        val channelNameKey = "android_config_channel_name"
        val keys = listOf(callbackHandleKey, channelNameKey)
        if (!call.checkRequiredFields(keys, result)) return
        val callbackDispatcherHandleKey = call.argument<Long>(callbackHandleKey)!!
        val channelName = call.argument<String>(channelNameKey)!!
        NotificationUtil.createNotificationChannels(ctx, channelName)
        SharedPrefsUtil.saveCallbackDispatcherHandleKey(ctx, callbackDispatcherHandleKey)
        result.success(true)
    }

    private fun isTracking(ctx: Context, call: MethodCall, result: MethodChannel.Result) = result.success(SharedPrefsUtil.isTracking(ctx))

    private fun startTracking(ctx: Context, call: MethodCall, result: MethodChannel.Result) {
        serviceConnection.service?.startTracking()
        result.success(true)
    }

    private fun stopTracking(ctx: Context, call: MethodCall, result: MethodChannel.Result) {
        serviceConnection.service?.stopTracking()
        result.success(true)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {}

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        serviceConnection.bound(ctx)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        serviceConnection.onResume(ctx)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        serviceConnection.onPause(ctx)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        serviceConnection.onStop(ctx)
    }

    override fun onLocationUpdate(location: Location) = FlutterBackgroundManager.sendLocation(ctx, location)

    companion object {
        private val TAG = MethodCallHelper::class.java.simpleName

        private var sInstance: MethodCallHelper? = null

        fun getInstance(ctx: Context): MethodCallHelper? {
            if (sInstance == null) {
                sInstance = MethodCallHelper(ctx)
            }
            return sInstance
        }
    }
}
