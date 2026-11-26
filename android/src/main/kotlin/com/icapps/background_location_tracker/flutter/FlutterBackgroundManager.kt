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
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation

internal object FlutterBackgroundManager {
    private const val BACKGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/background_channel"
    private const val MAX_PENDING_LOCATIONS = 32

    private val flutterLoader = FlutterLoader()
    private var backgroundChannel: MethodChannel? = null
    private var isInitialized = false
    private val pendingLocations = ArrayDeque<Location>()
    private var isSettingUp = false

    private fun getInitializedFlutterEngine(ctx: Context): FlutterEngine {
        Logger.debug("BackgroundManager", "Getting Flutter engine")
        return BackgroundLocationTrackerPlugin.getFlutterEngine(ctx)
    }

    fun sendLocation(ctx: Context, location: Location) {
        Logger.debug("BackgroundManager", "Location: ${location.latitude}: ${location.longitude}")
        
        if (isInitialized) {
            // Engine is already initialized, send location immediately
            sendLocationToChannel(ctx, location)
        } else {
            // Store the location and initialize if needed, dropping the oldest when the queue is full
            if (pendingLocations.size >= MAX_PENDING_LOCATIONS) {
                pendingLocations.removeFirst()
            }
            pendingLocations.addLast(location)
            setupBackgroundChannelIfNeeded(ctx)
        }
    }

    private fun setupBackgroundChannelIfNeeded(ctx: Context) {
        if (backgroundChannel != null || isSettingUp) {
            Logger.debug("BackgroundManager", "Setup already in progress or completed")
            return // Already setup or in progress
        }

        isSettingUp = true
        Logger.debug("BackgroundManager", "Setting up background channel and dart executor")
        val engine = getInitializedFlutterEngine(ctx)
        
        // Check if the DartExecutor is already running to prevent the error
        if (engine.dartExecutor.isExecutingDart) {
            Logger.debug("BackgroundManager", "DartExecutor is already running, reusing existing executor")
            backgroundChannel = MethodChannel(engine.dartExecutor, BACKGROUND_CHANNEL_NAME)
            backgroundChannel?.setMethodCallHandler { call, result ->
                when (call.method) {
                    "initialized" -> {
                        Logger.debug("BackgroundManager", "Dart background isolate already initialized")
                        isInitialized = true
                        isSettingUp = false
                        result.success(true)
                        
                        // Send the pending locations
                        flushPendingLocations(ctx)
                    }
                    else -> {
                        result.notImplemented()
                    }
                }
            }
            
            // Mark as initialized since the executor is already running
            isInitialized = true
            isSettingUp = false
            
            // Send the pending locations immediately
            flushPendingLocations(ctx)
            
            return
        }
        
        backgroundChannel = MethodChannel(engine.dartExecutor, BACKGROUND_CHANNEL_NAME)
        backgroundChannel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "initialized" -> {
                    Logger.debug("BackgroundManager", "Dart background isolate initialized")
                    isInitialized = true
                    isSettingUp = false
                    result.success(true)
                    
                    // Send the pending locations
                    flushPendingLocations(ctx)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }

        if (!flutterLoader.initialized()) {
            Logger.debug("BackgroundManager", "Initializing FlutterLoader")
            flutterLoader.startInitialization(ctx)
        }
        
        flutterLoader.ensureInitializationCompleteAsync(ctx, null, Handler(Looper.getMainLooper())) {
            Logger.debug("BackgroundManager", "FlutterLoader initialization complete, executing Dart callback")
            val callbackHandle = SharedPrefsUtil.getCallbackHandle(ctx)
            val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
            val dartBundlePath = flutterLoader.findAppBundlePath()
            
            try {
                engine.dartExecutor.executeDartCallback(DartExecutor.DartCallback(ctx.assets, dartBundlePath, callbackInfo))
            } catch (e: Exception) {
                Logger.debug("BackgroundManager", "Error executing Dart callback: ${e.message}")
                isSettingUp = false
            }
        }
    }

    private fun flushPendingLocations(ctx: Context) {
        Logger.debug("BackgroundManager", "Sending ${pendingLocations.size} pending locations")
        while (pendingLocations.isNotEmpty()) {
            sendLocationToChannel(ctx, pendingLocations.removeFirst())
        }
    }

    private fun sendLocationToChannel(ctx: Context, location: Location) {
        val channel = backgroundChannel ?: return
        Logger.debug("BackgroundManager", "Sending location to initialized channel")
        
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
        data["time"] = location.time
        data["logging_enabled"] = SharedPrefsUtil.isLoggingEnabled(ctx)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            data["vertical_accuracy"] = if (location.hasVerticalAccuracy()) location.verticalAccuracyMeters else -1.0
            data["course_accuracy"] = if (location.hasBearingAccuracy()) location.bearingAccuracyDegrees else -1.0
            data["speed_accuracy"] = if (location.hasSpeedAccuracy()) location.speedAccuracyMetersPerSecond else -1.0
        }

        channel.invokeMethod("onLocationUpdate", data, object : MethodChannel.Result {
            override fun success(result: Any?) {
                Logger.debug("BackgroundManager", "Successfully sent location update")
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                Logger.debug("BackgroundManager", "Error sending location update: $errorCode - $errorMessage : $errorDetails")
            }

            override fun notImplemented() {
                Logger.debug("BackgroundManager", "Method not implemented for location update")
            }
        })
    }
    
    fun cleanup() {
        Logger.debug("BackgroundManager", "Cleaning up background resources")
        isInitialized = false
        isSettingUp = false
        backgroundChannel?.setMethodCallHandler(null)
        backgroundChannel = null
        pendingLocations.clear()
        
        // Instead of destroying the engine completely, just mark it as not initialized
        // This allows for reuse without the "DartExecutor already running" error
        Logger.debug("BackgroundManager", "Background channel cleaned up, ready for reuse")
    }
    
    fun forceCleanup() {
        Logger.debug("BackgroundManager", "Force cleaning up all background resources")
        cleanup()
        
        // Clean up the Flutter engine completely to prevent DartExecutor reuse issues
        BackgroundLocationTrackerPlugin.cleanupFlutterEngine()
    }
}
