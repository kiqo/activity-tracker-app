package com.activitytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a GPS location point.
 * 
 * Location points are stored ONCE and linked to sessions via the SessionLocationPointEntity
 * junction table. This prevents duplicate GPS data when multiple sessions are active.
 * 
 * Implements Requirements 2.1, 2.2, 2.6 for shared location tracking.
 */
@Entity(
    tableName = "location_points",
    indices = [Index(value = ["timestamp"])] // Index for efficient time-based queries
)
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val latitude: Double, // Latitude in degrees
    val longitude: Double, // Longitude in degrees
    val altitude: Double?, // Altitude in meters, nullable if unavailable
    val accuracy: Float, // Accuracy in meters
    val timestamp: Long // Unix timestamp in milliseconds when location was recorded
)
