package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.model.TimeInterval
import com.activitytracker.app.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for GetActivityStatisticsUseCase.
 * Tests statistics aggregation calculations.
 */
class GetActivityStatisticsUseCaseTest {

    @Mock
    private lateinit var activityRepository: ActivityRepository

    private lateinit var getActivityStatisticsUseCase: GetActivityStatisticsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getActivityStatisticsUseCase = GetActivityStatisticsUseCase(activityRepository)
    }

    @Test
    fun `calculate statistics with no sessions returns zeros`() = runTest {
        // Given
        `when`(activityRepository.getSessionsInTimeRange(anyLong(), anyLong()))
            .thenReturn(flowOf(emptyList()))

        // When
        val statistics = getActivityStatisticsUseCase(TimeInterval.DAILY)

        // Then
        assertEquals(0.0, statistics.walkingDistanceKm, 0.01)
        assertEquals(0.0, statistics.cyclingDistanceKm, 0.01)
        assertEquals(0.0, statistics.runningDistanceKm, 0.01)
        assertEquals(0, statistics.totalSteps)
    }

    @Test
    fun `calculate statistics aggregates walking distance correctly`() = runTest {
        // Given - two walking sessions
        val sessions = listOf(
            ActivitySession(
                id = 1,
                activityType = ActivityType.WALKING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 1000.0, // 1 km
                averageSpeed = 1.4,
                stepCount = 1312
            ),
            ActivitySession(
                id = 2,
                activityType = ActivityType.WALKING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 2000.0, // 2 km
                averageSpeed = 1.4,
                stepCount = 2624
            )
        )
        `when`(activityRepository.getSessionsInTimeRange(anyLong(), anyLong()))
            .thenReturn(flowOf(sessions))

        // When
        val statistics = getActivityStatisticsUseCase(TimeInterval.DAILY)

        // Then - 3 km total
        assertEquals(3.0, statistics.walkingDistanceKm, 0.01)
        assertEquals(3936, statistics.totalSteps) // 1312 + 2624
    }

    @Test
    fun `calculate statistics aggregates cycling distance correctly`() = runTest {
        // Given - two cycling sessions
        val sessions = listOf(
            ActivitySession(
                id = 1,
                activityType = ActivityType.CYCLING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 5000.0, // 5 km
                averageSpeed = 5.0,
                stepCount = 0
            ),
            ActivitySession(
                id = 2,
                activityType = ActivityType.CYCLING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 10000.0, // 10 km
                averageSpeed = 5.5,
                stepCount = 0
            )
        )
        `when`(activityRepository.getSessionsInTimeRange(anyLong(), anyLong()))
            .thenReturn(flowOf(sessions))

        // When
        val statistics = getActivityStatisticsUseCase(TimeInterval.DAILY)

        // Then - 15 km total
        assertEquals(15.0, statistics.cyclingDistanceKm, 0.01)
        assertEquals(0, statistics.totalSteps) // No steps for cycling
    }

    @Test
    fun `calculate statistics aggregates running distance correctly`() = runTest {
        // Given - two running sessions
        val sessions = listOf(
            ActivitySession(
                id = 1,
                activityType = ActivityType.RUNNING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 3000.0, // 3 km
                averageSpeed = 3.0,
                stepCount = 3333
            ),
            ActivitySession(
                id = 2,
                activityType = ActivityType.RUNNING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 2000.0, // 2 km
                averageSpeed = 2.8,
                stepCount = 2222
            )
        )
        `when`(activityRepository.getSessionsInTimeRange(anyLong(), anyLong()))
            .thenReturn(flowOf(sessions))

        // When
        val statistics = getActivityStatisticsUseCase(TimeInterval.DAILY)

        // Then - 5 km total
        assertEquals(5.0, statistics.runningDistanceKm, 0.01)
        assertEquals(5555, statistics.totalSteps) // 3333 + 2222
    }

    @Test
    fun `calculate statistics with mixed activities aggregates correctly`() = runTest {
        // Given - mixed activity sessions
        val sessions = listOf(
            ActivitySession(
                id = 1,
                activityType = ActivityType.WALKING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 1000.0,
                averageSpeed = 1.4,
                stepCount = 1312
            ),
            ActivitySession(
                id = 2,
                activityType = ActivityType.CYCLING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 5000.0,
                averageSpeed = 5.0,
                stepCount = 0
            ),
            ActivitySession(
                id = 3,
                activityType = ActivityType.RUNNING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 3000.0,
                averageSpeed = 3.0,
                stepCount = 3333
            )
        )
        `when`(activityRepository.getSessionsInTimeRange(anyLong(), anyLong()))
            .thenReturn(flowOf(sessions))

        // When
        val statistics = getActivityStatisticsUseCase(TimeInterval.DAILY)

        // Then
        assertEquals(1.0, statistics.walkingDistanceKm, 0.01)
        assertEquals(5.0, statistics.cyclingDistanceKm, 0.01)
        assertEquals(3.0, statistics.runningDistanceKm, 0.01)
        assertEquals(4645, statistics.totalSteps) // 1312 + 3333
    }

    @Test
    fun `calculate statistics ignores vehicle activities`() = runTest {
        // Given - vehicle session should not be included
        val sessions = listOf(
            ActivitySession(
                id = 1,
                activityType = ActivityType.IN_VEHICLE,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 10000.0,
                averageSpeed = 15.0,
                stepCount = 0
            )
        )
        `when`(activityRepository.getSessionsInTimeRange(anyLong(), anyLong()))
            .thenReturn(flowOf(sessions))

        // When
        val statistics = getActivityStatisticsUseCase(TimeInterval.DAILY)

        // Then - all zeros
        assertEquals(0.0, statistics.walkingDistanceKm, 0.01)
        assertEquals(0.0, statistics.cyclingDistanceKm, 0.01)
        assertEquals(0.0, statistics.runningDistanceKm, 0.01)
        assertEquals(0, statistics.totalSteps)
    }

    @Test
    fun `calculate statistics converts meters to kilometers correctly`() = runTest {
        // Given - session with 2500 meters
        val sessions = listOf(
            ActivitySession(
                id = 1,
                activityType = ActivityType.WALKING,
                startTime = System.currentTimeMillis(),
                endTime = System.currentTimeMillis(),
                totalDistance = 2500.0,
                averageSpeed = 1.4,
                stepCount = 3280
            )
        )
        `when`(activityRepository.getSessionsInTimeRange(anyLong(), anyLong()))
            .thenReturn(flowOf(sessions))

        // When
        val statistics = getActivityStatisticsUseCase(TimeInterval.DAILY)

        // Then - 2.5 km
        assertEquals(2.5, statistics.walkingDistanceKm, 0.01)
    }
}
