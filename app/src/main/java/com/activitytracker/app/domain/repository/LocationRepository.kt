package com.activitytracker.app.domain.repository

import com.activitytracker.app.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for location point operations.
 * Provides Flow-based reactive data access for location points.
 */
interface LocationRepository {
    
    /**
     * Get all location points for a specific session ordered by timestamp.
     */
    fun getLocationPointsForSession(sessionId: Long): Flow<List<LocationPoint>>
    
    /**
     * Get location points for a session with accuracy filter.
     * Used for route rendering (only points with accuracy < 20m).
     */
    fun getAccurateLocationPointsForSession(
        sessionId: Long,
        maxAccuracy: Float = 20f
    ): Flow<List<LocationPoint>>
    
    /**
     * Get the last location point for a specific session.
     * Used for bike location feature.
     */
    suspend fun getLastLocationForSession(sessionId: Long): LocationPoint?
    
    /**
     * Get the first location point for a specific session.
     * Used for route start marker.
     */
    suspend fun getFirstLocationForSession(sessionId: Long): LocationPoint?
    
    /**
     * Insert a new location point.
     */
    suspend fun insertLocationPoint(point: LocationPoint): Long
    
    /**
     * Insert multiple location points in a single transaction.
     */
    suspend fun insertLocationPoints(points: List<LocationPoint>)
}
