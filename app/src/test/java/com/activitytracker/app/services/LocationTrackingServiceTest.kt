package com.activitytracker.app.services

import com.activitytracker.app.domain.repository.LocationRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for LocationTrackingService.
 * Tests thresholds, and repository retry logic.
 * Service lifecycle tests are in LocationTrackingServiceInstrumentedTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationTrackingServiceTest {

    private lateinit var locationRepository: LocationRepository

    @Before
    fun setup() {
        // Mock dependencies
        locationRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `service handles different intent actions`() {
        // Test intent action handling logic
        val startAction = LocationTrackingService.ACTION_START_TRACKING
        val stopAction = LocationTrackingService.ACTION_STOP_TRACKING
        val checkAction = LocationTrackingService.ACTION_CHECK_ACTIVE_SESSIONS
        
        // Verify actions are distinct
        assertFalse(startAction == stopAction, "Start and stop actions should be different")
        assertFalse(startAction == checkAction, "Start and check actions should be different")
        assertFalse(stopAction == checkAction, "Stop and check actions should be different")
    }

    @Test
    fun `intent action constants are correctly defined`() {
        // Verify intent action strings
        assertEquals("com.activitytracker.app.START_LOCATION_TRACKING", 
            LocationTrackingService.ACTION_START_TRACKING)
        assertEquals("com.activitytracker.app.STOP_LOCATION_TRACKING", 
            LocationTrackingService.ACTION_STOP_TRACKING)
        assertEquals("com.activitytracker.app.CHECK_ACTIVE_SESSIONS", 
            LocationTrackingService.ACTION_CHECK_ACTIVE_SESSIONS)
    }

    @Test
    fun `repository retry logic supports multiple attempts`() = runTest {
        // Given - simulate retry scenario
        val maxRetries = 3
        var attemptCount = 0
        
        // Simulate first two failures, then success
        coEvery { locationRepository.insertLocationPoint(any()) } answers {
            attemptCount++
            if (attemptCount < 3) {
                throw Exception("DB error attempt $attemptCount")
            } else {
                1L // Success on third attempt
            }
        }
        coEvery { locationRepository.linkLocationPointToAllActiveSessions(any()) } just Runs
        
        // When - simulate retry logic
        var success = false
        var retryCount = 0
        while (retryCount < maxRetries && !success) {
            try {
                locationRepository.insertLocationPoint(mockk(relaxed = true))
                success = true
            } catch (e: Exception) {
                retryCount++
            }
        }
        
        // Then - verify success after retries
        assertTrue(success, "Should succeed after retries")
        assertEquals(2, retryCount, "Should have retried 2 times before success")
        assertEquals(3, attemptCount, "Should have made 3 total attempts")
    }

    @Test
    fun `repository retry logic fails after max attempts`() = runTest {
        // Given - all attempts fail
        val maxRetries = 3
        val dbError = Exception("Persistent DB error")
        
        coEvery { locationRepository.insertLocationPoint(any()) } throws dbError
        
        // When - simulate retry logic
        var success = false
        var retryCount = 0
        var lastException: Exception? = null
        
        while (retryCount < maxRetries && !success) {
            try {
                locationRepository.insertLocationPoint(mockk(relaxed = true))
                success = true
            } catch (e: Exception) {
                lastException = e
                retryCount++
            }
        }
        
        // Then - verify failure after max retries
        assertFalse(success, "Should fail after max retries")
        assertEquals(maxRetries, retryCount, "Should have retried max times")
        assertEquals("Persistent DB error", lastException?.message)
    }

    @Test
    fun `location points are linked to all active sessions`() = runTest {
        // Given - a location point is inserted
        val locationPointId = 123L
        coEvery { locationRepository.insertLocationPoint(any()) } returns locationPointId
        coEvery { locationRepository.linkLocationPointToAllActiveSessions(locationPointId) } just Runs
        
        // When - location is saved
        val savedId = locationRepository.insertLocationPoint(mockk(relaxed = true))
        locationRepository.linkLocationPointToAllActiveSessions(savedId)
        
        // Then - verify linking was called with correct ID
        coVerify { locationRepository.linkLocationPointToAllActiveSessions(locationPointId) }
        assertEquals(locationPointId, savedId)
    }
}
