package com.activitytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table entity for many-to-many relationship between sessions and location points.
 * 
 * This design allows:
 * - Each location point to be stored ONCE (no duplicate GPS data)
 * - Each location point to be linked to multiple sessions
 * - When both manual and automatic sessions are active, location points are shared
 * 
 * Implements Requirements 2.1, 2.2, 2.6 for shared location tracking.
 */
@Entity(
    tableName = "session_location_points",
    primaryKeys = ["sessionId", "locationPointId"],
    foreignKeys = [
        ForeignKey(
            entity = ActivitySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LocationPointEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationPointId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["locationPointId"])
    ]
)
data class SessionLocationPointEntity(
    val sessionId: Long,
    val locationPointId: Long
)
