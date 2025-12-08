package com.activitytracker.app.data.repository

import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.local.dao.LocationPointDao
import com.activitytracker.app.data.local.dao.SessionLocationPointDao
import com.activitytracker.app.data.local.entity.ActivitySessionEntity
import com.activitytracker.app.data.local.entity.LocationPointEntity
import com.activitytracker.app.data.local.entity.SessionLocationPointEntity
import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.util.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times

/**
 * Unit tests for LocationRepositoryImpl.
 * Tests shared location tracking functionality with junction table.
 * Requirements: 2.1, 2.5, 2.6
 */
class LocationRepositoryImplTest {

    @Mock
    private lateinit var locationPointDao: LocationPointDao

    @Mock
    private lateinit var sessionLocationPointDao: SessionLocationPointDao

    @Mock
    private lateinit var activitySessionDao: ActivitySessionDao
    
    @Mock
    private lateinit var logger: Logger

    private lateinit var repository: LocationRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = LocationRepositoryImpl(
            locationPointDao,
            sessionLocationPointDao,
            activitySessionDao,
            logger
        )
    }

    @Test
    fun `insertLocationPoint stores location point only once and returns id`() = runTest {
        // Given
        val locationPoint = LocationPoint(
            id = 0L,
            sessionId = 0L, // Not used in entity
            latitude = 37.7749,
            longitude = -122.4194,
            altitude = 10.0,
            accuracy = 15.0f,
            timestamp = 1000L
        )
        val expectedId = 42L
        `when`(locationPointDao.insertLocationPoint(any())).thenReturn(expectedId)

        // When
        val result = repository.insertLocationPoint(locationPoint)

        // Then
        assertEquals(expectedId, result)
        verify(locationPointDao, times(1)).insertLocationPoint(any())
    }

    @Test
    fun `linkLocationPointToSession creates junction table entry`() = runTest {
        // Given
        val locationPointId = 100L
        val sessionId = 1L

        // When
        repository.linkLocationPointToSession(locationPointId, sessionId)

        // Then
        val captor = argumentCaptor<SessionLocationPointEntity>()
        verify(sessionLocationPointDao).linkLocationPointToSession(captor.capture())
        
        val link = captor.firstValue
        assertEquals(sessionId, link.sessionId)
        assertEquals(locationPointId, link.locationPointId)
    }

    @Test
    fun `linkLocationPointToAllActiveSessions links to all active sessions`() = runTest {
        // Given
        val locationPointId = 100L
        val manualSession = ActivitySessionEntity(
            id = 1L,
            activityType = "CYCLING",
            startTime = 1000L,
            endTime = null, // Active
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = true
        )
        val autoSession = ActivitySessionEntity(
            id = 2L,
            activityType = "WALKING",
            startTime = 1000L,
            endTime = null, // Active
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0,
            isManuallyStarted = false
        )
        val activeSessions = listOf(manualSession, autoSession)
        `when`(activitySessionDao.getActiveSessionsSync()).thenReturn(activeSessions)

        // When
        repository.linkLocationPointToAllActiveSessions(locationPointId)

        // Then
        val captor = argumentCaptor<List<SessionLocationPointEntity>>()
        verify(sessionLocationPointDao).linkLocationPointToSessions(captor.capture())
        
        val links = captor.firstValue
        assertEquals(2, links.size)
        assertEquals(1L, links[0].sessionId)
        assertEquals(locationPointId, links[0].locationPointId)
        assertEquals(2L, links[1].sessionId)
        assertEquals(locationPointId, links[1].locationPointId)
    }

    @Test
    fun `linkLocationPointToAllActiveSessions handles no active sessions gracefully`() = runTest {
        // Given
        val locationPointId = 100L
        `when`(activitySessionDao.getActiveSessionsSync()).thenReturn(emptyList())

        // When
        repository.linkLocationPointToAllActiveSessions(locationPointId)

        // Then - should not throw exception, just log warning
        verify(activitySessionDao).getActiveSessionsSync()
    }

    @Test
    fun `getLocationPointsForSession returns mapped domain models`() = runTest {
        // Given
        val sessionId = 1L
        val entities = listOf(
            LocationPointEntity(1L, 37.7749, -122.4194, 10.0, 15.0f, 1000L),
            LocationPointEntity(2L, 37.7750, -122.4195, 11.0, 16.0f, 2000L)
        )
        `when`(locationPointDao.getLocationPointsForSession(sessionId))
            .thenReturn(flowOf(entities))

        // When
        val result = repository.getLocationPointsForSession(sessionId).first()

        // Then
        assertEquals(2, result.size)
        assertEquals(37.7749, result[0].latitude, 0.0001)
        assertEquals(-122.4194, result[0].longitude, 0.0001)
        assertEquals(37.7750, result[1].latitude, 0.0001)
        assertEquals(-122.4195, result[1].longitude, 0.0001)
    }

    @Test
    fun `getAccurateLocationPointsForSession filters by accuracy`() = runTest {
        // Given
        val sessionId = 1L
        val maxAccuracy = 50.0f
        val entities = listOf(
            LocationPointEntity(1L, 37.7749, -122.4194, 10.0, 15.0f, 1000L)
        )
        `when`(locationPointDao.getAccurateLocationPointsForSession(sessionId, maxAccuracy))
            .thenReturn(flowOf(entities))

        // When
        val result = repository.getAccurateLocationPointsForSession(sessionId, maxAccuracy).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(15.0f, result[0].accuracy, 0.01f)
    }

    @Test
    fun `getLastLocationForSession returns most recent location`() = runTest {
        // Given
        val sessionId = 1L
        val entity = LocationPointEntity(5L, 37.7749, -122.4194, 10.0, 15.0f, 5000L)
        `when`(locationPointDao.getLastLocationForSession(sessionId)).thenReturn(entity)

        // When
        val result = repository.getLastLocationForSession(sessionId)

        // Then
        assertNotNull(result)
        assertEquals(5L, result?.id)
        assertEquals(5000L, result?.timestamp)
    }

    @Test
    fun `getLastLocationForSession returns null when no location exists`() = runTest {
        // Given
        val sessionId = 999L
        `when`(locationPointDao.getLastLocationForSession(sessionId)).thenReturn(null)

        // When
        val result = repository.getLastLocationForSession(sessionId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getFirstLocationForSession returns earliest location`() = runTest {
        // Given
        val sessionId = 1L
        val entity = LocationPointEntity(1L, 37.7749, -122.4194, 10.0, 15.0f, 1000L)
        `when`(locationPointDao.getFirstLocationForSession(sessionId)).thenReturn(entity)

        // When
        val result = repository.getFirstLocationForSession(sessionId)

        // Then
        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals(1000L, result?.timestamp)
    }

    @Test
    fun `insertLocationPoints stores multiple points in batch`() = runTest {
        // Given
        val points = listOf(
            LocationPoint(0L, 0L, 37.7749, -122.4194, 10.0, 15.0f, 1000L),
            LocationPoint(0L, 0L, 37.7750, -122.4195, 11.0, 16.0f, 2000L)
        )

        // When
        repository.insertLocationPoints(points)

        // Then
        verify(locationPointDao).insertLocationPoints(any())
    }
}
