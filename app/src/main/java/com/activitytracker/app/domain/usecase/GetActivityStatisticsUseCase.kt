package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.ActivityStatistics
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.model.TimeInterval
import com.activitytracker.app.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case for calculating aggregate statistics for a time interval.
 */
class GetActivityStatisticsUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {
    /**
     * Get activity statistics for a specific time interval.
     * @param timeInterval The time interval (DAILY, WEEKLY, MONTHLY)
     * @return ActivityStatistics with aggregated data
     */
    suspend operator fun invoke(timeInterval: TimeInterval): ActivityStatistics {
        val (startTime, endTime) = getTimeRange(timeInterval)
        
        // Get all sessions in the time range
        val sessions = activityRepository.getSessionsInTimeRange(startTime, endTime).first()
        
        // Aggregate statistics by activity type
        var walkingDistanceMeters = 0.0
        var cyclingDistanceMeters = 0.0
        var runningDistanceMeters = 0.0
        var totalSteps = 0
        
        sessions.forEach { session ->
            when (session.activityType) {
                ActivityType.WALKING -> {
                    walkingDistanceMeters += session.totalDistance
                    totalSteps += session.stepCount
                }
                ActivityType.CYCLING -> {
                    cyclingDistanceMeters += session.totalDistance
                }
                ActivityType.RUNNING -> {
                    runningDistanceMeters += session.totalDistance
                    totalSteps += session.stepCount
                }
                ActivityType.IN_VEHICLE -> {
                    // Vehicle activities not included in statistics
                }
            }
        }
        
        // Convert meters to kilometers
        return ActivityStatistics(
            walkingDistanceKm = walkingDistanceMeters / 1000.0,
            cyclingDistanceKm = cyclingDistanceMeters / 1000.0,
            runningDistanceKm = runningDistanceMeters / 1000.0,
            totalSteps = totalSteps,
            timeInterval = timeInterval
        )
    }
    
    /**
     * Calculate start and end timestamps for a time interval.
     * @return Pair of (startTime, endTime) in milliseconds
     */
    private fun getTimeRange(timeInterval: TimeInterval): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        
        when (timeInterval) {
            TimeInterval.DAILY -> {
                // Start of today
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeInterval.WEEKLY -> {
                // Start of this week (Monday)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            TimeInterval.MONTHLY -> {
                // Start of this month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }
        
        val startTime = calendar.timeInMillis
        return Pair(startTime, endTime)
    }
}
