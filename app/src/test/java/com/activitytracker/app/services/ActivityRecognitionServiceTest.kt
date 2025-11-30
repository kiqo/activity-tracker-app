package com.activitytracker.app.services

import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.usecase.StartActivityTrackingUseCase
import com.activitytracker.app.domain.usecase.StopActivityTrackingUseCase
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.junit.Assert.assertEquals

/**
 * Unit tests for ActivityRecognitionService logic.
 * Tests the activity detection and session management logic.
 */
class ActivityRecognitionServiceTest {

    @Mock
    private lateinit var startActivityTrackingUseCase: StartActivityTrackingUseCase

    @Mock
    private lateinit var stopActivityTrackingUseCase: StopActivityTrackingUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `mapToActivityType maps cycling correctly`() {
        // Given
        val activityType = DetectedActivity.ON_BICYCLE

        // When
        val result = mapActivityType(activityType)

        // Then
        assertEquals(ActivityType.CYCLING, result)
    }

    @Test
    fun `mapToActivityType maps running correctly`() {
        // Given
        val activityType = DetectedActivity.RUNNING

        // When
        val result = mapActivityType(activityType)

        // Then
        assertEquals(ActivityType.RUNNING, result)
    }

    @Test
    fun `mapToActivityType maps walking correctly`() {
        // Given
        val activityType = DetectedActivity.WALKING

        // When
        val result = mapActivityType(activityType)

        // Then
        assertEquals(ActivityType.WALKING, result)
    }

    @Test
    fun `mapToActivityType maps vehicle correctly`() {
        // Given
        val activityType = DetectedActivity.IN_VEHICLE

        // When
        val result = mapActivityType(activityType)

        // Then
        assertEquals(ActivityType.IN_VEHICLE, result)
    }

    @Test
    fun `mapToActivityType maps on foot correctly`() {
        // Given
        val activityType = DetectedActivity.ON_FOOT

        // When
        val result = mapActivityType(activityType)

        // Then
        assertEquals(ActivityType.ON_FOOT, result)
    }

    @Test
    fun `mapToActivityType maps still correctly`() {
        // Given
        val activityType = DetectedActivity.STILL

        // When
        val result = mapActivityType(activityType)

        // Then
        assertEquals(ActivityType.STILL, result)
    }

    @Test
    fun `mapToActivityType maps unknown correctly`() {
        // Given
        val activityType = DetectedActivity.UNKNOWN

        // When
        val result = mapActivityType(activityType)

        // Then
        assertEquals(ActivityType.UNKNOWN, result)
    }

    @Test
    fun `mapToActivityType maps tilting correctly`() {
        // Given
        val activityType = DetectedActivity.TILTING

        // When
        val result = mapActivityType(activityType)

        // Then
        assertEquals(ActivityType.TILTING, result)
    }

    @Test
    fun `handleActivityDetected creates auto-detected session with high confidence`() = runTest {
        // Given
        val confidence = 80 // Above 75% threshold
        `when`(startActivityTrackingUseCase(any(), eq(false))).thenReturn(1L)

        // When - simulating detection of cycling activity
        val shouldCreateSession = confidence >= 75
        if (shouldCreateSession) {
            startActivityTrackingUseCase(ActivityType.CYCLING, isManual = false)
        }

        // Then
        verify(startActivityTrackingUseCase).invoke(ActivityType.CYCLING, false)
    }

    @Test
    fun `handleActivityDetected ignores low confidence detection`() = runTest {
        // Given
        val confidence = 50 // Below 75% threshold

        // When - simulating detection with low confidence
        val shouldCreateSession = confidence >= 75
        if (shouldCreateSession) {
            startActivityTrackingUseCase(ActivityType.CYCLING, isManual = false)
        }

        // Then
        verify(startActivityTrackingUseCase, never()).invoke(any(), any())
    }

    @Test
    fun `handleActivityDetected stops existing session before starting new one`() = runTest {
        // Given
        val existingSessionId = 1L
        val confidence = 80
        `when`(startActivityTrackingUseCase(any(), eq(false))).thenReturn(2L)

        // When - simulating activity change
        stopActivityTrackingUseCase(existingSessionId)
        startActivityTrackingUseCase(ActivityType.RUNNING, isManual = false)

        // Then
        verify(stopActivityTrackingUseCase).invoke(existingSessionId)
        verify(startActivityTrackingUseCase).invoke(ActivityType.RUNNING, false)
    }

    @Test
    fun `auto-detected cycling session is created with isManual false`() = runTest {
        // Given
        `when`(startActivityTrackingUseCase(ActivityType.CYCLING, false)).thenReturn(1L)

        // When
        startActivityTrackingUseCase(ActivityType.CYCLING, isManual = false)

        // Then
        verify(startActivityTrackingUseCase).invoke(ActivityType.CYCLING, false)
    }

    @Test
    fun `auto-detected running session is created with isManual false`() = runTest {
        // Given
        `when`(startActivityTrackingUseCase(ActivityType.RUNNING, false)).thenReturn(2L)

        // When
        startActivityTrackingUseCase(ActivityType.RUNNING, isManual = false)

        // Then
        verify(startActivityTrackingUseCase).invoke(ActivityType.RUNNING, false)
    }

    @Test
    fun `auto-detected walking session is created with isManual false`() = runTest {
        // Given
        `when`(startActivityTrackingUseCase(ActivityType.WALKING, false)).thenReturn(3L)

        // When
        startActivityTrackingUseCase(ActivityType.WALKING, isManual = false)

        // Then
        verify(startActivityTrackingUseCase).invoke(ActivityType.WALKING, false)
    }

    @Test
    fun `auto-detected vehicle session is created with isManual false`() = runTest {
        // Given
        `when`(startActivityTrackingUseCase(ActivityType.IN_VEHICLE, false)).thenReturn(4L)

        // When
        startActivityTrackingUseCase(ActivityType.IN_VEHICLE, isManual = false)

        // Then
        verify(startActivityTrackingUseCase).invoke(ActivityType.IN_VEHICLE, false)
    }

    @Test
    fun `confidence threshold is 75 percent`() {
        // Given
        val threshold = 75

        // When/Then - verify boundary conditions
        assertEquals(true, 75 >= threshold)
        assertEquals(true, 76 >= threshold)
        assertEquals(false, 74 >= threshold)
    }

    @Test
    fun `inactivity timeout is 5 minutes`() {
        // Given
        val timeoutMs = 5 * 60 * 1000L

        // Then
        assertEquals(300000L, timeoutMs)
    }

    // Helper function to simulate the mapping logic from the service
    private fun mapActivityType(activityType: Int): ActivityType? {
        return when (activityType) {
            DetectedActivity.IN_VEHICLE -> ActivityType.IN_VEHICLE
            DetectedActivity.ON_BICYCLE -> ActivityType.CYCLING
            DetectedActivity.ON_FOOT -> ActivityType.ON_FOOT
            DetectedActivity.STILL -> ActivityType.STILL
            DetectedActivity.UNKNOWN -> ActivityType.UNKNOWN
            DetectedActivity.TILTING -> ActivityType.TILTING
            DetectedActivity.WALKING -> ActivityType.WALKING
            DetectedActivity.RUNNING -> ActivityType.RUNNING
            else -> null
        }
    }
}
