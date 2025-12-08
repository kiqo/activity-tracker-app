package com.activitytracker.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.activitytracker.app.util.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ActivityTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Logger.initializeLogger(BuildConfig.DEBUG)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackingChannel = NotificationChannel(
                CHANNEL_ID_TRACKING,
                getString(R.string.notification_channel_tracking_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_tracking_description)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(trackingChannel)
        }
    }

    companion object {
        const val CHANNEL_ID_TRACKING = "activity_tracking_channel"
    }
}
