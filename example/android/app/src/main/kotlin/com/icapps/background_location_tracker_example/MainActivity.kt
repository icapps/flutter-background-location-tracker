package com.icapps.background_location_tracker_example

import android.util.Log
import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {

    override fun onResume() {
        Log.i("MainActivity", "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.i("MainActivity", "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.i("MainActivity", "onStop")
        super.onStop()
    }
}
