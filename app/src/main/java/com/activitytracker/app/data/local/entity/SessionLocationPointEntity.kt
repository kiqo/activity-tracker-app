package com.activitytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table entity for many-to-many relationship between sessions and location points.
 * Allows a single location point to be shared across multiple active sessions.
 * 
 * This design enables:
 * - Single location point stored once (no duplication)
 * - Location point linked to all active sessions (manual + automatic)
 * - Cascade deletion when either session or location point is deleted
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
