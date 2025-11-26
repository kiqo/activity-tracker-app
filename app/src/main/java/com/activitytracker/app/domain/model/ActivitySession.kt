package com.activitytracker.app.domain.model

/**
 * Domain model representing an activity tracking session.
 */
data class ActivitySession(
    val id: Long = 0,
    val activityType: ActivityType,
    val startTime: Long, // Unix timestamp in milliseconds
    val endTime: Long?, // Unix timestamp in milliseconds, null if session is active
    val totalDistance: Double = 0.0, // Total distance in meters
    val averageSpeed: Double = 0.0, // Average speed in meters per second
    val stepCount: Int = 0 // Estimated step count for walking/running
)
