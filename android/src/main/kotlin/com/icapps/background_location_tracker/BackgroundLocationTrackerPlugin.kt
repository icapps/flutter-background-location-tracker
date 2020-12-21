package com.icapps.background_location_tracker

import android.content.Context
import androidx.lifecycle.Lifecycle
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import com.icapps.background_location_tracker.flutter.FlutterLifecycleAdapter
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry


class BackgroundLocationTrackerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private var lifecycle: Lifecycle? = null
    private var methodCallHelper: MethodCallHelper? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) = registerBackgroundLocationManager(binding.binaryMessenger, binding.applicationContext)

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {}

    override fun onMethodCall(call: MethodCall, result: Result) {
        methodCallHelper?.handle(call, result)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding)
        if (methodCallHelper == null) {
            methodCallHelper = MethodCallHelper.getInstance(binding.activity.applicationContext)
        }
        methodCallHelper?.let {
            lifecycle?.removeObserver(it)
            lifecycle?.addObserver(it)
        }
    }

    override fun onDetachedFromActivity() {}

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}

    override fun onDetachedFromActivityForConfigChanges() {}

    companion object {
        var pluginRegistryCallback: PluginRegistry.PluginRegistrantCallback? = null

        @JvmStatic
        private fun registerBackgroundLocationManager(messenger: BinaryMessenger, ctx: Context) {
            val channel = MethodChannel(messenger, "com.icapps.background_location_tracker/foreground_channel")
            channel.setMethodCallHandler(BackgroundLocationTrackerPlugin().apply {
                if (methodCallHelper == null) {
                    methodCallHelper = MethodCallHelper.getInstance(ctx)
                }
                methodCallHelper?.let {
                    lifecycle?.removeObserver(it)
                    lifecycle?.addObserver(it)
                }
            })
        }

        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            registerBackgroundLocationManager(registrar.messenger(), registrar.activeContext())
        }

        @Deprecated(message = "Use the Android v2 embedding method.")
        @JvmStatic
        fun setPluginRegistrantCallback(pluginRegistryCallback: PluginRegistry.PluginRegistrantCallback) {
            BackgroundLocationTrackerPlugin.pluginRegistryCallback = pluginRegistryCallback
        }
    }
}
