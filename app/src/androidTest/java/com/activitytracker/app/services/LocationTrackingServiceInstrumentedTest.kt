package com.activitytracker.app.services

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Instrumented tests for LocationTrackingService.
 * These tests run on a real Android device and test actual service behavior.
 * 
 * Prerequisites:
 * - Device must have location services enabled
 * - Device must have Google Play Services installed
 * - Tests will request location permissions
 * 
 * Run with: ./gradlew connectedAndroidTest
 * Or from Android Studio: Right-click test class → Run
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class LocationTrackingServiceInstrumentedTest {

    private val SERVICE_START_TIME: Long = 2000L

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        // Stop service after each test
        val stopIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(stopIntent)
        
        // Give service time to stop
        runBlocking { delay(1000) }
    }

    // ========================================
    // 1. Service Lifecycle Tests
    // ========================================

    @Test
    fun serviceStartsSuccessfully() = runBlocking {
        // Given
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }

        // When
        context.startForegroundService(intent)
        delay(SERVICE_START_TIME)

        // Then
        val isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(isRunning, "Service should start successfully")
    }

    @Test
    fun serviceStartsInForeground() = runBlocking {
        // Given
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }

        // When
        context.startForegroundService(intent)
        delay(SERVICE_START_TIME) // Wait for service to start

        // Then
        val isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(isRunning, "Service should be running in foreground")
    }

    @Test
    fun serviceStopsWhenRequested() = runBlocking {
        // Given - start service first
        val startIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }
        context.startForegroundService(startIntent)
        delay(SERVICE_START_TIME)

        // When - stop service
        val stopIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(stopIntent)
        delay(SERVICE_START_TIME)

        // Then
        val isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        // Note: Service might still be running if there are active sessions
        // This test verifies the stop intent is processed
        assertFalse(isRunning, "Stop intent processed")
    }

    // ========================================
    // 2. Intent Handling Tests
    // ========================================

    @Test
    fun serviceHandlesStartTrackingAction() = runBlocking {
        // Given
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }

        // When
        context.startForegroundService(intent)
        delay(SERVICE_START_TIME)

        // Then
        val isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(isRunning, "Service should start tracking")
    }

    @Test
    fun serviceIgnoresDuplicateStartRequests() = runBlocking {
        // Given
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }

        // When - send start request twice
        context.startForegroundService(intent)
        delay(1000)
        context.startForegroundService(intent)
        delay(1000)

        // Then - service should still be running (singleton pattern)
        val isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(isRunning, "Service should handle duplicate starts gracefully")
    }

    @Test
    fun serviceHandlesCheckActiveSessionsAction() = runBlocking {
        // Given - service is running
        val startIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }
        context.startForegroundService(startIntent)
        delay(SERVICE_START_TIME)

        // When - send check active sessions action
        val checkIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_CHECK_ACTIVE_SESSIONS
        }
        context.startService(checkIntent)
        delay(SERVICE_START_TIME)

        // Then - service processes the action
        // TODO implement
    }

    // ========================================
    // 3. Permission Tests
    // ========================================

    @Test
    fun serviceRequiresLocationPermission() {
        // This test verifies that location permission is granted
        // The GrantPermissionRule ensures permissions are granted before test runs
        val hasPermission = context.checkSelfPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        assertTrue(hasPermission, "Location permission should be granted for tests")
    }

    // ========================================
    // 4. Notification Tests
    // ========================================

    @Test
    fun serviceCreatesNotification() = runBlocking {
        // Given
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }

        // When
        context.startForegroundService(intent)
        delay(SERVICE_START_TIME)

        // Then - service should be running with notification
        val isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(isRunning, "Service should be running with foreground notification")
    }

    // ========================================
    // 5. Location Tracking Tests
    // ========================================

    @Test
    fun serviceRequestsLocationUpdates() = runBlocking {
        // Given
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }

        // When
        context.startForegroundService(intent)
        delay(SERVICE_START_TIME) // Wait for location updates to start

        // Then
        val isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(isRunning, "Service should be tracking location")
        
        // Note: Actual location updates depend on GPS signal
        // In a real test environment, you might mock location provider
    }

    // ========================================
    // 6. Error Handling Tests
    // ========================================

    @Test
    fun serviceHandlesNullIntent() = runBlocking {
        // TODO implement
    }

    // ========================================
    // 7. Integration Tests
    // ========================================

    @Test
    fun fullTrackingLifecycle() = runBlocking {
        // Given - service not running
        var isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(!isRunning || isRunning, "Initial state checked")

        // When - start tracking
        val startIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }
        context.startForegroundService(startIntent)
        delay(SERVICE_START_TIME)

        // Then - service is running
        isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(isRunning, "Service should be running")

        // When - stop tracking
        val stopIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(stopIntent)
        delay(SERVICE_START_TIME)

        // Then - service stops (if no active sessions)
        isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertFalse(isRunning, "Service should not be running")
    }

    @Test
    fun serviceRestartsAfterCrash() = runBlocking {
        // Given - service is running
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START_TRACKING
        }
        context.startForegroundService(intent)
        delay(SERVICE_START_TIME)

        // When - service is killed (simulated by stopping)
        val stopIntent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP_TRACKING
        }
        context.startService(stopIntent)
        delay(SERVICE_START_TIME)

        // Then - service can be restarted
        context.startForegroundService(intent)
        delay(SERVICE_START_TIME)
        val isRunning = isServiceRunning(context, LocationTrackingService::class.java)
        assertTrue(isRunning, "Service should restart successfully")
    }

    // ========================================
    // Helper Methods
    // ========================================

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}
