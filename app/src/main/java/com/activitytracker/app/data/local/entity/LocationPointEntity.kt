package com.activitytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a GPS location point during an activity session.
 * Foreign key relationship ensures cascade deletion when parent session is deleted.
 */
@Entity(
    tableName = "location_points",
    foreignKeys = [
        ForeignKey(
            entity = ActivitySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])] // Index for efficient session-based queries
)
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val sessionId: Long, // Foreign key to ActivitySessionEntity
    val latitude: Double, // Latitude in degrees
    val longitude: Double, // Longitude in degrees
    val altitude: Double?, // Altitude in meters, nullable if unavailable
    val accuracy: Float, // Accuracy in meters
    val timestamp: Long // Unix timestamp in milliseconds when location was recorded
)
