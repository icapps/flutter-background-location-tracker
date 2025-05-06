package com.example.example

import io.flutter.app.FlutterApplication
import com.icapps.background_location_tracker.BackgroundLocationTrackerPlugin
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugins.GeneratedPluginRegistrant

// This Application class shows both approaches:
// 1. The deprecated approach using PluginRegistrantCallback (for backward compatibility)
// 2. Comments showing that this is not needed with the modern FlutterPluginBinding approach
class Application : FlutterApplication() {
    override fun onCreate() {
        super.onCreate()

        // Note: This is the deprecated approach and is only kept for backward compatibility
        // With modern Flutter plugins using the v2 embedding (FlutterPluginBinding),
        // this callback registration is not necessary
        BackgroundLocationTrackerPlugin.setPluginRegistrantCallback { engine ->
            // In a modern plugin using FlutterPluginBinding, plugins are automatically
            // registered through the FlutterEngine's plugin registry
            GeneratedPluginRegistrant.registerWith(engine)
        }
    }
} 