package com.lwy.andytoolkits.application

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log


class AppLifecycleCallbacks : ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.d("Lifecycle", activity.localClassName + " created")
    }

    override fun onActivityStarted(activity: Activity) {
        Log.d("Lifecycle", activity.localClassName + " started")
    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("Lifecycle", activity.localClassName + " resumed")
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d("Lifecycle", activity.localClassName + " paused")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("Lifecycle", activity.localClassName + " stopped")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.d("Lifecycle", activity.localClassName + " save instance state")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d("Lifecycle", activity.localClassName + " destroyed")
    }
}
