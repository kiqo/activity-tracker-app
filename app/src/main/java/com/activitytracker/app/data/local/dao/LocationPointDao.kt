package com.activitytracker.app.data.local.dao

import androidx.room.*
import com.activitytracker.app.data.local.entity.LocationPointEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for LocationPoint operations.
 * Provides queries for session-based location retrieval via junction table.
 */
@Dao
interface LocationPointDao {
    
    /**
     * Get all location points for a specific session ordered by timestamp.
     * Queries via junction table to support shared location points.
     */
    @Query("""
        SELECT lp.* FROM location_points lp
        INNER JOIN session_location_points slp ON lp.id = slp.locationPointId
        WHERE slp.sessionId = :sessionId
        ORDER BY lp.timestamp ASC
    """)
    fun getLocationPointsForSession(sessionId: Long): Flow<List<LocationPointEntity>>
    
    /**
     * Get location points for a session with accuracy filter ordered by timestamp.
     * Used for route rendering (only points with accuracy < 30m).
     */
    @Query("""
        SELECT lp.* FROM location_points lp
        INNER JOIN session_location_points slp ON lp.id = slp.locationPointId
        WHERE slp.sessionId = :sessionId AND lp.accuracy < :maxAccuracy
        ORDER BY lp.timestamp ASC
    """)
    fun getAccurateLocationPointsForSession(sessionId: Long, maxAccuracy: Float = 30f): Flow<List<LocationPointEntity>>
    
    /**
     * Get the last location point for a specific session.
     * Used for bike location feature.
     */
    @Query("""
        SELECT lp.* FROM location_points lp
        INNER JOIN session_location_points slp ON lp.id = slp.locationPointId
        WHERE slp.sessionId = :sessionId
        ORDER BY lp.timestamp DESC
        LIMIT 1
    """)
    suspend fun getLastLocationForSession(sessionId: Long): LocationPointEntity?
    
    /**
     * Get the first location point for a specific session.
     * Used for route start marker.
     */
    @Query("""
        SELECT lp.* FROM location_points lp
        INNER JOIN session_location_points slp ON lp.id = slp.locationPointId
        WHERE slp.sessionId = :sessionId
        ORDER BY lp.timestamp ASC
        LIMIT 1
    """)
    suspend fun getFirstLocationForSession(sessionId: Long): LocationPointEntity?
    
    /**
     * Get count of location points for a session.
     */
    @Query("""
        SELECT COUNT(*) FROM location_points lp
        INNER JOIN session_location_points slp ON lp.id = slp.locationPointId
        WHERE slp.sessionId = :sessionId
    """)
    suspend fun getLocationPointCount(sessionId: Long): Int
    
    /**
     * Insert a new location point and return its ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(point: LocationPointEntity): Long
    
    /**
     * Insert multiple location points in a single transaction.
     */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoints(points: List<LocationPointEntity>)
    
    /**
     * Delete all location points.
     * Useful for testing or clearing all data.
     */
    @Query("DELETE FROM location_points")
    suspend fun deleteAllLocationPoints()
}
