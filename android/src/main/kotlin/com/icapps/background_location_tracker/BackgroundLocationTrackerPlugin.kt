package com.icapps.background_location_tracker

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.icapps.background_location_tracker.utils.ActivityCounter
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.shim.ShimPluginRegistry
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

interface PluginRegistryCallback {
    fun registerWith(registry: ShimPluginRegistry)
}

class BackgroundLocationTrackerPlugin : FlutterPlugin, MethodChannel.MethodCallHandler,
    ActivityAware, Application.ActivityLifecycleCallbacks, LifecycleOwner {
    override lateinit var lifecycle: LifecycleRegistry
    private var methodCallHelper: MethodCallHelper? = null
    private var activity: Activity? = null

    companion object {
        var pluginRegistryCallback: PluginRegistryCallback? = null
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        lifecycle = LifecycleRegistry(this)
        val channel = MethodChannel(binding.binaryMessenger, FOREGROUND_CHANNEL_NAME)
        channel.setMethodCallHandler(this).apply {
            methodCallHelper = MethodCallHelper.getInstance(binding.applicationContext)
            methodCallHelper?.let {
                lifecycle.addObserver(it)
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        methodCallHelper?.let {
            lifecycle.removeObserver(it)
        }
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        methodCallHelper?.handle(call, result)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        activity?.application?.registerActivityLifecycleCallbacks(this)

        if (methodCallHelper == null) {
            ActivityCounter.attach(binding.activity)
            methodCallHelper = MethodCallHelper.getInstance(binding.activity.applicationContext)
        }

        methodCallHelper?.let {
            lifecycle.removeObserver(it)
            lifecycle.addObserver(it)
        }
    }

    override fun onDetachedFromActivity() {
        activity?.application?.unregisterActivityLifecycleCallbacks(this)
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity.hashCode() != this.activity?.hashCode()) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity.hashCode() != this.activity?.hashCode()) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity.hashCode() != this.activity?.hashCode()) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity.hashCode() != this.activity?.hashCode()) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity.hashCode() != this.activity?.hashCode()) {
            return
        }
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (activity.hashCode() != this.activity?.hashCode()) {
            return
        }
        activity.application.unregisterActivityLifecycleCallbacks(this)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    private val FOREGROUND_CHANNEL_NAME =
        "com.icapps.background_location_tracker/foreground_channel"

}