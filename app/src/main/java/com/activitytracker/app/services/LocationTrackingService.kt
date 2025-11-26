package com.activitytracker.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint

/**
 * Foreground service that tracks GPS location during active sessions.
 * Implementation will be completed in Task 4.2
 */
@AndroidEntryPoint
class LocationTrackingService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Service implementation will be added in Task 4.2
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Service implementation will be added in Task 4.2
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup will be added in Task 4.2
    }
}
