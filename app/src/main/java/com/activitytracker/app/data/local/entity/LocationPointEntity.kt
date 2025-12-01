package com.activitytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a GPS location point.
 * Location points are now shared across multiple sessions via SessionLocationPointEntity junction table.
 * This allows a single location point to be associated with multiple active sessions (manual + automatic).
 */
@Entity(tableName = "location_points")
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val latitude: Double, // Latitude in degrees
    val longitude: Double, // Longitude in degrees
    val altitude: Double?, // Altitude in meters, nullable if unavailable
    val accuracy: Float, // Accuracy in meters
    val timestamp: Long // Unix timestamp in milliseconds when location was recorded
)
