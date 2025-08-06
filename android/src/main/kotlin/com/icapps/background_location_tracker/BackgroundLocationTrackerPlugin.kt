package com.icapps.background_location_tracker

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.icapps.background_location_tracker.flutter.FlutterBackgroundManager
import com.icapps.background_location_tracker.flutter.FlutterLifecycleAdapter
import com.icapps.background_location_tracker.utils.ActivityCounter
import com.icapps.background_location_tracker.utils.Logger
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * BackgroundLocationTrackerPlugin handles location tracking in both foreground and background modes.
 * This plugin manages Flutter engine lifecycle for background execution and provides location updates.
 */
class BackgroundLocationTrackerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    
    private var lifecycle: Lifecycle? = null
    private var methodCallHelper: MethodCallHelper? = null
    private var channel: MethodChannel? = null
    private var applicationContext: Context? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        Logger.debug(TAG, "Plugin attached to engine")
        
        applicationContext = binding.applicationContext
        channel = MethodChannel(binding.binaryMessenger, FOREGROUND_CHANNEL_NAME).also {
            it.setMethodCallHandler(this)
        }
        
        if (methodCallHelper == null) {
            methodCallHelper = MethodCallHelper.getInstance(binding.applicationContext)
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        Logger.debug(TAG, "Plugin detached from engine")
        
        // Clean up lifecycle observers first
        lifecycle?.let { lifecycle ->
            methodCallHelper?.let { helper ->
                lifecycle.removeObserver(helper)
            }
        }
        
        // Clean up method call helper
        methodCallHelper?.cleanup()
        methodCallHelper = null
        
        // Clean up channel
        channel?.setMethodCallHandler(null)
        channel = null
        
        // Clean up context references
        applicationContext = null
        lifecycle = null
        
        // Clean up background manager
        FlutterBackgroundManager.cleanup()
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        methodCallHelper?.handle(call, result) ?: run {
            Logger.debug(TAG, "MethodCallHelper is null, method call not handled")
            result.notImplemented()
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Logger.debug(TAG, "Plugin attached to activity")
        
        lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding)
        ActivityCounter.attach(binding.activity)
        
        methodCallHelper?.let { helper ->
            lifecycle?.let { lifecycle ->
                lifecycle.removeObserver(helper)
                lifecycle.addObserver(helper)
            }
        }
    }

    override fun onDetachedFromActivity() {
        Logger.debug(TAG, "Plugin detached from activity")
        
        // Clean up lifecycle observers
        lifecycle?.let { lifecycle ->
            methodCallHelper?.let { helper ->
                lifecycle.removeObserver(helper)
            }
        }
        lifecycle = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Logger.debug(TAG, "Plugin reattached to activity for config changes")
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Logger.debug(TAG, "Plugin detached from activity for config changes")
        // Don't clean up lifecycle here as it's just a config change
    }

    companion object {
        private const val TAG = "FBLTPlugin"
        private const val FOREGROUND_CHANNEL_NAME = "com.icapps.background_location_tracker/foreground_channel"
        private const val CACHED_ENGINE_ID = "background_location_tracker_engine"

        // Thread-safe engine management
        @Volatile
        private var flutterEngine: FlutterEngine? = null
        @Volatile
        private var isEngineInitialized = false
        private val engineLock = ReentrantReadWriteLock()

        /**
         * Gets or creates a Flutter engine for background execution.
         * Uses FlutterEngineCache for proper lifecycle management.
         *
         * @param context Application context
         * @return FlutterEngine instance for background execution
         */
        @JvmStatic
        fun getFlutterEngine(context: Context): FlutterEngine {
            // First try to read with read lock
            engineLock.read {
                flutterEngine?.let { 
                    Logger.debug(TAG, "Reusing existing Flutter engine")
                    return it 
                }
            }
            
            // Need to create engine, use write lock
            return engineLock.write {
                // Double-check pattern
                flutterEngine?.let { 
                    Logger.debug(TAG, "Reusing existing Flutter engine (double-check)")
                    return@write it 
                }
                
                Logger.debug(TAG, "Creating new Flutter engine for background execution")
                
                // Try to get from cache first
                val cache = FlutterEngineCache.getInstance()
                var engine = cache.get(CACHED_ENGINE_ID)
                
                if (engine == null) {
                    // Create new engine and cache it
                    engine = FlutterEngine(context.applicationContext).also { newEngine ->
                        cache.put(CACHED_ENGINE_ID, newEngine)
                        Logger.debug(TAG, "Flutter engine created and cached")
                    }
                } else {
                    Logger.debug(TAG, "Retrieved Flutter engine from cache")
                }
                
                flutterEngine = engine
                isEngineInitialized = true
                
                engine
            }
        }
        
        /**
         * Safely cleans up the Flutter engine and removes it from cache.
         * This method is thread-safe and handles cleanup gracefully.
         */
        @JvmStatic
        fun cleanupFlutterEngine() {
            engineLock.write {
                try {
                    Logger.debug(TAG, "Starting Flutter engine cleanup")
                    
                    // Remove from cache first
                    val cache = FlutterEngineCache.getInstance()
                    cache.remove(CACHED_ENGINE_ID)
                    
                    flutterEngine?.let { engine ->
                        try {
                            // Graceful shutdown
                            if (engine.dartExecutor.isExecutingDart) {
                                Logger.debug(TAG, "Dart is executing, destroying engine gracefully")
                            }
                            
                            // Destroy the engine (this handles Dart isolate cleanup internally)
                            engine.destroy()
                            Logger.debug(TAG, "Flutter engine destroyed successfully")
                            
                        } catch (e: Exception) {
                            Logger.error(TAG, "Error during Flutter engine cleanup: ${e.message}")
                            // Re-throw critical exceptions, but handle them gracefully
                            when (e) {
                                is IllegalStateException,
                                is SecurityException -> {
                                    Logger.error(TAG, "Critical error during engine cleanup: ${e.message}")
                                    // Don't re-throw to avoid crashing the app
                                }
                                else -> {
                                    Logger.debug(TAG, "Non-critical error during cleanup, continuing: ${e.message}")
                                }
                            }
                        }
                    }
                    
                } finally {
                    // Always reset state
                    flutterEngine = null
                    isEngineInitialized = false
                    Logger.debug(TAG, "Flutter engine cleanup completed")
                }
            }
        }
        
        /**
         * Checks if the Flutter engine is currently initialized and available.
         *
         * @return true if engine is initialized, false otherwise
         */
        @JvmStatic
        fun isEngineInitialized(): Boolean {
            return engineLock.read { isEngineInitialized && flutterEngine != null }
        }
        
        /**
         * Forces cleanup of all static references. Use with caution.
         * This is typically called when the application is being destroyed.
         */
        @JvmStatic
        fun forceCleanup() {
            Logger.debug(TAG, "Force cleanup requested")
            cleanupFlutterEngine()
            // Additional cleanup can be added here if needed
        }
    }
}

/**
 * Extension function to add cleanup method to MethodCallHelper if needed.
 * This should be implemented in the MethodCallHelper class.
 */
private fun MethodCallHelper.cleanup() {
    // This method should be implemented in MethodCallHelper class
    // to clean up any resources, listeners, or observers
    Logger.debug("MethodCallHelper", "Cleanup called")
}