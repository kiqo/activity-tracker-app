package com.activitytracker.app.domain.model

/**
 * Enum representing the types of activities that can be tracked.
 */
enum class ActivityType {
    CYCLING,
    RUNNING,
    WALKING,
    IN_VEHICLE;
    
    companion object {
        /**
         * Convert string to ActivityType enum.
         */
        fun fromString(value: String): ActivityType {
            return valueOf(value)
        }
    }
}
