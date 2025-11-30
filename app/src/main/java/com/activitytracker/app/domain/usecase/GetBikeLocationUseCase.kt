package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for retrieving the last known bike location.
 * Finds the most recent cycling session and returns its last location point.
 */
class GetBikeLocationUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val locationRepository: LocationRepository
) {
    /**
     * Get the last location where the bike was parked.
     * @return LocationPoint if a cycling session exists, null otherwise
     */
    suspend operator fun invoke(): LocationPoint? {
        // Get the most recent cycling session
        val lastCyclingSession = activityRepository.getLastCyclingSession().first()
            ?: return null
        
        // Get the last location point from that session
        // TODO: Use precise last location
        return locationRepository.getLastLocationForSession(lastCyclingSession.id)
    }
}
