package com.icapps.background_location_tracker.flutter

import android.content.Context
import android.location.Location
import com.icapps.background_location_tracker.BackgroundLocationTrackerPlugin
import com.icapps.background_location_tracker.utils.Logger
import com.icapps.background_location_tracker.utils.SharedPrefsUtil
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.plugins.shim.ShimPluginRegistry
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain

internal object FlutterBackgroundManager {
    private const val BACKGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/background_channel"

    private fun getInitializedFlutterEngine(ctx: Context): FlutterEngine {
        Logger.debug("BackgroundManager", "Creating new engine")

        val engine = FlutterEngine(ctx)
        FlutterMain.ensureInitializationComplete(ctx, null)
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

        val callbackHandle = SharedPrefsUtil.getCallbackHandle(ctx)
        val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
        val dartBundlePath = FlutterMain.findAppBundlePath()
        engine.dartExecutor.executeDartCallback(DartExecutor.DartCallback(ctx.assets, dartBundlePath, callbackInfo))
    }

    private fun handleInitialized(call: MethodCall, result: MethodChannel.Result, ctx: Context, channel: MethodChannel, location: Location, engine: FlutterEngine) {
        val data = mutableMapOf<String, Any>()
        data["lat"] = location.latitude
        data["lon"] = location.longitude
        data["accuracy"]= location.accuracy
        data["date"] = location.time.toString()
        data["logging_enabled"] = SharedPrefsUtil.isLoggingEnabled(ctx)
        channel.invokeMethod("onLocationUpdate", data, object : MethodChannel.Result {
            override fun success(result: Any?) {
                Logger.debug("BackgroundManager", "Got success, destroy engine!")
                engine.destroy()
            }

            override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
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