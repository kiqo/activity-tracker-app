package com.activitytracker.app.data.local.dao

import androidx.room.*
import com.activitytracker.app.data.local.entity.ActivitySessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ActivitySession operations.
 * Provides queries for CRUD operations and time-based filtering.
 */
@Dao
interface ActivitySessionDao {
    
    /**
     * Get all activity sessions ordered by start time (newest first).
     */
    @Query("SELECT * FROM activity_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ActivitySessionEntity>>
    
    /**
     * Get a specific activity session by ID.
     */
    @Query("SELECT * FROM activity_sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: Long): Flow<ActivitySessionEntity?>
    
    /**
     * Get activity sessions within a specific time range.
     * Used for statistics calculations (daily, weekly, monthly).
     */
    @Query("SELECT * FROM activity_sessions WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    fun getSessionsInTimeRange(startTime: Long, endTime: Long): Flow<List<ActivitySessionEntity>>
    
    /**
     * Get the most recent cycling session.
     * Used for bike location feature.
     */
    @Query("SELECT * FROM activity_sessions WHERE activityType = 'CYCLING' ORDER BY startTime DESC LIMIT 1")
    fun getLastCyclingSession(): Flow<ActivitySessionEntity?>
    
    /**
     * Get sessions by activity type.
     */
    @Query("SELECT * FROM activity_sessions WHERE activityType = :activityType ORDER BY startTime DESC")
    fun getSessionsByType(activityType: String): Flow<List<ActivitySessionEntity>>
    
    /**
     * Insert a new activity session.
     * Returns the ID of the inserted session.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ActivitySessionEntity): Long
    
    /**
     * Update an existing activity session.
     */
    @Update
    suspend fun updateSession(session: ActivitySessionEntity)
    
    /**
     * Delete an activity session by ID.
     * Cascade delete will automatically remove associated location points.
     */
    @Query("DELETE FROM activity_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
    
    /**
     * Delete all activity sessions.
     * Useful for testing or clearing all data.
     */
    @Query("DELETE FROM activity_sessions")
    suspend fun deleteAllSessions()
    
    /**
     * Get all currently active sessions (where endTime is null).
     */
    @Query("SELECT * FROM activity_sessions WHERE endTime IS NULL ORDER BY startTime DESC")
    fun getActiveSessions(): Flow<List<ActivitySessionEntity>>
    
    /**
     * Get the active manually-started session (if any).
     * Returns null if no manual session is currently active.
     * Used to enforce at most 1 manual session active at a time (Requirement 1.7).
     */
    @Query("SELECT * FROM activity_sessions WHERE endTime IS NULL AND isManuallyStarted = 1 LIMIT 1")
    suspend fun getActiveManualSession(): ActivitySessionEntity?
    
    /**
     * Get the active automatically-detected session (if any).
     * Returns null if no automatic session is currently active.
     * Used to enforce at most 1 automatic session active at a time (Requirement 1.5).
     */
    @Query("SELECT * FROM activity_sessions WHERE endTime IS NULL AND isManuallyStarted = 0 LIMIT 1")
    suspend fun getActiveAutomaticSession(): ActivitySessionEntity?
    
    /**
     * Get all currently active sessions synchronously (where endTime is null).
     * Used by LocationRepository to link location points to all active sessions.
     */
    @Query("SELECT * FROM activity_sessions WHERE endTime IS NULL ORDER BY startTime DESC")
    suspend fun getActiveSessionsSync(): List<ActivitySessionEntity>
}
