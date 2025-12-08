package com.activitytracker.app.domain.usecase

import android.content.Context
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.util.Logger
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

/**
 * Unit tests for StopActivityTrackingUseCase.
 */
class StopActivityTrackingUseCaseTest {

    @Mock
    private lateinit var activityRepository: ActivityRepository

    @Mock
    private lateinit var calculateRouteDistanceUseCase: CalculateRouteDistanceUseCase

    @Mock
    private lateinit var estimateStepCountUseCase: EstimateStepCountUseCase
    
    @Mock
    private lateinit var logger: Logger

    @Mock
    private lateinit var context: Context

    private lateinit var stopActivityTrackingUseCase: StopActivityTrackingUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        stopActivityTrackingUseCase = StopActivityTrackingUseCase(
            activityRepository,
            calculateRouteDistanceUseCase,
            estimateStepCountUseCase,
            logger,
            context
        )
    }

    @Test
    fun `invoke stops manually started cycling session`() = runTest {
        // Given
        val sessionId = 1L
        val session = ActivitySession(
            id = sessionId,
            activityType = ActivityType.CYCLING,
            startTime = 1000L,
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = true
        )
        
        `when`(activityRepository.getSessionById(sessionId)).thenReturn(flowOf(session))
        `when`(calculateRouteDistanceUseCase(sessionId)).thenReturn(5000.0)
        `when`(estimateStepCountUseCase(any(), any())).thenReturn(0)

        // When
        stopActivityTrackingUseCase(sessionId)

        // Then
        val captor = argumentCaptor<ActivitySession>()
        verify(activityRepository).updateSession(captor.capture())
        
        val updatedSession = captor.firstValue
        assertEquals(ActivityType.CYCLING, updatedSession.activityType)
        assertEquals(true, updatedSession.isManuallyStarted)
        assertNotNull(updatedSession.endTime)
        assertEquals(5000.0, updatedSession.totalDistance, 0.01)
    }

    @Test
    fun `invoke stops auto-detected running session`() = runTest {
        // Given
        val sessionId = 2L
        val session = ActivitySession(
            id = sessionId,
            activityType = ActivityType.RUNNING,
            startTime = 2000L,
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = false
        )
        
        `when`(activityRepository.getSessionById(sessionId)).thenReturn(flowOf(session))
        `when`(calculateRouteDistanceUseCase(sessionId)).thenReturn(3000.0)
        `when`(estimateStepCountUseCase(3000.0, ActivityType.RUNNING)).thenReturn(4000)

        // When
        stopActivityTrackingUseCase(sessionId)

        // Then
        val captor = argumentCaptor<ActivitySession>()
        verify(activityRepository).updateSession(captor.capture())
        
        val updatedSession = captor.firstValue
        assertEquals(ActivityType.RUNNING, updatedSession.activityType)
        assertEquals(false, updatedSession.isManuallyStarted)
        assertNotNull(updatedSession.endTime)
        assertEquals(3000.0, updatedSession.totalDistance, 0.01)
        assertEquals(4000, updatedSession.stepCount)
    }

    @Test
    fun `invoke stops auto-detected walking session`() = runTest {
        // Given
        val sessionId = 3L
        val session = ActivitySession(
            id = sessionId,
            activityType = ActivityType.WALKING,
            startTime = 3000L,
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = false
        )
        
        `when`(activityRepository.getSessionById(sessionId)).thenReturn(flowOf(session))
        `when`(calculateRouteDistanceUseCase(sessionId)).thenReturn(2000.0)
        `when`(estimateStepCountUseCase(2000.0, ActivityType.WALKING)).thenReturn(2500)

        // When
        stopActivityTrackingUseCase(sessionId)

        // Then
        val captor = argumentCaptor<ActivitySession>()
        verify(activityRepository).updateSession(captor.capture())
        
        val updatedSession = captor.firstValue
        assertEquals(ActivityType.WALKING, updatedSession.activityType)
        assertEquals(false, updatedSession.isManuallyStarted)
        assertNotNull(updatedSession.endTime)
        assertEquals(2000.0, updatedSession.totalDistance, 0.01)
        assertEquals(2500, updatedSession.stepCount)
    }

    @Test
    fun `invoke stops auto-detected vehicle session`() = runTest {
        // Given
        val sessionId = 4L
        val session = ActivitySession(
            id = sessionId,
            activityType = ActivityType.IN_VEHICLE,
            startTime = 4000L,
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = false
        )
        
        `when`(activityRepository.getSessionById(sessionId)).thenReturn(flowOf(session))
        `when`(calculateRouteDistanceUseCase(sessionId)).thenReturn(10000.0)
        `when`(estimateStepCountUseCase(10000.0, ActivityType.IN_VEHICLE)).thenReturn(0)

        // When
        stopActivityTrackingUseCase(sessionId)

        // Then
        val captor = argumentCaptor<ActivitySession>()
        verify(activityRepository).updateSession(captor.capture())
        
        val updatedSession = captor.firstValue
        assertEquals(ActivityType.IN_VEHICLE, updatedSession.activityType)
        assertEquals(false, updatedSession.isManuallyStarted)
        assertNotNull(updatedSession.endTime)
        assertEquals(10000.0, updatedSession.totalDistance, 0.01)
        assertEquals(0, updatedSession.stepCount)
    }

    @Test
    fun `invoke calculates correct average speed for manually started session`() = runTest {
        // Given
        val sessionId = 5L
        val startTime = System.currentTimeMillis() - 10000L // 10 seconds ago
        val session = ActivitySession(
            id = sessionId,
            activityType = ActivityType.CYCLING,
            startTime = startTime,
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = true
        )
        
        `when`(activityRepository.getSessionById(sessionId)).thenReturn(flowOf(session))
        `when`(calculateRouteDistanceUseCase(sessionId)).thenReturn(100.0) // 100 meters
        `when`(estimateStepCountUseCase(any(), any())).thenReturn(0)

        // When
        stopActivityTrackingUseCase(sessionId)

        // Then
        val captor = argumentCaptor<ActivitySession>()
        verify(activityRepository).updateSession(captor.capture())
        
        val updatedSession = captor.firstValue
        assertEquals(true, updatedSession.isManuallyStarted)
        // Average speed should be approximately 10 m/s (100m / 10s)
        assert(updatedSession.averageSpeed > 8.0 && updatedSession.averageSpeed < 12.0)
    }

    @Test
    fun `invoke calculates correct average speed for auto-detected session`() = runTest {
        // Given
        val sessionId = 6L
        val startTime = System.currentTimeMillis() - 20000L // 20 seconds ago
        val session = ActivitySession(
            id = sessionId,
            activityType = ActivityType.RUNNING,
            startTime = startTime,
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = false
        )
        
        `when`(activityRepository.getSessionById(sessionId)).thenReturn(flowOf(session))
        `when`(calculateRouteDistanceUseCase(sessionId)).thenReturn(100.0) // 100 meters
        `when`(estimateStepCountUseCase(any(), any())).thenReturn(150)

        // When
        stopActivityTrackingUseCase(sessionId)

        // Then
        val captor = argumentCaptor<ActivitySession>()
        verify(activityRepository).updateSession(captor.capture())
        
        val updatedSession = captor.firstValue
        assertEquals(false, updatedSession.isManuallyStarted)
        // Average speed should be approximately 5 m/s (100m / 20s)
        assert(updatedSession.averageSpeed > 4.0 && updatedSession.averageSpeed < 6.0)
    }

    @Test
    fun `invoke handles zero duration session`() = runTest {
        // Given
        val sessionId = 7L
        val currentTime = System.currentTimeMillis()
        val session = ActivitySession(
            id = sessionId,
            activityType = ActivityType.WALKING,
            startTime = currentTime,
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = false
        )
        
        `when`(activityRepository.getSessionById(sessionId)).thenReturn(flowOf(session))
        `when`(calculateRouteDistanceUseCase(sessionId)).thenReturn(0.0)
        `when`(estimateStepCountUseCase(any(), any())).thenReturn(0)

        // When
        stopActivityTrackingUseCase(sessionId)

        // Then
        val captor = argumentCaptor<ActivitySession>()
        verify(activityRepository).updateSession(captor.capture())
        
        val updatedSession = captor.firstValue
        assertEquals(0.0, updatedSession.averageSpeed, 0.01)
    }
}
