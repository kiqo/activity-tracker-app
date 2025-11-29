package com.activitytracker.app.domain.usecase

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.services.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for starting a new activity tracking session.
 */
class StartActivityTrackingUseCase @Inject constructor(
    private val activityRepository: ActivityRepository,
    @ApplicationContext private val context: Context
) {
    /**
     * Start a new activity session and begin location tracking.
     * @param activityType The type of activity to track
     * @return The ID of the newly created session
     */
    suspend operator fun invoke(activityType: ActivityType): Long {
        val session = ActivitySession(
            activityType = activityType,
            startTime = System.currentTimeMillis(),
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0
        )
        val sessionId = activityRepository.insertSession(session)
        
        // Start LocationTrackingService
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
            putExtra(LocationTrackingService.EXTRA_SESSION_ID, sessionId)
        }
        ContextCompat.startForegroundService(context, intent)
        
        return sessionId
    }
}
