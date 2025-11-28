package com.activitytracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing an activity tracking session.
 * Stores metadata about a continuous period of a single activity type.
 */
@Entity(
    tableName = "activity_sessions",
    indices = [
        androidx.room.Index(value = ["startTime"], name = "index_activity_sessions_startTime"),
        androidx.room.Index(value = ["activityType"], name = "index_activity_sessions_activityType")
    ]
)
data class ActivitySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val activityType: String, // CYCLING, RUNNING, WALKING, IN_VEHICLE
    val startTime: Long, // Unix timestamp in milliseconds
    val endTime: Long?, // Unix timestamp in milliseconds, null if session is active
    val totalDistance: Double = 0.0, // Total distance in meters
    val averageSpeed: Double = 0.0, // Average speed in meters per second
    val stepCount: Int = 0 // Estimated step count for walking/running
)
