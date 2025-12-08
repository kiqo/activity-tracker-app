package com.activitytracker.app.data.repository

import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.local.dao.LocationPointDao
import com.activitytracker.app.data.local.dao.SessionLocationPointDao
import com.activitytracker.app.data.local.entity.SessionLocationPointEntity
import com.activitytracker.app.data.mapper.toDomain
import com.activitytracker.app.data.mapper.toEntity
import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.repository.LocationRepository
import com.activitytracker.app.util.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of LocationRepository using Room database.
 * Handles data mapping between entities and domain models.
 * Supports shared location tracking via junction table.
 */
class LocationRepositoryImpl @Inject constructor(
    private val locationPointDao: LocationPointDao,
    private val sessionLocationPointDao: SessionLocationPointDao,
    private val activitySessionDao: ActivitySessionDao,
    private val logger: Logger
) : LocationRepository {
    
    override fun getLocationPointsForSession(sessionId: Long): Flow<List<LocationPoint>> {
        return locationPointDao.getLocationPointsForSession(sessionId)
            .map { entities -> 
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        logger.e(e, "Failed to map location point")
                        null
                    }
                }
            }
            .catch { e ->
                logger.e(e as? Exception ?: Exception(e), "Database read error")
                emit(emptyList())
            }
    }
    
    override fun getAccurateLocationPointsForSession(
        sessionId: Long,
        maxAccuracy: Float
    ): Flow<List<LocationPoint>> {
        return locationPointDao.getAccurateLocationPointsForSession(sessionId, maxAccuracy)
            .map { entities -> 
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        logger.e(e, "Failed to map location point")
                        null
                    }
                }
            }
            .catch { e ->
                logger.e(e as? Exception ?: Exception(e), "Database read error")
                emit(emptyList())
            }
    }
    
    override suspend fun getLastLocationForSession(sessionId: Long): LocationPoint? {
        return try {
            locationPointDao.getLastLocationForSession(sessionId)?.toDomain()
        } catch (e: Exception) {
            logger.e(e, "Failed to get last location")
            null
        }
    }
    
    override suspend fun getFirstLocationForSession(sessionId: Long): LocationPoint? {
        return try {
            locationPointDao.getFirstLocationForSession(sessionId)?.toDomain()
        } catch (e: Exception) {
            logger.e(e, "Failed to get first location")
            null
        }
    }
    
    override suspend fun insertLocationPoint(point: LocationPoint): Long {
        var retryCount = 0
        val maxRetries = 3
        var lastException: Exception? = null
        
        while (retryCount < maxRetries) {
            try {
                return locationPointDao.insertLocationPoint(point.toEntity())
            } catch (e: Exception) {
                lastException = e
                retryCount++
                logger.e(e, "Failed to insert location point (attempt $retryCount/$maxRetries)")
                
                if (retryCount < maxRetries) {
                    kotlinx.coroutines.delay(500L * retryCount)
                }
            }
        }
        
        throw lastException ?: Exception("Failed to insert location point after $maxRetries attempts")
    }
    
    override suspend fun insertLocationPoints(points: List<LocationPoint>) {
        var retryCount = 0
        val maxRetries = 3
        var lastException: Exception? = null
        
        while (retryCount < maxRetries) {
            try {
                locationPointDao.insertLocationPoints(points.toEntity())
                return
            } catch (e: Exception) {
                lastException = e
                retryCount++
                logger.e(e, "Failed to insert location points (attempt $retryCount/$maxRetries)")
                
                if (retryCount < maxRetries) {
                    kotlinx.coroutines.delay(500L * retryCount)
                }
            }
        }
        
        throw lastException ?: Exception("Failed to insert location points after $maxRetries attempts")
    }
    
    override suspend fun linkLocationPointToSession(locationPointId: Long, sessionId: Long) {
        try {
            val link = SessionLocationPointEntity(
                sessionId = sessionId,
                locationPointId = locationPointId
            )
            sessionLocationPointDao.linkLocationPointToSession(link)
        } catch (e: Exception) {
            logger.e(e, "Failed to link location point to session")
            throw e
        }
    }
    
    override suspend fun linkLocationPointToAllActiveSessions(locationPointId: Long) {
        try {
            // Get all active sessions (where endTime is null)
            val activeSessions = activitySessionDao.getActiveSessionsSync()
            
            if (activeSessions.isEmpty()) {
                logger.w("No active sessions to link location point to")
                return
            }
            
            // Create junction table entries for all active sessions
            val links = activeSessions.map { session ->
                SessionLocationPointEntity(
                    sessionId = session.id,
                    locationPointId = locationPointId
                )
            }
            
            sessionLocationPointDao.linkLocationPointToSessions(links)
            
            logger.d("Linked location point $locationPointId to ${activeSessions.size} active sessions")
        } catch (e: Exception) {
            logger.e(e, "Failed to link location point to active sessions")
            throw e
        }
    }
}
