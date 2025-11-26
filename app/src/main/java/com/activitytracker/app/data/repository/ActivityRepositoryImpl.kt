package com.activitytracker.app.data.repository

import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.mapper.toDomain
import com.activitytracker.app.data.mapper.toEntity
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
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
            .map { entities -> entities.toDomain() }
    }
    
    override fun getSessionById(id: Long): Flow<ActivitySession?> {
        return activitySessionDao.getSessionById(id)
            .map { entity -> entity?.toDomain() }
    }
    
    override fun getSessionsInTimeRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<ActivitySession>> {
        return activitySessionDao.getSessionsInTimeRange(startTime, endTime)
            .map { entities -> entities.toDomain() }
    }
    
    override fun getLastCyclingSession(): Flow<ActivitySession?> {
        return activitySessionDao.getLastCyclingSession()
            .map { entity -> entity?.toDomain() }
    }
    
    override fun getSessionsByType(activityType: String): Flow<List<ActivitySession>> {
        return activitySessionDao.getSessionsByType(activityType)
            .map { entities -> entities.toDomain() }
    }
    
    override suspend fun insertSession(session: ActivitySession): Long {
        return activitySessionDao.insertSession(session.toEntity())
    }
    
    override suspend fun updateSession(session: ActivitySession) {
        activitySessionDao.updateSession(session.toEntity())
    }
    
    override suspend fun deleteSession(id: Long) {
        activitySessionDao.deleteSession(id)
    }
}
