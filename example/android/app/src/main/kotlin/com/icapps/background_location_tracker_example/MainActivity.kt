package com.icapps.background_location_tracker_example

import android.os.Bundle
import com.icapps.background_location_tracker.BackgroundLocationTrackerPlugin
import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup background plugins for the location tracker
        BackgroundLocationTrackerPlugin.setBackgroundPluginRegistrar { engine ->
            // Register all plugins you need for background execution
            // Here are examples - replace with your actual plugins

            // For plugins that have direct registration methods:
            // YourPlugin.registerWith(engine.dartExecutor.binaryMessenger)
            // AnotherPlugin.registerWith(engine.dartExecutor.binaryMessenger)

            // Don't forget to add your location tracker plugin itself if needed
            engine.plugins.add(BackgroundLocationTrackerPlugin())
        }
    }
}
