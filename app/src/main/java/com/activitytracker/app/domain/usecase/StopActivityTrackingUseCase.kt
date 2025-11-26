package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for stopping an activity tracking session and calculating final statistics.
 */
class StopActivityTrackingUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val locationRepository: LocationRepository,
    private val calculateRouteDistanceUseCase: CalculateRouteDistanceUseCase,
    private val estimateStepCountUseCase: EstimateStepCountUseCase
) {
    /**
     * Stop an active session and update it with final statistics.
     * @param sessionId The ID of the session to stop
     */
    suspend operator fun invoke(sessionId: Long) {
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
