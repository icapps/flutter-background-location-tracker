package com.icapps.background_location_tracker.flutter

import android.content.Context
import android.location.Location
import com.icapps.background_location_tracker.BackgroundLocationTrackerPlugin
import com.icapps.background_location_tracker.utils.SharedPrefsUtil
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.plugins.shim.ShimPluginRegistry
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain

object FlutterBackgroundManager {
    private const val BACKGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/background_channel"

    fun sendLocation(ctx: Context, location: Location) {
        val engine = FlutterEngine(ctx)
        FlutterMain.ensureInitializationComplete(ctx, null)

        val backgroundChannel = MethodChannel(engine.dartExecutor, BACKGROUND_CHANNEL_NAME)
        backgroundChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "initialized" -> handleInitialized(call, result, backgroundChannel, location)
                else -> result.notImplemented()
            }
        }

        val callbackHandle = SharedPrefsUtil.getCallbackHandle(ctx)
        val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
        val dartBundlePath = FlutterMain.findAppBundlePath()

        //Backwards compatibility with v1. We register all the user's plugins.
        BackgroundLocationTrackerPlugin.pluginRegistryCallback?.registerWith(ShimPluginRegistry(engine))
        engine.dartExecutor.executeDartCallback(DartExecutor.DartCallback(ctx.assets, dartBundlePath, callbackInfo))
    }

    private fun handleInitialized(call: MethodCall, result: MethodChannel.Result, channel: MethodChannel, location: Location) {
        val data = mutableMapOf<String, Any>()
        data["lat"] = location.latitude
        data["lon"] = location.longitude
        channel.invokeMethod("onLocationUpdate", data)
    }
}