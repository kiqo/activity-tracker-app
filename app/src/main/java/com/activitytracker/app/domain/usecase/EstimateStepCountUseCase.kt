package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.ActivityType
import javax.inject.Inject

/**
 * Use case for estimating step count based on distance and activity type.
 * Uses average stride length for walking and running.
 */
class EstimateStepCountUseCase @Inject constructor() {
    
    companion object {
        private const val WALKING_STRIDE_LENGTH_METERS = 0.762 // Average: 2.5 feet
        private const val RUNNING_STRIDE_LENGTH_METERS = 0.9 // Longer stride when running
    }
    
    /**
     * Estimate step count for an activity.
     * @param distanceMeters Total distance in meters
     * @param activityType Type of activity
     * @return Estimated step count (0 for non-walking/running activities)
     */
    operator fun invoke(distanceMeters: Double, activityType: ActivityType): Int {
        return when (activityType) {
            ActivityType.WALKING -> (distanceMeters / WALKING_STRIDE_LENGTH_METERS).toInt()
            ActivityType.RUNNING -> (distanceMeters / RUNNING_STRIDE_LENGTH_METERS).toInt()
            else -> 0 // No steps for cycling or vehicle
        }
    }
}
