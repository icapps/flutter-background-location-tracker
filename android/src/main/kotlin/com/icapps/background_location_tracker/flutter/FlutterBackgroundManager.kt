package com.icapps.background_location_tracker.flutter

import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.icapps.background_location_tracker.BackgroundLocationTrackerPlugin
import com.icapps.background_location_tracker.utils.Logger
import com.icapps.background_location_tracker.utils.SharedPrefsUtil
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.embedding.engine.plugins.shim.ShimPluginRegistry
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation

internal object FlutterBackgroundManager {
    private const val BACKGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/background_channel"

    private val flutterLoader = FlutterLoader()

    private fun getInitializedFlutterEngine(ctx: Context): FlutterEngine {
        Logger.debug("BackgroundManager", "Creating new engine")

        val engine = FlutterEngine(ctx)
        //Backwards compatibility with v1. We register all the user's plugins.
        BackgroundLocationTrackerPlugin.pluginRegistryCallback?.registerWith(ShimPluginRegistry(engine))
        return engine
    }

    fun sendLocation(ctx: Context, location: Location) {
        Logger.debug("BackgroundManager", "Location: ${location.latitude}: ${location.longitude}")
        val engine = getInitializedFlutterEngine(ctx)

        val backgroundChannel = MethodChannel(engine.dartExecutor, BACKGROUND_CHANNEL_NAME)
        backgroundChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                "initialized" -> handleInitialized(call, result, ctx, backgroundChannel, location, engine)
                else -> {
                    result.notImplemented()
                    engine.destroy()
                }
            }
        }

        if (!flutterLoader.initialized()) {
            flutterLoader.startInitialization(ctx)
        }
        flutterLoader.ensureInitializationCompleteAsync(ctx, null, Handler(Looper.getMainLooper())) {
            val callbackHandle = SharedPrefsUtil.getCallbackHandle(ctx)
            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
            val dartBundlePath = flutterLoader.findAppBundlePath()
            engine.dartExecutor.executeDartCallback(DartExecutor.DartCallback(ctx.assets, dartBundlePath, callbackInfo))
        }
    }

    private fun handleInitialized(call: MethodCall, result: MethodChannel.Result, ctx: Context, channel: MethodChannel, location: Location, engine: FlutterEngine) {
        val data = mutableMapOf<String, Any>()
        data["lat"] = location.latitude
        data["lon"] = location.longitude
        data["alt"] = if (location.hasAltitude()) location.altitude else 0.0
        data["vertical_accuracy"] = -1.0
        data["horizontal_accuracy"] = if (location.hasAccuracy()) location.accuracy else -1.0
        data["course"] = if (location.hasBearing()) location.bearing else -1.0
        data["course_accuracy"] = -1.0
        data["speed"] = if (location.hasSpeed()) location.speed else -1.0
        data["speed_accuracy"] = -1.0
        data["logging_enabled"] = SharedPrefsUtil.isLoggingEnabled(ctx)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data["vertical_accuracy"] = if (location.hasVerticalAccuracy()) location.verticalAccuracyMeters else -1.0
            data["course_accuracy"] = if (location.hasBearingAccuracy()) location.bearingAccuracyDegrees else -1.0
            data["speed_accuracy"] = if (location.hasSpeedAccuracy()) location.speedAccuracyMetersPerSecond else -1.0
        }

        channel.invokeMethod("onLocationUpdate", data, object : MethodChannel.Result {
            override fun success(result: Any?) {
                Logger.debug("BackgroundManager", "Got success, destroy engine!")
                engine.destroy()
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                Logger.debug("BackgroundManager", "Got error, destroy engine! $errorCode - $errorMessage : $errorDetails")
                engine.destroy()
            }

            override fun notImplemented() {
                Logger.debug("BackgroundManager", "Got not implemented, destroy engine!")
                engine.destroy()
            }
        })
    }
}