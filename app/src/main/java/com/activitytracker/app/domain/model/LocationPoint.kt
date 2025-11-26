package com.activitytracker.app.domain.model

/**
 * Domain model representing a GPS location point during an activity session.
 */
data class LocationPoint(
    val id: Long = 0,
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float, // Accuracy in meters
    val timestamp: Long // Unix timestamp in milliseconds
)
