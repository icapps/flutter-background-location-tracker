package com.icapps.background_location_tracker

import android.content.Context
import android.content.IntentFilter
import android.location.Location
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.icapps.background_location_tracker.service.LocationServiceConnection
import com.icapps.background_location_tracker.service.LocationUpdateListener
import com.icapps.background_location_tracker.service.LocationUpdatesService
import com.icapps.background_location_tracker.utils.SharedPrefsUtil
import com.icapps.background_location_tracker.utils.Utils
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.plugins.shim.ShimPluginRegistry
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain

class MethodCallHelper(private val ctx: Context) : MethodChannel.MethodCallHandler, LifecycleObserver, LocationUpdateListener {
    private lateinit var backgroundChannel: MethodChannel

    private var serviceConnection = LocationServiceConnection(this)

    fun handle(call: MethodCall, result: MethodChannel.Result) = when (call.method) {
        "initialize" -> initialize(ctx, call, result)
        "startTracking" -> startTracking(ctx, call, result)
        "stopTracking" -> stopTracking(ctx, call, result)
        else -> result.error("404", "${call.method} is not supported", null)
    }

    private fun initialize(ctx: Context, call: MethodCall, result: MethodChannel.Result) {
        val callbackHandleKey = "callbackHandle"
        if (!call.hasArgument(callbackHandleKey)) {
            result.error("404", "$callbackHandleKey not found, but required", null)
            return;
        }
        val callbackDispatcherHandleKey = call.argument<Long>("callbackHandle")!!
        SharedPrefsUtil.saveCallbackDispatcherHandleKey(ctx, callbackDispatcherHandleKey)
        serviceConnection.bound(ctx)
        result.success(true)
    }

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
        Log.i(TAG, "onStart")
        serviceConnection.bound(ctx)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Log.i(TAG, "onResume")
        serviceConnection.onResume(ctx)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Log.i(TAG, "onPause")
        serviceConnection.onPause(ctx)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Log.i(TAG, "onStop")
        serviceConnection.onStop(ctx)
    }

    override fun onLocationUpdate(location: Location) = sendBackgroundLocation(location)

    private fun sendBackgroundLocation(location: Location) {
        val text = "New location data => Send to dart: ${Utils.getLocationText(location)}"
        Log.i(TAG, text)
        val engine = FlutterEngine(ctx)
        FlutterMain.ensureInitializationComplete(ctx, null)

        val callbackHandle = SharedPrefsUtil.getCallbackHandle(ctx)
        val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
        val dartBundlePath = FlutterMain.findAppBundlePath()

        //Backwards compatibility with v1. We register all the user's plugins.
        BackgroundLocationTrackerPlugin.pluginRegistryCallback?.registerWith(ShimPluginRegistry(engine))
        engine.dartExecutor.executeDartCallback(DartExecutor.DartCallback(ctx.assets, dartBundlePath, callbackInfo))

        backgroundChannel = MethodChannel(engine.dartExecutor, BACKGROUND_CHANNEL_NAME)
        backgroundChannel.setMethodCallHandler(this)
        val data = mutableMapOf<String, Any>()
        data["lat"] = location.latitude
        data["lon"] = location.longitude
        backgroundChannel.invokeMethod("onLocationUpdate", data)
    }

    companion object {
        const val BACKGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/background_channel"
        private val TAG = MethodCallHelper::class.java.simpleName
    }
}
