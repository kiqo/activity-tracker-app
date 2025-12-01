package com.activitytracker.app.domain.repository

import com.activitytracker.app.domain.model.LocationPoint
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for location point operations.
 * Provides Flow-based reactive data access for location points.
 * Supports shared location tracking where one location point can be linked to multiple sessions.
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
     * Insert a new location point and return its ID.
     * The location point is stored once and can be linked to multiple sessions.
     */
    suspend fun insertLocationPoint(point: LocationPoint): Long
    
    /**
     * Insert multiple location points in a single transaction.
     */
    suspend fun insertLocationPoints(points: List<LocationPoint>)
    
    /**
     * Link a location point to a specific session.
     * Creates an entry in the junction table.
     */
    suspend fun linkLocationPointToSession(locationPointId: Long, sessionId: Long)
    
    /**
     * Link a location point to all currently active sessions.
     * Used by LocationTrackingService to share location points across manual and automatic sessions.
     */
    suspend fun linkLocationPointToAllActiveSessions(locationPointId: Long)
}
