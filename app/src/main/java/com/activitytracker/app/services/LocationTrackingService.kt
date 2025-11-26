package com.activitytracker.app.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.activitytracker.app.ActivityTrackerApplication
import com.activitytracker.app.MainActivity
import com.activitytracker.app.R
import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that tracks GPS location during active sessions.
 * Requests location updates every 10 seconds and stores them in the database.
 */
@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient
    
    @Inject
    lateinit var locationRepository: LocationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentSessionId: Long? = null
    private var locationCallback: LocationCallback? = null

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val LOCATION_UPDATE_INTERVAL_MS = 10_000L // 10 seconds
        private const val LOCATION_FASTEST_INTERVAL_MS = 5_000L // 5 seconds
        const val ACCURACY_THRESHOLD_METERS = 50f
        
        const val ACTION_START_TRACKING = "com.activitytracker.app.START_LOCATION_TRACKING"
        const val ACTION_STOP_TRACKING = "com.activitytracker.app.STOP_LOCATION_TRACKING"
        const val EXTRA_SESSION_ID = "session_id"
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
                if (sessionId != -1L) {
                    startLocationTracking(sessionId)
                }
            }
            ACTION_STOP_TRACKING -> {
                stopLocationTracking()
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationTracking()
        serviceScope.cancel()
    }

    /**
     * Start requesting location updates for the session.
     */
    private fun startLocationTracking(sessionId: Long) {
        currentSessionId = sessionId
        
        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopSelf()
            return
        }

        // Create location request
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
            setWaitForAccurateLocation(false)
        }.build()

        // Create location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }
        }

        // Request location updates
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    /**
     * Stop requesting location updates.
     */
    private fun stopLocationTracking() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
        }
        locationCallback = null
        currentSessionId = null
    }

    /**
     * Handle a location update from FusedLocationProviderClient.
     * Stores all location data in database with accuracy filtering for route display.
     */
    private fun handleLocationUpdate(location: Location) {
        val sessionId = currentSessionId ?: return
        
        val locationPoint = LocationPoint(
            sessionId = sessionId,
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = if (location.hasAltitude()) location.altitude else null,
            accuracy = location.accuracy,
            timestamp = location.time
        )
        
        // Store location point in database
        serviceScope.launch(Dispatchers.IO) {
            try {
                locationRepository.insertLocationPoint(locationPoint)
                
                // Update notification with accuracy info
                launch(Dispatchers.Main) {
                    val accuracyText = if (location.accuracy < ACCURACY_THRESHOLD_METERS) {
                        "Tracking (Good GPS: ${location.accuracy.toInt()}m)"
                    } else {
                        "Tracking (Poor GPS: ${location.accuracy.toInt()}m)"
                    }
                    updateNotification(accuracyText)
                }
            } catch (e: Exception) {
                // Log error but continue tracking
                e.printStackTrace()
            }
        }
    }

    /**
     * Create foreground service notification.
     */
    private fun createNotification(contentText: String = "Tracking location"): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, ActivityTrackerApplication.CHANNEL_ID_TRACKING)
            .setContentTitle("Location Tracking")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    /**
     * Update the foreground notification.
     */
    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification(contentText))
    }
}

