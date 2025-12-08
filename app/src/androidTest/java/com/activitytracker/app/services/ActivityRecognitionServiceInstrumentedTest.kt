package com.activitytracker.app.services

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Instrumented tests for ActivityRecognitionService.
 * Tests that the service correctly starts LocationTrackingService when activities are detected.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ActivityRecognitionServiceInstrumentedTest {

    private val SERVICE_START_TIME: Long = 2000L
    private val SERVICE_STOP_TIME: Long = 2000L

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Stop any running services
        stopAllServices()
        runBlocking { delay(1000) }
    }

    @After
    fun tearDown() {
        stopAllServices()
        runBlocking { delay(1000) }
    }

    private fun stopAllServices() {
        // Stop ActivityRecognitionService
        val stopActivityIntent = Intent(context, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.ACTION_STOP_TRACKING
        }
        context.startService(stopActivityIntent)
        
        // Stop LocationTrackingService
        val stopLocationIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(stopLocationIntent)
    }

    @Test
    fun activityRecognitionServiceStartsLocationTrackingWhenActivityDetected() = runBlocking {
        // Given - ActivityRecognitionService is running
        val startIntent = Intent(context, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.ACTION_START_TRACKING
        }
        context.startForegroundService(startIntent)
        delay(SERVICE_START_TIME)

        // Verify ActivityRecognitionService is running
        val isActivityServiceRunning = isServiceRunning(context, ActivityRecognitionService::class.java)
        assertTrue(isActivityServiceRunning, "ActivityRecognitionService should be running")

        // When - simulate activity detected (cycling with high confidence)
        val activityIntent = Intent(context, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.ACTION_ACTIVITY_DETECTED
            putExtra(ActivityRecognitionService.EXTRA_ACTIVITY_TYPE, DetectedActivity.ON_BICYCLE)
            putExtra(ActivityRecognitionService.EXTRA_CONFIDENCE, 90)
        }
        context.startService(activityIntent)
        delay(SERVICE_START_TIME)

        // Then - LocationTrackingService should be started
        val isLocationServiceRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(isLocationServiceRunning, 
            "LocationTrackingService should be started when activity is detected")
    }

    @Test
    fun activityRecognitionServiceDoesNotStartLocationTrackingForLowConfidence() = runBlocking {
        // Given - ActivityRecognitionService is running
        val startIntent = Intent(context, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.ACTION_START_TRACKING
        }
        context.startForegroundService(startIntent)
        delay(SERVICE_START_TIME)

        // When - simulate activity detected with LOW confidence (below 75% threshold)
        val activityIntent = Intent(context, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.ACTION_ACTIVITY_DETECTED
            putExtra(ActivityRecognitionService.EXTRA_ACTIVITY_TYPE, DetectedActivity.ON_BICYCLE)
            putExtra(ActivityRecognitionService.EXTRA_CONFIDENCE, 50) // Below threshold
        }
        context.startService(activityIntent)
        delay(SERVICE_START_TIME)

        // Then - LocationTrackingService should NOT be started
        val isLocationServiceRunning = isServiceRunning(context, LocationTrackingService::class.java)
        // Note: This might be true if already running from previous test
        // In a real scenario, we'd ensure clean state
        assertFalse(isLocationServiceRunning,
            "LocationTrackingService should stop when ActivityRecognitionService stops")
    }

    @Test
    fun locationTrackingServiceStopsWhenActivityRecognitionServiceStops() = runBlocking {
        var isLocationServiceRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertFalse(isLocationServiceRunning)

        // Given - both services are running
        val startActivityIntent = Intent(context, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.ACTION_START_TRACKING
        }
        context.startForegroundService(startActivityIntent)
        delay(SERVICE_START_TIME)

        // Simulate activity detected to start location tracking
        val activityIntent = Intent(context, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.ACTION_ACTIVITY_DETECTED
            putExtra(ActivityRecognitionService.EXTRA_ACTIVITY_TYPE, DetectedActivity.RUNNING)
            putExtra(ActivityRecognitionService.EXTRA_CONFIDENCE, 85)
        }
        context.startService(activityIntent)
        delay(SERVICE_START_TIME)

        // When - stop ActivityRecognitionService
        val stopIntent = Intent(context, ActivityRecognitionService::class.java).apply {
            action = ActivityRecognitionService.ACTION_STOP_TRACKING
        }
        context.startService(stopIntent)
        delay(SERVICE_START_TIME)

        // Then - LocationTrackingService should also stop
        isLocationServiceRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(!isLocationServiceRunning, 
            "LocationTrackingService should stop when ActivityRecognitionService stops")
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}
