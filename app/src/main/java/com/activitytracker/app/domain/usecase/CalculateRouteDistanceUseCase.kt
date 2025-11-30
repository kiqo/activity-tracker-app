package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.math.*

/**
 * Use case for calculating total distance of a route using the Haversine formula.
 * Only includes accurate location points.
 */
class CalculateRouteDistanceUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    /**
     * Calculate total distance for a session's route.
     * @param sessionId The ID of the session
     * @return Total distance in meters
     */
    suspend operator fun invoke(sessionId: Long): Double {
        val points = locationRepository.getAccurateLocationPointsForSession(sessionId).first()
        
        if (points.size < 2) {
            return 0.0
        }
        
        var totalDistance = 0.0
        
        // Calculate distance between consecutive points
        for (i in 0 until points.size - 1) {
            val point1 = points[i]
            val point2 = points[i + 1]
            totalDistance += calculateDistance(
                point1.latitude, point1.longitude,
                point2.latitude, point2.longitude
            )
        }
        
        return totalDistance
    }
    
    /**
     * Calculate distance between two GPS coordinates using Haversine formula.
     * @return Distance in meters
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusMeters = 6371000.0
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return earthRadiusMeters * c
    }
}
