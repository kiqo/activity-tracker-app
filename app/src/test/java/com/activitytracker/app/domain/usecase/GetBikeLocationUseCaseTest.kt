package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for GetBikeLocationUseCase.
 */
class GetBikeLocationUseCaseTest {

    @Mock
    private lateinit var activityRepository: ActivityRepository

    @Mock
    private lateinit var locationRepository: LocationRepository

    private lateinit var getBikeLocationUseCase: GetBikeLocationUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getBikeLocationUseCase = GetBikeLocationUseCase(activityRepository, locationRepository)
    }

    @Test
    fun `invoke returns last location of most recent cycling session`() = runTest {
        // Given
        val cyclingSession = ActivitySession(
            id = 5L,
            activityType = ActivityType.CYCLING,
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 5000.0,
            averageSpeed = 5.5,
            stepCount = 0
        )
        val lastLocation = LocationPoint(
            id = 10L,
            sessionId = 5L,
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = 50.0,
            accuracy = 10f,
            timestamp = 2000L
        )
        
        `when`(activityRepository.getLastCyclingSession()).thenReturn(flowOf(cyclingSession))
        `when`(locationRepository.getLastLocationForSession(5L)).thenReturn(lastLocation)

        // When
        val result = getBikeLocationUseCase()

        // Then
        assertEquals(lastLocation, result)
        assertEquals(37.7749, result?.latitude)
        assertEquals(-122.4194, result?.longitude)
    }

    @Test
    fun `invoke returns null when no cycling session exists`() = runTest {
        // Given
        `when`(activityRepository.getLastCyclingSession()).thenReturn(flowOf(null))

        // When
        val result = getBikeLocationUseCase()

        // Then
        assertNull(result)
    }

    @Test
    fun `invoke returns null when cycling session has no location points`() = runTest {
        // Given
        val cyclingSession = ActivitySession(
            id = 5L,
            activityType = ActivityType.CYCLING,
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0
        )
        
        `when`(activityRepository.getLastCyclingSession()).thenReturn(flowOf(cyclingSession))
        `when`(locationRepository.getLastLocationForSession(5L)).thenReturn(null)

        // When
        val result = getBikeLocationUseCase()

        // Then
        assertNull(result)
    }

    @Test
    fun `invoke uses most recent cycling session when multiple exist`() = runTest {
        // Given - repository should return the most recent one
        val mostRecentSession = ActivitySession(
            id = 10L,
            activityType = ActivityType.CYCLING,
            startTime = 5000L,
            endTime = 6000L,
            totalDistance = 8000.0,
            averageSpeed = 6.0,
            stepCount = 0
        )
        val lastLocation = LocationPoint(
            id = 20L,
            sessionId = 10L,
            latitude = 34.0522,
            longitude = -118.2437,
            altitude = 100.0,
            accuracy = 12f,
            timestamp = 6000L
        )
        
        `when`(activityRepository.getLastCyclingSession()).thenReturn(flowOf(mostRecentSession))
        `when`(locationRepository.getLastLocationForSession(10L)).thenReturn(lastLocation)

        // When
        val result = getBikeLocationUseCase()

        // Then
        assertEquals(10L, result?.sessionId)
        assertEquals(34.0522, result?.latitude)
    }
}
