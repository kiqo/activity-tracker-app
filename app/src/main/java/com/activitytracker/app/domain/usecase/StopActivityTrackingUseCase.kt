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
 */
class StopActivityTrackingUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val calculateRouteDistanceUseCase: CalculateRouteDistanceUseCase,
    private val estimateStepCountUseCase: EstimateStepCountUseCase,
    @ApplicationContext private val context: Context
) {
    /**
     * Stop an active session, stop location tracking, and update with final statistics.
     * @param sessionId The ID of the session to stop
     */
    suspend operator fun invoke(sessionId: Long) {
        // Stop LocationTrackingService first
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(intent)
        
        val session = activityRepository.getSessionById(sessionId).first() ?: return
        
        // Calculate total distance from location points
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
    }
}
