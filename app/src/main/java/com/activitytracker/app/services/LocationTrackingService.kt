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
    private var lastLocation: Location? = null
    private var isStationary = false

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val LOCATION_UPDATE_INTERVAL_MS = 10_000L // 10 seconds
        private const val LOCATION_FASTEST_INTERVAL_MS = 5_000L // 5 seconds
        private const val LOCATION_UPDATE_INTERVAL_STATIONARY_MS = 30_000L // 30 seconds when stationary
        private const val MAX_WAIT_TIME_MS = 60_000L // 1 minute for batched updates
        private const val STATIONARY_DISTANCE_THRESHOLD = 20f // 20 meters
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
        
        try {
            // Check if Google Play Services is available
            val googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
            
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                android.util.Log.e("LocationTracking", "Google Play Services not available: $resultCode")
                updateNotification("Error: Google Play Services unavailable")
                stopSelf()
                return
            }
            
            // Check if location services are enabled
            val locationManager = getSystemService(LOCATION_SERVICE) as? android.location.LocationManager
            val isGpsEnabled = locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ?: false
            val isNetworkEnabled = locationManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) ?: false
            
            if (!isGpsEnabled && !isNetworkEnabled) {
                android.util.Log.e("LocationTracking", "Location services are disabled")
                updateNotification("Error: Location services disabled")
                stopSelf()
                return
            }
            
            // Check location permission
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                android.util.Log.e("LocationTracking", "Location permission not granted")
                updateNotification("Error: Location permission denied")
                stopSelf()
                return
            }

            // Create location request with battery optimizations
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                if (isStationary) LOCATION_UPDATE_INTERVAL_STATIONARY_MS else LOCATION_UPDATE_INTERVAL_MS
            ).apply {
                setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL_MS)
                setMaxUpdateDelayMillis(MAX_WAIT_TIME_MS) // Enable batched updates
                setWaitForAccurateLocation(false)
            }.build()

            // Create location callback
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        handleLocationUpdate(location)
                    } ?: run {
                        // No location available - GPS signal lost
                        android.util.Log.w("LocationTracking", "No location in result")
                        updateNotification("Waiting for GPS signal...")
                    }
                }
            }

            // Request location updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            ).addOnSuccessListener {
                android.util.Log.d("LocationTracking", "Location updates started successfully")
                updateNotification("Tracking location")
            }.addOnFailureListener { e ->
                android.util.Log.e("LocationTracking", "Failed to start location updates", e)
                updateNotification("Error: Failed to start location tracking")
                // Retry after a delay
                serviceScope.launch {
                    kotlinx.coroutines.delay(10000)
                    if (currentSessionId != null) {
                        startLocationTracking(sessionId)
                    }
                }
            }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationTracking", "Security exception", e)
            updateNotification("Error: Permission denied")
            stopSelf()
        } catch (e: Exception) {
            android.util.Log.e("LocationTracking", "Failed to start location tracking", e)
            updateNotification("Error: Failed to start tracking")
            stopSelf()
        }
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
        lastLocation = null
        isStationary = false
    }
    
    /**
     * Restart location updates with adjusted interval based on stationary state.
     * Used for battery optimization when user is not moving.
     */
    private fun restartLocationUpdates(sessionId: Long) {
        stopLocationTracking()
        currentSessionId = sessionId
        startLocationTracking(sessionId)
    }

    /**
     * Handle a location update from FusedLocationProviderClient.
     * Stores all location data in database with accuracy filtering for route display.
     * Implements stationary detection for battery optimization.
     */
    private fun handleLocationUpdate(location: Location) {
        val sessionId = currentSessionId ?: return
        
        // Check if user is stationary (battery optimization)
        val wasStationary = isStationary
        lastLocation?.let { last ->
            val distance = last.distanceTo(location)
            isStationary = distance < STATIONARY_DISTANCE_THRESHOLD
            
            // If stationary state changed, restart location updates with new interval
            if (wasStationary != isStationary) {
                android.util.Log.d("LocationTracking", "Stationary state changed: $isStationary")
                restartLocationUpdates(sessionId)
            }
        }
        lastLocation = location
        
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
            var retryCount = 0
            val maxRetries = 3
            
            while (retryCount < maxRetries) {
                try {
                    locationRepository.insertLocationPoint(locationPoint)
                    
                    // Update notification with accuracy and stationary info
                    launch(Dispatchers.Main) {
                        val stationaryText = if (isStationary) " (Stationary)" else ""
                        val accuracyText = if (location.accuracy < ACCURACY_THRESHOLD_METERS) {
                            "Tracking (Good GPS: ${location.accuracy.toInt()}m)$stationaryText"
                        } else {
                            "Tracking (Poor GPS: ${location.accuracy.toInt()}m)$stationaryText"
                        }
                        updateNotification(accuracyText)
                    }
                    break // Success, exit retry loop
                } catch (e: Exception) {
                    retryCount++
                    android.util.Log.e("LocationTracking", "Failed to save location (attempt $retryCount/$maxRetries)", e)
                    
                    if (retryCount >= maxRetries) {
                        // Failed after all retries
                        android.util.Log.e("LocationTracking", "Failed to save location after $maxRetries attempts")
                        launch(Dispatchers.Main) {
                            updateNotification("Error: Failed to save location")
                        }
                    } else {
                        // Wait before retrying
                        kotlinx.coroutines.delay(1000L * retryCount)
                    }
                }
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

