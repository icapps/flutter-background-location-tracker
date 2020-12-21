package com.icapps.background_location_tracker.flutter

import androidx.lifecycle.Lifecycle
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference

internal object FlutterLifecycleAdapter {
    private const val TAG = "FlutterLifecycleAdapter"

    fun getActivityLifecycle(activityPluginBinding: ActivityPluginBinding): Lifecycle {
        val reference = activityPluginBinding.lifecycle as HiddenLifecycleReference
        return reference.lifecycle
    }
}