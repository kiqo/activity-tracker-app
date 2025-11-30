package com.activitytracker.app.domain.model

/**
 * Enum representing the types of activities that can be tracked.
 * Maps to Google Activity Recognition API activity types.
 */
enum class ActivityType {
    IN_VEHICLE,    // DetectedActivity.IN_VEHICLE = 0
    CYCLING,       // DetectedActivity.ON_BICYCLE = 1
    ON_FOOT,       // DetectedActivity.ON_FOOT = 2 (generic foot activity)
    STILL,         // DetectedActivity.STILL = 3 (not moving)
    UNKNOWN,       // DetectedActivity.UNKNOWN = 4
    TILTING,       // DetectedActivity.TILTING = 5 (device angle changed)
    WALKING,       // DetectedActivity.WALKING = 7
    RUNNING;       // DetectedActivity.RUNNING = 8
    
    companion object {
        /**
         * Convert string to ActivityType enum.
         */
        fun fromString(value: String): ActivityType {
            return valueOf(value)
        }
        
        /**
         * Check if this activity type should be tracked with GPS.
         * STILL, UNKNOWN, and TILTING are not real movement activities.
         */
        fun ActivityType.shouldTrackLocation(): Boolean {
            return when (this) {
                STILL, UNKNOWN, TILTING -> false
                else -> true
            }
        }
        
        /**
         * Check if this activity type should count steps.
         */
        fun ActivityType.shouldCountSteps(): Boolean {
            return when (this) {
                WALKING, RUNNING, ON_FOOT -> true
                else -> false
            }
        }
    }
}
