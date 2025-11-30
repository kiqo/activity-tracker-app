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
     * Link a location point to multiple sessions at once.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkLocationPointToSessions(links: List<SessionLocationPointEntity>)

    /**
     * Get all session IDs linked to a specific location point.
     */
    @Query("SELECT sessionId FROM session_location_points WHERE locationPointId = :locationPointId")
    suspend fun getSessionIdsForLocationPoint(locationPointId: Long): List<Long>

    /**
     * Get all location point IDs linked to a specific session.
     */
    @Query("SELECT locationPointId FROM session_location_points WHERE sessionId = :sessionId")
    suspend fun getLocationPointIdsForSession(sessionId: Long): List<Long>

    /**
     * Delete all links for a specific session.
     */
    @Query("DELETE FROM session_location_points WHERE sessionId = :sessionId")
    suspend fun deleteLinksForSession(sessionId: Long)

    /**
     * Delete all links for a specific location point.
     */
    @Query("DELETE FROM session_location_points WHERE locationPointId = :locationPointId")
    suspend fun deleteLinksForLocationPoint(locationPointId: Long)

    /**
     * Check if a location point is linked to any session.
     */
    @Query("SELECT COUNT(*) FROM session_location_points WHERE locationPointId = :locationPointId")
    suspend fun getLinkCountForLocationPoint(locationPointId: Long): Int
}
