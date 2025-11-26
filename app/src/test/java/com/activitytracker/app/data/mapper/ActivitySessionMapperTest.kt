package com.activitytracker.app.data.mapper

import com.activitytracker.app.data.local.entity.ActivitySessionEntity
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for ActivitySession mapper functions.
 * Tests conversion between entity and domain models.
 */
class ActivitySessionMapperTest {

    @Test
    fun `entity to domain conversion maps all fields correctly`() {
        // Given
        val entity = ActivitySessionEntity(
            id = 1L,
            activityType = "CYCLING",
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 5000.0,
            averageSpeed = 5.5,
            stepCount = 0
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(1L, domain.id)
        assertEquals(ActivityType.CYCLING, domain.activityType)
        assertEquals(1000L, domain.startTime)
        assertEquals(2000L, domain.endTime)
        assertEquals(5000.0, domain.totalDistance, 0.01)
        assertEquals(5.5, domain.averageSpeed, 0.01)
        assertEquals(0, domain.stepCount)
    }

    @Test
    fun `domain to entity conversion maps all fields correctly`() {
        // Given
        val domain = ActivitySession(
            id = 2L,
            activityType = ActivityType.RUNNING,
            startTime = 3000L,
            endTime = 4000L,
            totalDistance = 3000.0,
            averageSpeed = 3.0,
            stepCount = 3333
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(2L, entity.id)
        assertEquals("RUNNING", entity.activityType)
        assertEquals(3000L, entity.startTime)
        assertEquals(4000L, entity.endTime)
        assertEquals(3000.0, entity.totalDistance, 0.01)
        assertEquals(3.0, entity.averageSpeed, 0.01)
        assertEquals(3333, entity.stepCount)
    }

    @Test
    fun `entity to domain handles null endTime`() {
        // Given - active session with null endTime
        val entity = ActivitySessionEntity(
            id = 1L,
            activityType = "WALKING",
            startTime = 1000L,
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(null, domain.endTime)
    }

    @Test
    fun `list of entities to domain converts all items`() {
        // Given
        val entities = listOf(
            ActivitySessionEntity(1L, "CYCLING", 1000L, 2000L, 5000.0, 5.5, 0),
            ActivitySessionEntity(2L, "RUNNING", 3000L, 4000L, 3000.0, 3.0, 3333),
            ActivitySessionEntity(3L, "WALKING", 5000L, 6000L, 1000.0, 1.4, 1312)
        )

        // When
        val domains = entities.toDomain()

        // Then
        assertEquals(3, domains.size)
        assertEquals(ActivityType.CYCLING, domains[0].activityType)
        assertEquals(ActivityType.RUNNING, domains[1].activityType)
        assertEquals(ActivityType.WALKING, domains[2].activityType)
    }

    @Test
    fun `conversion preserves all activity types`() {
        val activityTypes = listOf(
            ActivityType.CYCLING,
            ActivityType.RUNNING,
            ActivityType.WALKING,
            ActivityType.IN_VEHICLE
        )

        activityTypes.forEach { activityType ->
            // Given
            val domain = ActivitySession(
                id = 1L,
                activityType = activityType,
                startTime = 1000L,
                endTime = 2000L,
                totalDistance = 1000.0,
                averageSpeed = 1.0,
                stepCount = 0
            )

            // When
            val entity = domain.toEntity()
            val backToDomain = entity.toDomain()

            // Then
            assertEquals(activityType, backToDomain.activityType)
        }
    }

    @Test
    fun `round trip conversion preserves data integrity`() {
        // Given
        val original = ActivitySession(
            id = 42L,
            activityType = ActivityType.CYCLING,
            startTime = 123456789L,
            endTime = 987654321L,
            totalDistance = 12345.67,
            averageSpeed = 6.78,
            stepCount = 0
        )

        // When - convert to entity and back
        val entity = original.toEntity()
        val result = entity.toDomain()

        // Then - all fields should match
        assertEquals(original.id, result.id)
        assertEquals(original.activityType, result.activityType)
        assertEquals(original.startTime, result.startTime)
        assertEquals(original.endTime, result.endTime)
        assertEquals(original.totalDistance, result.totalDistance, 0.01)
        assertEquals(original.averageSpeed, result.averageSpeed, 0.01)
        assertEquals(original.stepCount, result.stepCount)
    }
}
