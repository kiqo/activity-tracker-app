package com.activitytracker.app.data.repository

import com.activitytracker.app.data.local.dao.LocationPointDao
import com.activitytracker.app.data.mapper.toDomain
import com.activitytracker.app.data.mapper.toEntity
import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
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
            .map { entities -> entities.toDomain() }
    }
    
    override fun getAccurateLocationPointsForSession(
        sessionId: Long,
        maxAccuracy: Float
    ): Flow<List<LocationPoint>> {
        return locationPointDao.getAccurateLocationPointsForSession(sessionId, maxAccuracy)
            .map { entities -> entities.toDomain() }
    }
    
    override suspend fun getLastLocationForSession(sessionId: Long): LocationPoint? {
        return locationPointDao.getLastLocationForSession(sessionId)?.toDomain()
    }
    
    override suspend fun getFirstLocationForSession(sessionId: Long): LocationPoint? {
        return locationPointDao.getFirstLocationForSession(sessionId)?.toDomain()
    }
    
    override suspend fun insertLocationPoint(point: LocationPoint): Long {
        return locationPointDao.insertLocationPoint(point.toEntity())
    }
    
    override suspend fun insertLocationPoints(points: List<LocationPoint>) {
        locationPointDao.insertLocationPoints(points.toEntity())
    }
}
