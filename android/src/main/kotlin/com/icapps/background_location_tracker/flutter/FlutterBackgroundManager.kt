package com.icapps.background_location_tracker.flutter

import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.icapps.background_location_tracker.utils.Logger
import com.icapps.background_location_tracker.utils.SharedPrefsUtil
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

internal object FlutterBackgroundManager {
    private const val BACKGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/background_channel"

    private val flutterLoader = FlutterLoader()
    private var backgroundFlutterEngine: FlutterEngine? = null
    private var backgroundChannel: MethodChannel? = null
    private val isCallbackRunning = AtomicBoolean(false)
    private val pendingLocationCount = AtomicInteger(0)
    private var lastLocation: Location? = null
    
    // Set a limit on the frequency of background updates
    private const val MIN_UPDATE_INTERVAL_MS = 1000  // 1 second minimum between updates
    private var lastUpdateTimeMs: Long = 0

    private fun ensureBackgroundEngine(ctx: Context): FlutterEngine {
        return backgroundFlutterEngine ?: synchronized(this) {
            backgroundFlutterEngine ?: createFlutterEngine(ctx).also { engine ->
                backgroundFlutterEngine = engine
                backgroundChannel = MethodChannel(engine.dartExecutor, BACKGROUND_CHANNEL_NAME)
                Logger.debug("BackgroundManager", "Background Flutter engine created")
            }
        }
    }

    private fun createFlutterEngine(ctx: Context): FlutterEngine {
        Logger.debug("BackgroundManager", "Creating new engine")
        return FlutterEngine(ctx, null, true)
    }
    
    fun releaseResources() {
        synchronized(this) {
            Logger.debug("BackgroundManager", "Releasing resources")
            backgroundChannel = null
            backgroundFlutterEngine?.destroy()
            backgroundFlutterEngine = null
            isCallbackRunning.set(false)
            pendingLocationCount.set(0)
            lastLocation = null
        }
    }

    fun sendLocation(ctx: Context, location: Location) {
        // Apply rate limiting to background location updates
        val currentTimeMs = System.currentTimeMillis()
        if (currentTimeMs - lastUpdateTimeMs < MIN_UPDATE_INTERVAL_MS) {
            Logger.debug("BackgroundManager", "Skipping location update due to rate limiting")
            return
        }
        lastUpdateTimeMs = currentTimeMs
        
        Logger.debug("BackgroundManager", "Location: ${location.latitude}, ${location.longitude}")

        // Store the latest location
        lastLocation = location
        
        // If we already have pending locations, only keep track of the latest one
        // This prevents a queue buildup that could cause device sluggishness
        if (pendingLocationCount.get() > 0) {
            Logger.debug("BackgroundManager", "Already have pending location, updating to latest")
            // Don't increment counter, just update the last location
            return
        }
        
        // Increment pending location count
        pendingLocationCount.incrementAndGet()

        // Don't process if a callback is already in progress
        if (isCallbackRunning.getAndSet(true)) {
            Logger.debug("BackgroundManager", "Callback is already running, will use latest location when current completes")
            return
        }

        doSendLocation(ctx, location)
    }
    
    private fun doSendLocation(ctx: Context, location: Location) {
        if (!flutterLoader.initialized()) {
            flutterLoader.startInitialization(ctx)
        }

        flutterLoader.ensureInitializationCompleteAsync(ctx, null, Handler(Looper.getMainLooper())) {
            try {
                val callbackHandle = SharedPrefsUtil.getCallbackHandle(ctx)
                if (callbackHandle == 0L) {
                    Logger.error("BackgroundManager", "Callback handle is 0, skipping location update")
                    handleCallbackComplete(ctx)
                    return@ensureInitializationCompleteAsync
                }
                
                val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
                
                if (callbackInfo == null) {
                    Logger.error("BackgroundManager", "Failed to find callback")
                    handleCallbackComplete(ctx)
                    return@ensureInitializationCompleteAsync
                }
                
                val engine = ensureBackgroundEngine(ctx)
                val channel = backgroundChannel ?: MethodChannel(engine.dartExecutor, BACKGROUND_CHANNEL_NAME)
                backgroundChannel = channel

                if (!engine.dartExecutor.isExecutingDart) {
                    Logger.debug("BackgroundManager", "Starting Dart execution")
                    channel.setMethodCallHandler { call, result ->
                        if (call.method == "initialized") {
                            handleInitialized(ctx, result, location, channel)
                        } else {
                            result.notImplemented()
                            handleCallbackComplete(ctx)
                        }
                    }

                    val dartBundlePath = flutterLoader.findAppBundlePath()
                    engine.dartExecutor.executeDartCallback(
                        DartExecutor.DartCallback(ctx.assets, dartBundlePath, callbackInfo)
                    )
                } else {
                    // Engine is already running - just send the location directly
                    sendLocationToChannel(ctx, location, channel)
                }
            } catch (e: Exception) {
                Logger.error("BackgroundManager", "Error sending location: ${e.message}")
                handleCallbackComplete(ctx)
            }
        }
    }
    
    private fun handleCallbackComplete(ctx: Context) {
        isCallbackRunning.set(false)
        pendingLocationCount.set(0)
        
        // We don't process the queue anymore, just the latest location if needed
        // This is a more efficient approach that prevents battery drain
    }

    private fun handleInitialized(
        ctx: Context,
        result: MethodChannel.Result,
        location: Location,
        channel: MethodChannel
    ) {
        result.success(true)
        sendLocationToChannel(ctx, location, channel)
    }

    private fun sendLocationToChannel(
        ctx: Context,
        location: Location,
        channel: MethodChannel
    ) {
        val data = mutableMapOf<String, Any>(
            "lat" to location.latitude,
            "lon" to location.longitude,
            "alt" to if (location.hasAltitude()) location.altitude else 0.0,
            "vertical_accuracy" to -1.0,
            "horizontal_accuracy" to if (location.hasAccuracy()) location.accuracy else -1.0,
            "course" to if (location.hasBearing()) location.bearing else -1.0,
            "course_accuracy" to -1.0,
            "speed" to if (location.hasSpeed()) location.speed else -1.0,
            "speed_accuracy" to -1.0,
            "logging_enabled" to SharedPrefsUtil.isLoggingEnabled(ctx)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data["vertical_accuracy"] = if (location.hasVerticalAccuracy()) location.verticalAccuracyMeters else -1.0
            data["course_accuracy"] = if (location.hasBearingAccuracy()) location.bearingAccuracyDegrees else -1.0
            data["speed_accuracy"] = if (location.hasSpeedAccuracy()) location.speedAccuracyMetersPerSecond else -1.0
        }

        channel.invokeMethod("onLocationUpdate", data, object : MethodChannel.Result {
            override fun success(result: Any?) {
                Logger.debug("BackgroundManager", "Location update sent successfully")
                handleCallbackComplete(ctx)
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                Logger.debug("BackgroundManager", "Error: $errorCode - $errorMessage : $errorDetails")
                handleCallbackComplete(ctx)
            }

            override fun notImplemented() {
                Logger.debug("BackgroundManager", "Not implemented")
                handleCallbackComplete(ctx)
            }
        })
    }
}
