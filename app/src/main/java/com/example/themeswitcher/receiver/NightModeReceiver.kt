package com.example.themeswitcher.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.themeswitcher.activity.BaseActivity
import com.example.themeswitcher.manager.ThemeOverlayManager
import timber.log.Timber

/**
 * @author Alan Dreamer
 * @since 2018/06/22 Created
 */
class NightModeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            Timber.d("currentActivity: ${getCurrentActivity()}")
            ThemeOverlayManager.updateNightMode(getCurrentActivity(), it)
        }
    }

    /**
     * Get current activity.
     *
     * @return Return null when the application is in the background.
     */
    @SuppressLint("PrivateApi")
    @Suppress("UNCHECKED_CAST")
    private fun getCurrentActivity(): BaseActivity? {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null)
        val activitiesField = activityThreadClass.getDeclaredField("mActivities")
        activitiesField.isAccessible = true

        val activities = activitiesField.get(activityThread) as Map<Any, Any>

        for (activityRecord in activities.values) {
            val activityRecordClass = activityRecord.javaClass
            val pausedField = activityRecordClass.getDeclaredField("paused")
            pausedField.isAccessible = true
            if (!pausedField.getBoolean(activityRecord)) {
                val activityField = activityRecordClass.getDeclaredField("activity")
                activityField.isAccessible = true
                return activityField.get(activityRecord) as BaseActivity
            }
        }
        return null
    }
}