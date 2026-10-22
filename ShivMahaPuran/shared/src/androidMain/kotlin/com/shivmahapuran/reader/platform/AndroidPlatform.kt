package com.shivmahapuran.reader.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.shivmahapuran.reader.LocalAppLocaleSnapshot
import java.lang.ref.WeakReference
import java.util.Locale

/** Holds the application context and the foreground activity for platform services. */
object AndroidPlatform {
    lateinit var application: Application
        private set

    private var activityRef: WeakReference<Activity>? = null
    val currentActivity: Activity? get() = activityRef?.get()

    fun init(app: Application) {
        application = app
        LocalAppLocaleSnapshot.systemCode = Locale.getDefault().language
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                activityRef = WeakReference(activity)
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activityRef = WeakReference(activity)
            }
            override fun onActivityDestroyed(activity: Activity) {
                if (activityRef?.get() === activity) activityRef = null
            }
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        })
    }
}
