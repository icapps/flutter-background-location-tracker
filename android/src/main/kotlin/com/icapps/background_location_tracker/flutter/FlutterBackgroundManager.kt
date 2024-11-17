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

    private var lastLocation: Location? = null

    private fun calculateBearing(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Float {
        val latitude1 = Math.toRadians(startLat)
        val latitude2 = Math.toRadians(endLat)
        val longDiff = Math.toRadians(endLng - startLng)
        val y = Math.sin(longDiff) * Math.cos(latitude2)
        val x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff)
        var bearing = Math.toDegrees(Math.atan2(y, x))
        if (bearing < 0) {
            bearing += 360
        }
        return bearing.toFloat()
    }

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
            if (callbackHandle == 0L) {
                Logger.debug("BackgroundManager", "Invalid callback handle: $callbackHandle")
                return@ensureInitializationCompleteAsync
            }

            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
            if (callbackInfo == null) {
                Logger.debug("BackgroundManager", "Failed to find callback information for handle: $callbackHandle")
                return@ensureInitializationCompleteAsync
            }

            val dartBundlePath = flutterLoader.findAppBundlePath()

            engine.dartExecutor.executeDartCallback(DartExecutor.DartCallback(ctx.assets, dartBundlePath, callbackInfo))
        }
    }

    private fun handleInitialized(call: MethodCall, result: MethodChannel.Result, ctx: Context, channel: MethodChannel, location: Location, engine: FlutterEngine) {
        val data = mutableMapOf<String, Any>()
        data["lat"] = location.latitude
        data["lon"] = location.longitude
        data["alt"] = if (location.hasAltitude()) location.altitude else 0.0
        
        // Vertical accuracy available in Android O and above
        data["vertical_accuracy"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasVerticalAccuracy()) {
            location.verticalAccuracyMeters.toDouble()
        } else {
            -1.0
        }
        
        data["horizontal_accuracy"] = if (location.hasAccuracy()) location.accuracy.toDouble() else -1.0
        
        // Enhanced bearing calculation without speed dependency
        data["course"] = when {
            location.hasBearing() -> location.bearing.toDouble()
            lastLocation != null -> {
                // Calculate bearing from previous location
                calculateBearing(
                    lastLocation!!.latitude, lastLocation!!.longitude,
                    location.latitude, location.longitude
                ).toDouble()
            }
            else -> -1.0
        }
        
        // Course accuracy available in Android O and above
        data["course_accuracy"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasBearingAccuracy()) {
            location.bearingAccuracyDegrees.toDouble()
        } else {
            -1.0
        }
        
        data["speed"] = if (location.hasSpeed()) location.speed.toDouble() else -1.0
        
        // Speed accuracy available in Android O and above
        data["speed_accuracy"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && location.hasSpeedAccuracy()) {
            location.speedAccuracyMetersPerSecond.toDouble()
        } else {
            -1.0
        }
        
        data["logging_enabled"] = SharedPrefsUtil.isLoggingEnabled(ctx)
        
        // Store last location for bearing calculations
        lastLocation = location

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
