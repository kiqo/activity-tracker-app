package com.activitytracker.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service that monitors activity changes and manages activity sessions.
 * Implementation will be completed in Task 4.1
 */
@AndroidEntryPoint
class ActivityRecognitionService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Service implementation will be added in Task 4.1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Service implementation will be added in Task 4.1
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup will be added in Task 4.1
    }
}
