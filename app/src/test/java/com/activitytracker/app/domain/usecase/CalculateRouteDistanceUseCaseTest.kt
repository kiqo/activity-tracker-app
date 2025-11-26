package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.repository.LocationRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import kotlin.math.abs

/**
 * Unit tests for CalculateRouteDistanceUseCase.
 * Tests Haversine formula distance calculations.
 */
class CalculateRouteDistanceUseCaseTest {

    @Mock
    private lateinit var locationRepository: LocationRepository

    private lateinit var calculateRouteDistanceUseCase: CalculateRouteDistanceUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        calculateRouteDistanceUseCase = CalculateRouteDistanceUseCase(locationRepository)
    }

    @Test
    fun `calculate distance with no points returns zero`() = runTest {
        // Given
        val sessionId = 1L
        `when`(locationRepository.getAccurateLocationPointsForSession(sessionId))
            .thenReturn(flowOf(emptyList()))

        // When
        val distance = calculateRouteDistanceUseCase(sessionId)

        // Then
        assertEquals(0.0, distance, 0.01)
    }

    @Test
    fun `calculate distance with single point returns zero`() = runTest {
        // Given
        val sessionId = 1L
        val points = listOf(
            LocationPoint(1, sessionId, 37.7749, -122.4194, null, 10f, System.currentTimeMillis())
        )
        `when`(locationRepository.getAccurateLocationPointsForSession(sessionId))
            .thenReturn(flowOf(points))

        // When
        val distance = calculateRouteDistanceUseCase(sessionId)

        // Then
        assertEquals(0.0, distance, 0.01)
    }

    @Test
    fun `calculate distance between two points - San Francisco to Los Angeles`() = runTest {
        // Given - approximately 559 km apart
        val sessionId = 1L
        val points = listOf(
            LocationPoint(1, sessionId, 37.7749, -122.4194, null, 10f, System.currentTimeMillis()), // SF
            LocationPoint(2, sessionId, 34.0522, -118.2437, null, 10f, System.currentTimeMillis())  // LA
        )
        `when`(locationRepository.getAccurateLocationPointsForSession(sessionId))
            .thenReturn(flowOf(points))

        // When
        val distance = calculateRouteDistanceUseCase(sessionId)

        // Then - approximately 559,000 meters
        assertEquals(559000.0, distance, 5000.0) // 5km tolerance
    }

    @Test
    fun `calculate distance for short route - 1 km`() = runTest {
        // Given - points approximately 1 km apart
        val sessionId = 1L
        val points = listOf(
            LocationPoint(1, sessionId, 37.7749, -122.4194, null, 10f, System.currentTimeMillis()),
            LocationPoint(2, sessionId, 37.7839, -122.4194, null, 10f, System.currentTimeMillis()) // ~1 km north
        )
        `when`(locationRepository.getAccurateLocationPointsForSession(sessionId))
            .thenReturn(flowOf(points))

        // When
        val distance = calculateRouteDistanceUseCase(sessionId)

        // Then - approximately 1000 meters
        assertEquals(1000.0, distance, 100.0) // 100m tolerance
    }

    @Test
    fun `calculate distance for multiple points accumulates correctly`() = runTest {
        // Given - three points forming a path
        val sessionId = 1L
        val points = listOf(
            LocationPoint(1, sessionId, 37.7749, -122.4194, null, 10f, System.currentTimeMillis()),
            LocationPoint(2, sessionId, 37.7849, -122.4194, null, 10f, System.currentTimeMillis()),
            LocationPoint(3, sessionId, 37.7949, -122.4194, null, 10f, System.currentTimeMillis())
        )
        `when`(locationRepository.getAccurateLocationPointsForSession(sessionId))
            .thenReturn(flowOf(points))

        // When
        val distance = calculateRouteDistanceUseCase(sessionId)

        // Then - should be approximately 2.2 km (two ~1.1km segments)
        assertEquals(2200.0, distance, 300.0)
    }

    @Test
    fun `calculate distance with same coordinates returns zero`() = runTest {
        // Given - same location twice
        val sessionId = 1L
        val points = listOf(
            LocationPoint(1, sessionId, 37.7749, -122.4194, null, 10f, System.currentTimeMillis()),
            LocationPoint(2, sessionId, 37.7749, -122.4194, null, 10f, System.currentTimeMillis())
        )
        `when`(locationRepository.getAccurateLocationPointsForSession(sessionId))
            .thenReturn(flowOf(points))

        // When
        val distance = calculateRouteDistanceUseCase(sessionId)

        // Then
        assertEquals(0.0, distance, 0.01)
    }
}
