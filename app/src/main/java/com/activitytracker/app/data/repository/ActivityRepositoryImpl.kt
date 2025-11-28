package com.activitytracker.app.data.repository

import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.mapper.toDomain
import com.activitytracker.app.data.mapper.toEntity
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of ActivityRepository using Room database.
 * Handles data mapping between entities and domain models.
 */
class ActivityRepositoryImpl @Inject constructor(
    private val activitySessionDao: ActivitySessionDao
) : ActivityRepository {
    
    override fun getAllSessions(): Flow<List<ActivitySession>> {
        return activitySessionDao.getAllSessions()
            .map { entities -> 
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        android.util.Log.e("ActivityRepository", "Failed to map entity to domain", e)
                        null // Skip corrupted data
                    }
                }
            }
            .catch { e ->
                android.util.Log.e("ActivityRepository", "Database read error", e)
                emit(emptyList()) // Return empty list on error
            }
    }
    
    override fun getSessionById(id: Long): Flow<ActivitySession?> {
        return activitySessionDao.getSessionById(id)
            .map { entity -> 
                try {
                    entity?.toDomain()
                } catch (e: Exception) {
                    android.util.Log.e("ActivityRepository", "Failed to map entity to domain", e)
                    null // Return null for corrupted data
                }
            }
            .catch { e ->
                android.util.Log.e("ActivityRepository", "Database read error", e)
                emit(null)
            }
    }
    
    override fun getSessionsInTimeRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<ActivitySession>> {
        return activitySessionDao.getSessionsInTimeRange(startTime, endTime)
            .map { entities -> 
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        android.util.Log.e("ActivityRepository", "Failed to map entity to domain", e)
                        null
                    }
                }
            }
            .catch { e ->
                android.util.Log.e("ActivityRepository", "Database read error", e)
                emit(emptyList())
            }
    }
    
    override fun getLastCyclingSession(): Flow<ActivitySession?> {
        return activitySessionDao.getLastCyclingSession()
            .map { entity -> 
                try {
                    entity?.toDomain()
                } catch (e: Exception) {
                    android.util.Log.e("ActivityRepository", "Failed to map entity to domain", e)
                    null
                }
            }
            .catch { e ->
                android.util.Log.e("ActivityRepository", "Database read error", e)
                emit(null)
            }
    }
    
    override fun getSessionsByType(activityType: String): Flow<List<ActivitySession>> {
        return activitySessionDao.getSessionsByType(activityType)
            .map { entities -> 
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        android.util.Log.e("ActivityRepository", "Failed to map entity to domain", e)
                        null
                    }
                }
            }
            .catch { e ->
                android.util.Log.e("ActivityRepository", "Database read error", e)
                emit(emptyList())
            }
    }
    
    override suspend fun insertSession(session: ActivitySession): Long {
        var retryCount = 0
        val maxRetries = 3
        var lastException: Exception? = null
        
        while (retryCount < maxRetries) {
            try {
                return activitySessionDao.insertSession(session.toEntity())
            } catch (e: Exception) {
                lastException = e
                retryCount++
                android.util.Log.e("ActivityRepository", "Failed to insert session (attempt $retryCount/$maxRetries)", e)
                
                if (retryCount < maxRetries) {
                    kotlinx.coroutines.delay(1000L * retryCount)
                }
            }
        }
        
        throw lastException ?: Exception("Failed to insert session after $maxRetries attempts")
    }
    
    override suspend fun updateSession(session: ActivitySession) {
        var retryCount = 0
        val maxRetries = 3
        var lastException: Exception? = null
        
        while (retryCount < maxRetries) {
            try {
                activitySessionDao.updateSession(session.toEntity())
                return
            } catch (e: Exception) {
                lastException = e
                retryCount++
                android.util.Log.e("ActivityRepository", "Failed to update session (attempt $retryCount/$maxRetries)", e)
                
                if (retryCount < maxRetries) {
                    kotlinx.coroutines.delay(1000L * retryCount)
                }
            }
        }
        
        throw lastException ?: Exception("Failed to update session after $maxRetries attempts")
    }
    
    override suspend fun deleteSession(id: Long) {
        try {
            activitySessionDao.deleteSession(id)
        } catch (e: Exception) {
            android.util.Log.e("ActivityRepository", "Failed to delete session", e)
            throw e
        }
    }
}
