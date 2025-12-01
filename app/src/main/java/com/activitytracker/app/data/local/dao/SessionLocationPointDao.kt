package com.activitytracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.activitytracker.app.data.local.entity.SessionLocationPointEntity

/**
 * Data Access Object for SessionLocationPoint junction table operations.
 * Manages the many-to-many relationship between sessions and location points.
 */
@Dao
interface SessionLocationPointDao {
    
    /**
     * Link a location point to a specific session.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkLocationPointToSession(link: SessionLocationPointEntity)
    
    /**
     * Link a location point to multiple sessions in a single transaction.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkLocationPointToSessions(links: List<SessionLocationPointEntity>)
    
    /**
     * Get all session IDs that are linked to a specific location point.
     */
    @Query("SELECT sessionId FROM session_location_points WHERE locationPointId = :locationPointId")
    suspend fun getSessionIdsForLocationPoint(locationPointId: Long): List<Long>
    
    /**
     * Get count of sessions linked to a location point.
     */
    @Query("SELECT COUNT(*) FROM session_location_points WHERE locationPointId = :locationPointId")
    suspend fun getSessionCountForLocationPoint(locationPointId: Long): Int
    
    /**
     * Delete all links for a specific session.
     * Note: This is handled automatically by CASCADE delete, but provided for explicit control.
     */
    @Query("DELETE FROM session_location_points WHERE sessionId = :sessionId")
    suspend fun deleteLinksForSession(sessionId: Long)
    
    /**
     * Delete all links for a specific location point.
     * Note: This is handled automatically by CASCADE delete, but provided for explicit control.
     */
    @Query("DELETE FROM session_location_points WHERE locationPointId = :locationPointId")
    suspend fun deleteLinksForLocationPoint(locationPointId: Long)
    
    /**
     * Delete all links.
     * Useful for testing or clearing all data.
     */
    @Query("DELETE FROM session_location_points")
    suspend fun deleteAllLinks()
}
