package com.activitytracker.app.data.mapper

import com.activitytracker.app.data.local.entity.LocationPointEntity
import com.activitytracker.app.domain.model.LocationPoint

/**
 * Convert LocationPointEntity to domain model LocationPoint.
 */
fun LocationPointEntity.toDomain(): LocationPoint {
    return LocationPoint(
        id = id,
        sessionId = sessionId,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        timestamp = timestamp
    )
}

/**
 * Convert domain model LocationPoint to LocationPointEntity.
 */
fun LocationPoint.toEntity(): LocationPointEntity {
    return LocationPointEntity(
        id = id,
        sessionId = sessionId,
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        timestamp = timestamp
    )
}

/**
 * Convert list of LocationPointEntity to list of domain models.
 */
fun List<LocationPointEntity>.toDomain(): List<LocationPoint> {
    return map { it.toDomain() }
}

/**
 * Convert list of domain LocationPoint to list of entities.
 */
fun List<LocationPoint>.toEntity(): List<LocationPointEntity> {
    return map { it.toEntity() }
}
