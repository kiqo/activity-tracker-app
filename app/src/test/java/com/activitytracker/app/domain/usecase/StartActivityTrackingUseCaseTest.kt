package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.repository.ActivityRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

/**
 * Unit tests for StartActivityTrackingUseCase.
 */
class StartActivityTrackingUseCaseTest {

    @Mock
    private lateinit var activityRepository: ActivityRepository

    private lateinit var startActivityTrackingUseCase: StartActivityTrackingUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        startActivityTrackingUseCase = StartActivityTrackingUseCase(activityRepository)
    }

    @Test
    fun `invoke creates new session with correct activity type`() = runTest {
        // Given
        val activityType = ActivityType.CYCLING
        `when`(activityRepository.insertSession(any())).thenReturn(42L)

        // When
        val sessionId = startActivityTrackingUseCase(activityType)

        // Then
        assertEquals(42L, sessionId)
        
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        
        val capturedSession = captor.firstValue
        assertEquals(ActivityType.CYCLING, capturedSession.activityType)
        assertEquals(null, capturedSession.endTime)
        assertEquals(0.0, capturedSession.totalDistance, 0.01)
        assertEquals(0.0, capturedSession.averageSpeed, 0.01)
        assertEquals(0, capturedSession.stepCount)
    }

    @Test
    fun `invoke creates session for all activity types`() = runTest {
        val activityTypes = listOf(
            ActivityType.CYCLING,
            ActivityType.RUNNING,
            ActivityType.WALKING,
            ActivityType.IN_VEHICLE
        )

        activityTypes.forEach { activityType ->
            // Given
            `when`(activityRepository.insertSession(any())).thenReturn(1L)

            // When
            startActivityTrackingUseCase(activityType)

            // Then
            val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
            verify(activityRepository).insertSession(captor.capture())
            assertEquals(activityType, captor.lastValue.activityType)
        }
    }

    @Test
    fun `invoke sets startTime to current time`() = runTest {
        // Given
        val beforeTime = System.currentTimeMillis()
        `when`(activityRepository.insertSession(any())).thenReturn(1L)

        // When
        startActivityTrackingUseCase(ActivityType.RUNNING)
        val afterTime = System.currentTimeMillis()

        // Then
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        
        val startTime = captor.firstValue.startTime
        assert(startTime >= beforeTime && startTime <= afterTime)
    }
}
