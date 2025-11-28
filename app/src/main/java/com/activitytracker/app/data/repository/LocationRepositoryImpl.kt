package com.activitytracker.app.data.repository

import com.activitytracker.app.data.local.dao.LocationPointDao
import com.activitytracker.app.data.mapper.toDomain
import com.activitytracker.app.data.mapper.toEntity
import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of LocationRepository using Room database.
 * Handles data mapping between entities and domain models.
 */
class LocationRepositoryImpl @Inject constructor(
    private val locationPointDao: LocationPointDao
) : LocationRepository {
    
    override fun getLocationPointsForSession(sessionId: Long): Flow<List<LocationPoint>> {
        return locationPointDao.getLocationPointsForSession(sessionId)
            .map { entities -> 
                entities.mapNotNull { entity ->
                    try {
                        entity.toDomain()
                    } catch (e: Exception) {
                        android.util.Log.e("LocationRepository", "Failed to map location point", e)
                        null
                    }
                }
            }
            .catch { e ->
                android.util.Log.e("LocationRepository", "Database read error", e)
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
                        android.util.Log.e("LocationRepository", "Failed to map location point", e)
                        null
                    }
                }
            }
            .catch { e ->
                android.util.Log.e("LocationRepository", "Database read error", e)
                emit(emptyList())
            }
    }
    
    override suspend fun getLastLocationForSession(sessionId: Long): LocationPoint? {
        return try {
            locationPointDao.getLastLocationForSession(sessionId)?.toDomain()
        } catch (e: Exception) {
            android.util.Log.e("LocationRepository", "Failed to get last location", e)
            null
        }
    }
    
    override suspend fun getFirstLocationForSession(sessionId: Long): LocationPoint? {
        return try {
            locationPointDao.getFirstLocationForSession(sessionId)?.toDomain()
        } catch (e: Exception) {
            android.util.Log.e("LocationRepository", "Failed to get first location", e)
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
                android.util.Log.e("LocationRepository", "Failed to insert location point (attempt $retryCount/$maxRetries)", e)
                
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
                android.util.Log.e("LocationRepository", "Failed to insert location points (attempt $retryCount/$maxRetries)", e)
                
                if (retryCount < maxRetries) {
                    kotlinx.coroutines.delay(500L * retryCount)
                }
            }
        }
        
        throw lastException ?: Exception("Failed to insert location points after $maxRetries attempts")
    }
}
