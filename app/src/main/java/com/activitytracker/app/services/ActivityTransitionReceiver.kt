package com.activitytracker.app.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity

/**
 * BroadcastReceiver that handles activity transition events from Google Activity Recognition API.
 * Forwards detected activities to ActivityRecognitionService for processing.
 */
class ActivityTransitionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ActivityTransition"
        const val ACTION_ACTIVITY_TRANSITION = "com.activitytracker.app.ACTIVITY_TRANSITION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let { transitionResult ->
                for (event in transitionResult.transitionEvents) {
                    val activityType = event.activityType
                    val transitionType = event.transitionType
                    
                    Log.d(TAG, "Activity transition: ${getActivityString(activityType)} - ${getTransitionString(transitionType)}")
                    
                    // Only handle ENTER transitions (when activity starts)
                    if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                        // Forward to ActivityRecognitionService
                        val serviceIntent = Intent(context, ActivityRecognitionService::class.java).apply {
                            action = ActivityRecognitionService.ACTION_ACTIVITY_DETECTED
                            putExtra(ActivityRecognitionService.EXTRA_ACTIVITY_TYPE, activityType)
                            putExtra(ActivityRecognitionService.EXTRA_CONFIDENCE, 100) // Transitions have high confidence
                        }
                        context.startService(serviceIntent)
                    } else if (transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        // Activity ended - notify service to check for inactivity
                        val serviceIntent = Intent(context, ActivityRecognitionService::class.java).apply {
                            action = ActivityRecognitionService.ACTION_CHECK_INACTIVITY
                        }
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }

    private fun getActivityString(activityType: Int): String {
        return when (activityType) {
            DetectedActivity.ON_BICYCLE -> "CYCLING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            else -> "OTHER ($activityType)"
        }
    }

    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
            else -> "UNKNOWN"
        }
    }
}
