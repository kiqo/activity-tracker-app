package com.activitytracker.app.data.repository

import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.mapper.toDomain
import com.activitytracker.app.data.mapper.toEntity
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of ActivityRepository using Room database.
 * Handles data mapping between entities and domain models.
 */
class ActivityRepositoryImpl @Inject constructor(
    private val activitySessionDao: ActivitySessionDao,
    private val logger: Logger
) : ActivityRepository {
    
    override fun getAllSessions(): Flow<List<ActivitySession>> {
        return activitySessionDao.getAllSessions()
            .map { entities -> 
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        logger.e(e, "Failed to map entity to domain")
                        null // Skip corrupted data
                    }
                }
            }
            .catch { e ->
                logger.e(e, "Database read error")
                emit(emptyList()) // Return empty list on error
            }
    }
    
    override fun getSessionById(id: Long): Flow<ActivitySession?> {
        return activitySessionDao.getSessionById(id)
            .map { entity -> 
                try {
                    entity?.toDomain()
                } catch (e: Exception) {
                    logger.e(e, "Failed to map entity to domain")
                    null // Return null for corrupted data
                }
            }
            .catch { e ->
                logger.e(e, "Database read error")
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
                        logger.e(e, "Failed to map entity to domain")
                        null
                    }
                }
            }
            .catch { e ->
                logger.e(e, "Database read error")
                emit(emptyList())
            }
    }
    
    override fun getLastCyclingSession(): Flow<ActivitySession?> {
        return activitySessionDao.getLastCyclingSession()
            .map { entity -> 
                try {
                    entity?.toDomain()
                } catch (e: Exception) {
                    logger.e(e, "Failed to map entity to domain")
                    null
                }
            }
            .catch { e ->
                logger.e(e, "Database read error")
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
                        logger.e(e, "Failed to map entity to domain")
                        null
                    }
                }
            }
            .catch { e ->
                logger.e(e, "Database read error")
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
                logger.e(e, "Failed to insert session (attempt $retryCount/$maxRetries)")
                
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
                logger.e(e, "Failed to update session (attempt $retryCount/$maxRetries)")
                
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
            logger.e(e, "Failed to delete session")
            throw e
        }
    }
    
    override fun getActiveSessions(): Flow<List<ActivitySession>> {
        return activitySessionDao.getActiveSessions()
            .map { entities -> 
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        logger.e(e, "Failed to map entity to domain")
                        null
                    }
                }
            }
            .catch { e ->
                logger.e(e, "Database read error")
                emit(emptyList())
            }
    }
    
    override suspend fun getActiveManualSession(): ActivitySession? {
        return try {
            activitySessionDao.getActiveManualSession()?.toDomain()
        } catch (e: Exception) {
            logger.e(e, "Failed to get active manual session")
            null
        }
    }
    
    override suspend fun getActiveAutomaticSession(): ActivitySession? {
        return try {
            activitySessionDao.getActiveAutomaticSession()?.toDomain()
        } catch (e: Exception) {
            logger.e(e, "Failed to get active automatic session")
            null
        }
    }
}
