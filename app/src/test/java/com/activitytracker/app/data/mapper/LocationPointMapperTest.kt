package com.activitytracker.app.data.mapper

import com.activitytracker.app.data.local.entity.LocationPointEntity
import com.activitytracker.app.domain.model.LocationPoint
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for LocationPoint mapper functions.
 * Tests conversion between entity and domain models.
 */
class LocationPointMapperTest {

    @Test
    fun `entity to domain conversion maps all fields correctly`() {
        // Given
        val entity = LocationPointEntity(
            id = 1L,
            sessionId = 10L,
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = 50.0,
            accuracy = 10.5f,
            timestamp = 1000L
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(1L, domain.id)
        assertEquals(10L, domain.sessionId)
        assertEquals(37.7749, domain.latitude, 0.0001)
        assertEquals(-122.4194, domain.longitude, 0.0001)
        assertEquals(50.0, domain.altitude)
        assertEquals(10.5f, domain.accuracy)
        assertEquals(1000L, domain.timestamp)
    }

    @Test
    fun `domain to entity conversion maps all fields correctly`() {
        // Given
        val domain = LocationPoint(
            id = 2L,
            sessionId = 20L,
            latitude = 34.0522,
            longitude = -118.2437,
            altitude = 100.0,
            accuracy = 15.0f,
            timestamp = 2000L
        )

        // When
        val entity = domain.toEntity()

        // Then
        assertEquals(2L, entity.id)
        assertEquals(20L, entity.sessionId)
        assertEquals(34.0522, entity.latitude, 0.0001)
        assertEquals(-118.2437, entity.longitude, 0.0001)
        assertEquals(100.0, entity.altitude)
        assertEquals(15.0f, entity.accuracy)
        assertEquals(2000L, entity.timestamp)
    }

    @Test
    fun `entity to domain handles null altitude`() {
        // Given - location without altitude
        val entity = LocationPointEntity(
            id = 1L,
            sessionId = 10L,
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = null,
            accuracy = 10.5f,
            timestamp = 1000L
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals(null, domain.altitude)
    }

    @Test
    fun `list of entities to domain converts all items`() {
        // Given
        val entities = listOf(
            LocationPointEntity(1L, 10L, 37.7749, -122.4194, 50.0, 10f, 1000L),
            LocationPointEntity(2L, 10L, 37.7849, -122.4194, 51.0, 12f, 2000L),
            LocationPointEntity(3L, 10L, 37.7949, -122.4194, 52.0, 11f, 3000L)
        )

        // When
        val domains = entities.toDomain()

        // Then
        assertEquals(3, domains.size)
        assertEquals(37.7749, domains[0].latitude, 0.0001)
        assertEquals(37.7849, domains[1].latitude, 0.0001)
        assertEquals(37.7949, domains[2].latitude, 0.0001)
    }

    @Test
    fun `list of domains to entity converts all items`() {
        // Given
        val domains = listOf(
            LocationPoint(1L, 10L, 37.7749, -122.4194, 50.0, 10f, 1000L),
            LocationPoint(2L, 10L, 37.7849, -122.4194, 51.0, 12f, 2000L)
        )

        // When
        val entities = domains.toEntity()

        // Then
        assertEquals(2, entities.size)
        assertEquals(37.7749, entities[0].latitude, 0.0001)
        assertEquals(37.7849, entities[1].latitude, 0.0001)
    }

    @Test
    fun `round trip conversion preserves data integrity`() {
        // Given
        val original = LocationPoint(
            id = 123L,
            sessionId = 456L,
            latitude = 40.7128,
            longitude = -74.0060,
            altitude = 10.5,
            accuracy = 8.3f,
            timestamp = 1234567890L
        )

        // When - convert to entity and back
        val entity = original.toEntity()
        val result = entity.toDomain()

        // Then - all fields should match
        assertEquals(original.id, result.id)
        assertEquals(original.sessionId, result.sessionId)
        assertEquals(original.latitude, result.latitude, 0.0001)
        assertEquals(original.longitude, result.longitude, 0.0001)
        assertEquals(original.altitude, result.altitude)
        assertEquals(original.accuracy, result.accuracy)
        assertEquals(original.timestamp, result.timestamp)
    }

    @Test
    fun `conversion handles high precision coordinates`() {
        // Given - very precise GPS coordinates
        val domain = LocationPoint(
            id = 1L,
            sessionId = 1L,
            latitude = 37.77492537,
            longitude = -122.41941550,
            altitude = 50.123456,
            accuracy = 5.5f,
            timestamp = 1000L
        )

        // When
        val entity = domain.toEntity()
        val result = entity.toDomain()

        // Then - precision should be preserved
        assertEquals(37.77492537, result.latitude, 0.00000001)
        assertEquals(-122.41941550, result.longitude, 0.00000001)
        assertEquals(50.123456, result.altitude)
    }
}
