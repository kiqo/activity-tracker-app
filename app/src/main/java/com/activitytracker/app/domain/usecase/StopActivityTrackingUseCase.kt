package com.activitytracker.app.domain.usecase

import android.content.Context
import android.content.Intent
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.domain.repository.LocationRepository
import com.activitytracker.app.services.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for stopping an activity tracking session and calculating final statistics.
 * Handles shared location tracking: only stops LocationTrackingService when ALL sessions have ended.
 */
class StopActivityTrackingUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val calculateRouteDistanceUseCase: CalculateRouteDistanceUseCase,
    private val estimateStepCountUseCase: EstimateStepCountUseCase,
    @ApplicationContext private val context: Context
) {
    /**
     * Stop an active session and update with final statistics.
     * Only stops LocationTrackingService if no other sessions are active.
     * 
     * @param sessionId The ID of the session to stop
     */
    suspend operator fun invoke(sessionId: Long) {
        val session = activityRepository.getSessionById(sessionId).first() ?: return
        
        // Calculate total distance from location points linked to this session
        val totalDistance = calculateRouteDistanceUseCase(sessionId)
        
        // Calculate duration in seconds
        val endTime = System.currentTimeMillis()
        val durationSeconds = (endTime - session.startTime) / 1000.0
        
        // Calculate average speed (m/s)
        val averageSpeed = if (durationSeconds > 0) {
            totalDistance / durationSeconds
        } else {
            0.0
        }
        
        // Estimate step count for walking/running
        val stepCount = estimateStepCountUseCase(totalDistance, session.activityType)
        
        // Update session with final statistics
        val updatedSession = session.copy(
            endTime = endTime,
            totalDistance = totalDistance,
            averageSpeed = averageSpeed,
            stepCount = stepCount
        )
        
        activityRepository.updateSession(updatedSession)
        
        // Check if there are other active sessions
        val manualSession = activityRepository.getActiveManualSession()
        val automaticSession = activityRepository.getActiveAutomaticSession()
        
        val hasOtherActiveSessions = (manualSession != null && manualSession.id != sessionId) ||
                                     (automaticSession != null && automaticSession.id != sessionId)
        
        // Only stop LocationTrackingService if no other sessions are active
        if (!hasOtherActiveSessions) {
            android.util.Log.d("StopActivityTracking", "No other active sessions, stopping LocationTrackingService")
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = LocationTrackingService.ACTION_STOP_TRACKING
            }
            context.startService(intent)
        } else {
            android.util.Log.d("StopActivityTracking", "Other sessions still active, keeping LocationTrackingService running")
        }
    }
}
