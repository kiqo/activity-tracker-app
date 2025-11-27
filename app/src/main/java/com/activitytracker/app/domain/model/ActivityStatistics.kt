package com.activitytracker.app.domain.model

/**
 * Domain model representing aggregate statistics for activities within a time interval.
 */
data class ActivityStatistics(
    val walkingDistanceKm: Double,
    val cyclingDistanceKm: Double,
    val runningDistanceKm: Double,
    val totalSteps: Int,
    val walkingCount: Int,
    val cyclingCount: Int,
    val runningCount: Int,
    val vehicleCount: Int,
    val timeInterval: TimeInterval
)
