package com.icapps.background_location_tracker

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.icapps.background_location_tracker.flutter.FlutterLifecycleAdapter
import com.icapps.background_location_tracker.utils.ActivityCounter
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler

class BackgroundLocationTrackerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private var lifecycle: Lifecycle? = null
    private var methodCallHelper: MethodCallHelper? = null
    private var channel: MethodChannel? = null
    private lateinit var applicationContext: Context

    companion object {
        private const val FOREGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/foreground_channel"
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = binding.applicationContext
        channel = MethodChannel(binding.binaryMessenger, FOREGROUND_CHANNEL_NAME)
        channel?.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.setMethodCallHandler(null)
        channel = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding)
        ActivityCounter.attach(binding.activity)
        methodCallHelper = MethodCallHelper.getInstance(applicationContext)

        methodCallHelper?.let {
            lifecycle?.removeObserver(it)
            lifecycle?.addObserver(it)
        }
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        lifecycle?.let { 
            methodCallHelper?.let { helper ->
                it.removeObserver(helper)
            }
        }
    }

    override fun onDetachedFromActivity() {
        lifecycle?.let { 
            methodCallHelper?.let { helper ->
                it.removeObserver(helper)
            }
        }
        lifecycle = null
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        methodCallHelper?.handle(call, result)
    }
}
