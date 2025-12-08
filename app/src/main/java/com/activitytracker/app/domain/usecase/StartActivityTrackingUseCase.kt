package com.activitytracker.app.domain.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.services.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for starting a new activity tracking session.
 * 
 * Implements dual-track session management (Requirements 1.5-1.9):
 * - At most 1 manual session active at any time (Req 1.7)
 * - At most 1 automatic session active at any time (Req 1.5)
 * - Both manual and automatic sessions can coexist simultaneously (Req 1.9)
 * - New session of same type stops existing session of that type (Req 1.6, 1.8)
 */
class StartActivityTrackingUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val stopActivityTrackingUseCase: StopActivityTrackingUseCase,
    @ApplicationContext private val context: Context,
    private val logger: com.activitytracker.app.util.Logger
) {
    /**
     * Start a new activity session and begin location tracking.
     * 
     * If a session of the same type (manual/automatic) already exists, it will be
     * stopped first to ensure at most 1 manual and 1 automatic session are active.
     * 
     * @param activityType The type of activity to track
     * @param isManual True if manually started by user, false if auto-detected
     * @return The ID of the newly created session
     */
    suspend operator fun invoke(activityType: ActivityType, isManual: Boolean = true): Long {
        // Check for existing session of the same type and stop it if found
        // This ensures at most 1 manual and 1 automatic session active (Req 1.5, 1.7)
        val existingSession = if (isManual) {
            activityRepository.getActiveManualSession()
        } else {
            activityRepository.getActiveAutomaticSession()
        }
        
        existingSession?.let { session ->
            // Stop the existing session of the same type (Req 1.6, 1.8)
            stopActivityTrackingUseCase(session.id)
        }
        
        // Create new session
        val session = ActivitySession(
            activityType = activityType,
            startTime = System.currentTimeMillis(),
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = isManual
        )
        val sessionId = activityRepository.insertSession(session)
        
        logger.d("Starting LocationTrackingService from StartActivityTrackingUseCase")
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
            putExtra(LocationTrackingService.EXTRA_SESSION_ID, sessionId)
        }
        ContextCompat.startForegroundService(context, intent)
        
        return sessionId
    }
}
