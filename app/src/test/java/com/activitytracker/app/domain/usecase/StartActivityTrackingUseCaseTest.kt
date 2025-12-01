package com.activitytracker.app.domain.usecase

import android.content.Context
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

    @Mock
    private lateinit var stopActivityTrackingUseCase: StopActivityTrackingUseCase

    @Mock
    private lateinit var context: Context

    private lateinit var startActivityTrackingUseCase: StartActivityTrackingUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        startActivityTrackingUseCase = StartActivityTrackingUseCase(activityRepository, stopActivityTrackingUseCase, context)
    }

    @Test
    fun `invoke creates new session with correct activity type`() = runTest {
        // Given
        val activityType = ActivityType.CYCLING
        `when`(activityRepository.getActiveManualSession()).thenReturn(null)
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
    fun `invoke creates session for cycling`() = runTest {
        // Given
        `when`(activityRepository.getActiveManualSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(1L)

        // When
        startActivityTrackingUseCase(ActivityType.CYCLING)

        // Then
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        assertEquals(ActivityType.CYCLING, captor.firstValue.activityType)
    }

    @Test
    fun `invoke creates session for running`() = runTest {
        // Given
        `when`(activityRepository.getActiveManualSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(1L)

        // When
        startActivityTrackingUseCase(ActivityType.RUNNING)

        // Then
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        assertEquals(ActivityType.RUNNING, captor.firstValue.activityType)
    }

    @Test
    fun `invoke creates session for walking`() = runTest {
        // Given
        `when`(activityRepository.getActiveManualSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(1L)

        // When
        startActivityTrackingUseCase(ActivityType.WALKING)

        // Then
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        assertEquals(ActivityType.WALKING, captor.firstValue.activityType)
    }

    @Test
    fun `invoke creates session for vehicle`() = runTest {
        // Given
        `when`(activityRepository.getActiveManualSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(1L)

        // When
        startActivityTrackingUseCase(ActivityType.IN_VEHICLE)

        // Then
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        assertEquals(ActivityType.IN_VEHICLE, captor.firstValue.activityType)
    }

    @Test
    fun `invoke sets startTime to current time`() = runTest {
        // Given
        val beforeTime = System.currentTimeMillis()
        `when`(activityRepository.getActiveManualSession()).thenReturn(null)
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

    @Test
    fun `invoke creates manually started session by default`() = runTest {
        // Given
        `when`(activityRepository.getActiveManualSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(1L)

        // When
        startActivityTrackingUseCase(ActivityType.CYCLING)

        // Then
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        assertEquals(true, captor.firstValue.isManuallyStarted)
    }

    @Test
    fun `invoke creates manually started session when isManual is true`() = runTest {
        // Given
        `when`(activityRepository.getActiveManualSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(1L)

        // When
        startActivityTrackingUseCase(ActivityType.RUNNING, isManual = true)

        // Then
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        assertEquals(true, captor.firstValue.isManuallyStarted)
    }

    @Test
    fun `invoke creates auto-detected session when isManual is false`() = runTest {
        // Given
        `when`(activityRepository.getActiveAutomaticSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(1L)

        // When
        startActivityTrackingUseCase(ActivityType.WALKING, isManual = false)

        // Then
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        assertEquals(false, captor.firstValue.isManuallyStarted)
    }

    @Test
    fun `invoke creates auto-detected cycling session`() = runTest {
        // Given
        `when`(activityRepository.getActiveAutomaticSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(5L)

        // When
        val sessionId = startActivityTrackingUseCase(ActivityType.CYCLING, isManual = false)

        // Then
        assertEquals(5L, sessionId)
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        
        val capturedSession = captor.firstValue
        assertEquals(ActivityType.CYCLING, capturedSession.activityType)
        assertEquals(false, capturedSession.isManuallyStarted)
    }

    @Test
    fun `invoke creates auto-detected running session`() = runTest {
        // Given
        `when`(activityRepository.getActiveAutomaticSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(10L)

        // When
        val sessionId = startActivityTrackingUseCase(ActivityType.RUNNING, isManual = false)

        // Then
        assertEquals(10L, sessionId)
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        
        val capturedSession = captor.firstValue
        assertEquals(ActivityType.RUNNING, capturedSession.activityType)
        assertEquals(false, capturedSession.isManuallyStarted)
    }

    @Test
    fun `invoke creates auto-detected vehicle session`() = runTest {
        // Given
        `when`(activityRepository.insertSession(any())).thenReturn(15L)
        `when`(activityRepository.getActiveAutomaticSession()).thenReturn(null)

        // When
        val sessionId = startActivityTrackingUseCase(ActivityType.IN_VEHICLE, isManual = false)

        // Then
        assertEquals(15L, sessionId)
        val captor = argumentCaptor<com.activitytracker.app.domain.model.ActivitySession>()
        verify(activityRepository).insertSession(captor.capture())
        
        val capturedSession = captor.firstValue
        assertEquals(ActivityType.IN_VEHICLE, capturedSession.activityType)
        assertEquals(false, capturedSession.isManuallyStarted)
    }

    @Test
    fun `invoke stops existing manual session before creating new manual session`() = runTest {
        // Given
        val existingSession = com.activitytracker.app.domain.model.ActivitySession(
            id = 100L,
            activityType = ActivityType.RUNNING,
            startTime = System.currentTimeMillis() - 60000,
            endTime = null,
            isManuallyStarted = true
        )
        `when`(activityRepository.getActiveManualSession()).thenReturn(existingSession)
        `when`(activityRepository.insertSession(any())).thenReturn(200L)

        // When
        val sessionId = startActivityTrackingUseCase(ActivityType.CYCLING, isManual = true)

        // Then
        verify(stopActivityTrackingUseCase).invoke(100L)
        assertEquals(200L, sessionId)
    }

    @Test
    fun `invoke stops existing automatic session before creating new automatic session`() = runTest {
        // Given
        val existingSession = com.activitytracker.app.domain.model.ActivitySession(
            id = 50L,
            activityType = ActivityType.WALKING,
            startTime = System.currentTimeMillis() - 30000,
            endTime = null,
            isManuallyStarted = false
        )
        `when`(activityRepository.getActiveAutomaticSession()).thenReturn(existingSession)
        `when`(activityRepository.insertSession(any())).thenReturn(60L)

        // When
        val sessionId = startActivityTrackingUseCase(ActivityType.RUNNING, isManual = false)

        // Then
        verify(stopActivityTrackingUseCase).invoke(50L)
        assertEquals(60L, sessionId)
    }

    @Test
    fun `invoke does not stop automatic session when creating manual session`() = runTest {
        // Given
        val existingAutoSession = com.activitytracker.app.domain.model.ActivitySession(
            id = 75L,
            activityType = ActivityType.WALKING,
            startTime = System.currentTimeMillis() - 30000,
            endTime = null,
            isManuallyStarted = false
        )
        `when`(activityRepository.getActiveManualSession()).thenReturn(null)
        `when`(activityRepository.getActiveAutomaticSession()).thenReturn(existingAutoSession)
        `when`(activityRepository.insertSession(any())).thenReturn(80L)

        // When
        val sessionId = startActivityTrackingUseCase(ActivityType.CYCLING, isManual = true)

        // Then
        // Should NOT stop the automatic session (only checks manual sessions)
        verify(stopActivityTrackingUseCase, org.mockito.Mockito.never()).invoke(75L)
        assertEquals(80L, sessionId)
    }

    @Test
    fun `invoke does not stop manual session when creating automatic session`() = runTest {
        // Given
        val existingManualSession = com.activitytracker.app.domain.model.ActivitySession(
            id = 90L,
            activityType = ActivityType.CYCLING,
            startTime = System.currentTimeMillis() - 60000,
            endTime = null,
            isManuallyStarted = true
        )
        `when`(activityRepository.getActiveManualSession()).thenReturn(existingManualSession)
        `when`(activityRepository.getActiveAutomaticSession()).thenReturn(null)
        `when`(activityRepository.insertSession(any())).thenReturn(95L)

        // When
        val sessionId = startActivityTrackingUseCase(ActivityType.RUNNING, isManual = false)

        // Then
        // Should NOT stop the manual session (only checks automatic sessions)
        verify(stopActivityTrackingUseCase, org.mockito.Mockito.never()).invoke(90L)
        assertEquals(95L, sessionId)
    }

    @Test
    fun `invoke allows both manual and automatic sessions to coexist`() = runTest {
        // Given
        val existingManualSession = com.activitytracker.app.domain.model.ActivitySession(
            id = 110L,
            activityType = ActivityType.CYCLING,
            startTime = System.currentTimeMillis() - 120000,
            endTime = null,
            isManuallyStarted = true
        )
        val existingAutoSession = com.activitytracker.app.domain.model.ActivitySession(
            id = 120L,
            activityType = ActivityType.WALKING,
            startTime = System.currentTimeMillis() - 60000,
            endTime = null,
            isManuallyStarted = false
        )
        `when`(activityRepository.getActiveManualSession()).thenReturn(existingManualSession)
        `when`(activityRepository.getActiveAutomaticSession()).thenReturn(existingAutoSession)
        `when`(activityRepository.insertSession(any())).thenReturn(130L)

        // When - create a new automatic session
        val sessionId = startActivityTrackingUseCase(ActivityType.RUNNING, isManual = false)

        // Then
        // Should stop the existing automatic session but NOT the manual session
        verify(stopActivityTrackingUseCase).invoke(120L)
        verify(stopActivityTrackingUseCase, org.mockito.Mockito.never()).invoke(110L)
        assertEquals(130L, sessionId)
    }
}
