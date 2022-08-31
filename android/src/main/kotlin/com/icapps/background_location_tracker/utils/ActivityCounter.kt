package com.icapps.background_location_tracker.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

object ActivityCounter : Application.ActivityLifecycleCallbacks {
    private var knownApplication: Application? = null
    private val startedActivities = mutableListOf<WeakReference<Activity>>()

    fun attach(activity: Activity) {
        val application = activity.application
        if (knownApplication != application) {
            knownApplication = application
            application.registerActivityLifecycleCallbacks(this)
            startedActivities.add(WeakReference(activity))
        }
    }

    fun isAppInBackground(): Boolean = startedActivities.also { it.removeAll { ac -> ac.get() == null } }.isEmpty()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        startedActivities.add(WeakReference(activity))
    }

    override fun onActivityPaused(activity: Activity) {
        startedActivities.removeAll { it.get() == null || it.get() === activity }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}