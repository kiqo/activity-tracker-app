package com.activitytracker.app.domain.usecase

import com.activitytracker.app.domain.model.ActivityType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for EstimateStepCountUseCase.
 * Tests step count estimation logic based on distance and activity type.
 */
class EstimateStepCountUseCaseTest {

    private lateinit var estimateStepCountUseCase: EstimateStepCountUseCase

    @Before
    fun setup() {
        estimateStepCountUseCase = EstimateStepCountUseCase()
    }

    @Test
    fun `estimate steps for walking - 1 km`() {
        // Given - 1000 meters walking
        val distanceMeters = 1000.0
        val activityType = ActivityType.WALKING

        // When
        val steps = estimateStepCountUseCase(distanceMeters, activityType)

        // Then - approximately 1312 steps (1000 / 0.762)
        assertEquals(1312, steps)
    }

    @Test
    fun `estimate steps for running - 1 km`() {
        // Given - 1000 meters running
        val distanceMeters = 1000.0
        val activityType = ActivityType.RUNNING

        // When
        val steps = estimateStepCountUseCase(distanceMeters, activityType)

        // Then - approximately 1111 steps (1000 / 0.9)
        assertEquals(1111, steps)
    }

    @Test
    fun `estimate steps for cycling returns zero`() {
        // Given - cycling activity
        val distanceMeters = 1000.0
        val activityType = ActivityType.CYCLING

        // When
        val steps = estimateStepCountUseCase(distanceMeters, activityType)

        // Then
        assertEquals(0, steps)
    }

    @Test
    fun `estimate steps for vehicle returns zero`() {
        // Given - vehicle activity
        val distanceMeters = 1000.0
        val activityType = ActivityType.IN_VEHICLE

        // When
        val steps = estimateStepCountUseCase(distanceMeters, activityType)

        // Then
        assertEquals(0, steps)
    }

    @Test
    fun `estimate steps for zero distance returns zero`() {
        // Given - no distance
        val distanceMeters = 0.0
        val activityType = ActivityType.WALKING

        // When
        val steps = estimateStepCountUseCase(distanceMeters, activityType)

        // Then
        assertEquals(0, steps)
    }

    @Test
    fun `estimate steps for walking - 5 km`() {
        // Given - 5000 meters walking
        val distanceMeters = 5000.0
        val activityType = ActivityType.WALKING

        // When
        val steps = estimateStepCountUseCase(distanceMeters, activityType)

        // Then - approximately 6561 steps
        assertEquals(6561, steps)
    }

    @Test
    fun `estimate steps for running - 5 km`() {
        // Given - 5000 meters running
        val distanceMeters = 5000.0
        val activityType = ActivityType.RUNNING

        // When
        val steps = estimateStepCountUseCase(distanceMeters, activityType)

        // Then - approximately 5555 steps
        assertEquals(5555, steps)
    }

    @Test
    fun `running has fewer steps than walking for same distance`() {
        // Given - same distance for both activities
        val distanceMeters = 1000.0

        // When
        val walkingSteps = estimateStepCountUseCase(distanceMeters, ActivityType.WALKING)
        val runningSteps = estimateStepCountUseCase(distanceMeters, ActivityType.RUNNING)

        // Then - running should have fewer steps (longer stride)
        assert(runningSteps < walkingSteps)
    }
}
