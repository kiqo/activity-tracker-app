package com.activitytracker.app.domain.repository

import com.activitytracker.app.domain.model.ActivitySession
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for activity session operations.
 * Provides Flow-based reactive data access for activity sessions.
 */
interface ActivityRepository {
    
    /**
     * Get all activity sessions ordered by start time (newest first).
     */
    fun getAllSessions(): Flow<List<ActivitySession>>
    
    /**
     * Get a specific activity session by ID.
     */
    fun getSessionById(id: Long): Flow<ActivitySession?>
    
    /**
     * Get activity sessions within a specific time range.
     * Used for statistics calculations (daily, weekly, monthly).
     */
    fun getSessionsInTimeRange(startTime: Long, endTime: Long): Flow<List<ActivitySession>>
    
    /**
     * Get the most recent cycling session.
     * Used for bike location feature.
     */
    fun getLastCyclingSession(): Flow<ActivitySession?>
    
    /**
     * Get sessions by activity type.
     */
    fun getSessionsByType(activityType: String): Flow<List<ActivitySession>>
    
    /**
     * Insert a new activity session.
     * Returns the ID of the inserted session.
     */
    suspend fun insertSession(session: ActivitySession): Long
    
    /**
     * Update an existing activity session.
     */
    suspend fun updateSession(session: ActivitySession)
    
    /**
     * Delete an activity session by ID.
     */
    suspend fun deleteSession(id: Long)
}
