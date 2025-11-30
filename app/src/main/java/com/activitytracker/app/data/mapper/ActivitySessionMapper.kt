package com.activitytracker.app.data.mapper

import com.activitytracker.app.data.local.entity.ActivitySessionEntity
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType

/**
 * Convert ActivitySessionEntity to domain model ActivitySession.
 */
fun ActivitySessionEntity.toDomain(): ActivitySession {
    return ActivitySession(
        id = id,
        activityType = ActivityType.fromString(activityType),
        startTime = startTime,
        endTime = endTime,
        totalDistance = totalDistance,
        averageSpeed = averageSpeed,
        stepCount = stepCount,
        isManuallyStarted = isManuallyStarted
    )
}

/**
 * Convert domain model ActivitySession to ActivitySessionEntity.
 */
fun ActivitySession.toEntity(): ActivitySessionEntity {
    return ActivitySessionEntity(
        id = id,
        activityType = activityType.name,
        startTime = startTime,
        endTime = endTime,
        totalDistance = totalDistance,
        averageSpeed = averageSpeed,
        stepCount = stepCount,
        isManuallyStarted = isManuallyStarted
    )
}

/**
 * Convert list of ActivitySessionEntity to list of domain models.
 */
fun List<ActivitySessionEntity>.toDomain(): List<ActivitySession> {
    return map { it.toDomain() }
}
