package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.repository.ActivityRepository
import javax.inject.Inject

/**
 * Use case for starting a new activity tracking session.
 */
class StartActivityTrackingUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {
    /**
     * Start a new activity session.
     * @param activityType The type of activity to track
     * @return The ID of the newly created session
     */
    suspend operator fun invoke(activityType: ActivityType): Long {
        val session = ActivitySession(
            activityType = activityType,
            startTime = System.currentTimeMillis(),
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0
        )
        return activityRepository.insertSession(session)
    }
}
