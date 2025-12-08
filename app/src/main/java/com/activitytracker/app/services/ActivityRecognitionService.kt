package com.activitytracker.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.activitytracker.app.ActivityTrackerApplication
import com.activitytracker.app.MainActivity
import com.activitytracker.app.R
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.usecase.StartActivityTrackingUseCase
import com.activitytracker.app.domain.usecase.StopActivityTrackingUseCase
import com.activitytracker.app.util.Logger
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that monitors activity changes and manages activity sessions.
 * Registers with Activity Recognition API and creates/ends sessions based on detected activities.
 */
@AndroidEntryPoint
class ActivityRecognitionService : Service() {

    @Inject
    lateinit var startActivityTrackingUseCase: StartActivityTrackingUseCase
    
    @Inject
    lateinit var stopActivityTrackingUseCase: StopActivityTrackingUseCase
    
    @Inject
    lateinit var logger: Logger

    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var currentSessionId: Long? = null
    private var currentActivityType: ActivityType? = null
    private var lastActivityTime: Long = 0

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CONFIDENCE_THRESHOLD = 75
        private const val INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes
        private const val REQUEST_CODE_ACTIVITY_TRANSITION = 1001
        
        const val ACTION_START_TRACKING = "com.activitytracker.app.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.activitytracker.app.STOP_TRACKING"
        const val ACTION_ACTIVITY_DETECTED = "com.activitytracker.app.ACTIVITY_DETECTED"
        const val ACTION_CHECK_INACTIVITY = "com.activitytracker.app.CHECK_INACTIVITY"
        
        const val EXTRA_ACTIVITY_TYPE = "activity_type"
        const val EXTRA_CONFIDENCE = "confidence"
    }
    
    private var transitionPendingIntent: PendingIntent? = null

    override fun onCreate() {
        super.onCreate()
        activityRecognitionClient = ActivityRecognition.getClient(this)
        startForeground(NOTIFICATION_ID, createNotification("Monitoring activities"))
        
        // Start periodic inactivity check
        startInactivityCheck()
    }
    
    private fun startInactivityCheck() {
        serviceScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000L) // Check every minute
                checkInactivityTimeout()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> {
                registerActivityRecognition()
            }
            ACTION_STOP_TRACKING -> {
                stopTracking()
                stopSelf()
            }
            ACTION_ACTIVITY_DETECTED -> {
                val activityType = intent.getIntExtra(EXTRA_ACTIVITY_TYPE, -1)
                val confidence = intent.getIntExtra(EXTRA_CONFIDENCE, 0)
                if (activityType != -1) {
                    handleActivityDetected(activityType, confidence)
                }
            }
            ACTION_CHECK_INACTIVITY -> {
                checkInactivityTimeout()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        unregisterActivityRecognition()
        serviceScope.cancel()
        
        // Stop current session if active
        currentSessionId?.let { sessionId ->
            serviceScope.launch {
                stopActivityTrackingUseCase(sessionId)
            }
        }
    }

    /**
     * Register for activity recognition updates.
     * Monitors for cycling, running, walking, and vehicle activities.
     */
    private fun registerActivityRecognition() {
        try {
            // Check if Google Play Services is available
            val googleApiAvailability = com.google.android.gms.common.GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
            
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                logger.e("Google Play Services not available: $resultCode")
                updateNotification("Error: Google Play Services unavailable")
                return
            }
            
            val transitions = mutableListOf<ActivityTransition>()
            
            // Add transitions for activities we want to track
            val activities = listOf(
                DetectedActivity.ON_BICYCLE,
                DetectedActivity.RUNNING,
                DetectedActivity.WALKING,
                DetectedActivity.IN_VEHICLE
            )
            
            activities.forEach { activityType ->
                transitions.add(
                    ActivityTransition.Builder()
                        .setActivityType(activityType)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()
                )
                transitions.add(
                    ActivityTransition.Builder()
                        .setActivityType(activityType)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build()
                )
            }
            
            val request = ActivityTransitionRequest(transitions)
            
            // Create PendingIntent for BroadcastReceiver
            val intent = Intent(this, ActivityTransitionReceiver::class.java).apply {
                action = ActivityTransitionReceiver.ACTION_ACTIVITY_TRANSITION
            }
            transitionPendingIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_ACTIVITY_TRANSITION,
                intent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            // Register for activity transition updates
            activityRecognitionClient.requestActivityTransitionUpdates(request, transitionPendingIntent!!)
                .addOnSuccessListener {
                    logger.d("Activity recognition registered successfully")
                    updateNotification("Monitoring activities")
                }
                .addOnFailureListener { e ->
                    logger.e(e, "Failed to register activity recognition")
                    updateNotification("Error: Failed to start monitoring")
                }
        } catch (e: SecurityException) {
            logger.e(e, "Permission denied")
            updateNotification("Error: Permission denied")
        } catch (e: Exception) {
            logger.e(e, "Failed to register activity recognition")
            updateNotification("Error: Failed to start monitoring")
        }
    }

    /**
     * Unregister from activity recognition updates.
     */
    private fun unregisterActivityRecognition() {
        transitionPendingIntent?.let { pendingIntent ->
            activityRecognitionClient.removeActivityTransitionUpdates(pendingIntent)
                .addOnSuccessListener {
                    logger.d("Activity recognition unregistered successfully")
                }
                .addOnFailureListener { e ->
                    logger.e(e, "Failed to unregister activity recognition")
                }
        }
        transitionPendingIntent = null
    }

    /**
     * Handle detected activity with confidence filtering.
     * Creates new session if confidence > 75%.
     * 
     * Note: This only manages AUTOMATIC sessions (Req 1.5, 1.6).
     * Manual sessions are managed independently by the user through the UI.
     * The StartActivityTrackingUseCase handles stopping existing automatic sessions
     * before creating new ones, ensuring at most 1 automatic session is active.
     */
    private fun handleActivityDetected(activityType: Int, confidence: Int) {
        logger.d("Handling activity $activityType with confidence $confidence")
        if (confidence < CONFIDENCE_THRESHOLD) {
            logger.d("Discarding activity due to too low confidence")
            return
        }
        
        val detectedActivity = mapToActivityType(activityType) ?: return
        lastActivityTime = System.currentTimeMillis()
        
        // If this is a different activity type, start new automatic session
        // The StartActivityTrackingUseCase will handle stopping any existing automatic session
        if (detectedActivity != currentActivityType) {
            logger.d("Starting new activity due to different activity type")
            serviceScope.launch {
                try {
                    // Start new automatic session (isManual = false)
                    // StartActivityTrackingUseCase will automatically stop any existing
                    // automatic session before creating the new one (Req 1.6)
                    // This does NOT affect any active manual sessions (Req 1.9)
                    val newSessionId = startActivityTrackingUseCase(detectedActivity, isManual = false)
                    currentSessionId = newSessionId
                    currentActivityType = detectedActivity
                    
                    // Start location tracking for this session
                    startLocationTracking(newSessionId)
                    
                    // Update notification
                    updateNotification("Tracking: ${detectedActivity.name}")
                } catch (e: Exception) {
                    logger.e(e, "Failed to handle activity")
                    updateNotification("Error: Failed to start tracking")
                    // Retry after a delay
                    kotlinx.coroutines.delay(5000)
                    handleActivityDetected(activityType, confidence)
                }
            }
        }
    }

    /**
     * Check for inactivity timeout (5 minutes).
     * Ends session if no activity detected.
     */
    private fun checkInactivityTimeout() {
        val currentTime = System.currentTimeMillis()
        if (currentSessionId != null && 
            currentTime - lastActivityTime > INACTIVITY_TIMEOUT_MS) {
            
            serviceScope.launch {
                currentSessionId?.let { sessionId ->
                    stopActivityTrackingUseCase(sessionId)
                    stopLocationTracking()
                }
                currentSessionId = null
                currentActivityType = null
                updateNotification("Monitoring activities")
            }
        }
    }

    /**
     * Start location tracking service for the session.
     */
    private fun startLocationTracking(sessionId: Long) {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
            putExtra(LocationTrackingService.EXTRA_SESSION_ID, sessionId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    /**
     * Stop location tracking service.
     */
    private fun stopLocationTracking() {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        startService(intent)
    }

    /**
     * Stop all tracking and end service.
     */
    private fun stopTracking() {
        serviceScope.launch {
            currentSessionId?.let { sessionId ->
                stopActivityTrackingUseCase(sessionId)
            }
            stopLocationTracking()
        }
    }

    /**
     * Map Google Activity Recognition activity type to our ActivityType enum.
     */
    private fun mapToActivityType(activityType: Int): ActivityType? {
        return when (activityType) {
            DetectedActivity.IN_VEHICLE -> ActivityType.IN_VEHICLE
            DetectedActivity.ON_BICYCLE -> ActivityType.CYCLING
            DetectedActivity.ON_FOOT -> ActivityType.ON_FOOT
            DetectedActivity.STILL -> ActivityType.STILL
            DetectedActivity.UNKNOWN -> ActivityType.UNKNOWN
            DetectedActivity.TILTING -> ActivityType.TILTING
            DetectedActivity.WALKING -> ActivityType.WALKING
            DetectedActivity.RUNNING -> ActivityType.RUNNING
            else -> null
        }
    }

    /**
     * Create foreground service notification.
     */
    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, ActivityTrackerApplication.CHANNEL_ID_TRACKING)
            .setContentTitle(getString(R.string.notification_tracking_title))
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

