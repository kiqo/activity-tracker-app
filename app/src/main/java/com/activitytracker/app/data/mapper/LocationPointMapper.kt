package com.activitytracker.app.data.mapper

import com.activitytracker.app.data.local.entity.LocationPointEntity
import com.activitytracker.app.domain.model.LocationPoint

/**
 * Convert LocationPointEntity to domain model LocationPoint.
 * Note: sessionId is not stored in entity anymore (uses junction table).
 * The sessionId in domain model is set to 0 as a placeholder.
 */
fun LocationPointEntity.toDomain(): LocationPoint {
    return LocationPoint(
        id = id,
        sessionId = 0, // Not stored in entity; retrieved via junction table
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        accuracy = accuracy,
        timestamp = timestamp
    )
}

/**
 * Convert domain model LocationPoint to LocationPointEntity.
 * Note: sessionId is not stored in entity anymore (uses junction table).
 */
fun LocationPoint.toEntity(): LocationPointEntity {
    return LocationPointEntity(
        id = id,
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
