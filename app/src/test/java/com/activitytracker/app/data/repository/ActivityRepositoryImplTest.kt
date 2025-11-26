package com.activitytracker.app.data.repository

import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.local.entity.ActivitySessionEntity
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

/**
 * Unit tests for ActivityRepositoryImpl.
 * Tests repository implementation with mocked DAO.
 */
class ActivityRepositoryImplTest {

    @Mock
    private lateinit var activitySessionDao: ActivitySessionDao

    private lateinit var repository: ActivityRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = ActivityRepositoryImpl(activitySessionDao)
    }

    @Test
    fun `getAllSessions returns mapped domain models`() = runTest {
        // Given
        val entities = listOf(
            ActivitySessionEntity(1L, "CYCLING", 1000L, 2000L, 5000.0, 5.5, 0),
            ActivitySessionEntity(2L, "RUNNING", 3000L, 4000L, 3000.0, 3.0, 3333)
        )
        `when`(activitySessionDao.getAllSessions()).thenReturn(flowOf(entities))

        // When
        val result = repository.getAllSessions().first()

        // Then
        assertEquals(2, result.size)
        assertEquals(ActivityType.CYCLING, result[0].activityType)
        assertEquals(ActivityType.RUNNING, result[1].activityType)
    }

    @Test
    fun `getSessionById returns mapped domain model`() = runTest {
        // Given
        val entity = ActivitySessionEntity(1L, "WALKING", 1000L, 2000L, 1000.0, 1.4, 1312)
        `when`(activitySessionDao.getSessionById(1L)).thenReturn(flowOf(entity))

        // When
        val result = repository.getSessionById(1L).first()

        // Then
        assertEquals(1L, result?.id)
        assertEquals(ActivityType.WALKING, result?.activityType)
    }

    @Test
    fun `getSessionById returns null when not found`() = runTest {
        // Given
        `when`(activitySessionDao.getSessionById(999L)).thenReturn(flowOf(null))

        // When
        val result = repository.getSessionById(999L).first()

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `getSessionsInTimeRange returns filtered sessions`() = runTest {
        // Given
        val entities = listOf(
            ActivitySessionEntity(1L, "CYCLING", 1000L, 2000L, 5000.0, 5.5, 0)
        )
        `when`(activitySessionDao.getSessionsInTimeRange(1000L, 5000L))
            .thenReturn(flowOf(entities))

        // When
        val result = repository.getSessionsInTimeRange(1000L, 5000L).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(ActivityType.CYCLING, result[0].activityType)
    }

    @Test
    fun `getLastCyclingSession returns most recent cycling session`() = runTest {
        // Given
        val entity = ActivitySessionEntity(5L, "CYCLING", 5000L, 6000L, 10000.0, 6.0, 0)
        `when`(activitySessionDao.getLastCyclingSession()).thenReturn(flowOf(entity))

        // When
        val result = repository.getLastCyclingSession().first()

        // Then
        assertEquals(5L, result?.id)
        assertEquals(ActivityType.CYCLING, result?.activityType)
    }

    @Test
    fun `insertSession converts domain to entity and returns id`() = runTest {
        // Given
        val session = ActivitySession(
            id = 0L,
            activityType = ActivityType.RUNNING,
            startTime = 1000L,
            endTime = null,
            totalDistance = 0.0,
            averageSpeed = 0.0,
            stepCount = 0
        )
        `when`(activitySessionDao.insertSession(org.mockito.kotlin.any())).thenReturn(42L)

        // When
        val result = repository.insertSession(session)

        // Then
        assertEquals(42L, result)
        verify(activitySessionDao).insertSession(org.mockito.kotlin.any())
    }

    @Test
    fun `updateSession converts domain to entity and calls dao`() = runTest {
        // Given
        val session = ActivitySession(
            id = 1L,
            activityType = ActivityType.WALKING,
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 1000.0,
            averageSpeed = 1.4,
            stepCount = 1312
        )

        // When
        repository.updateSession(session)

        // Then
        verify(activitySessionDao).updateSession(org.mockito.kotlin.any())
    }

    @Test
    fun `deleteSession calls dao with correct id`() = runTest {
        // Given
        val sessionId = 123L

        // When
        repository.deleteSession(sessionId)

        // Then
        verify(activitySessionDao).deleteSession(123L)
    }

    @Test
    fun `getSessionsByType returns filtered sessions`() = runTest {
        // Given
        val entities = listOf(
            ActivitySessionEntity(1L, "CYCLING", 1000L, 2000L, 5000.0, 5.5, 0),
            ActivitySessionEntity(2L, "CYCLING", 3000L, 4000L, 8000.0, 6.0, 0)
        )
        `when`(activitySessionDao.getSessionsByType("CYCLING"))
            .thenReturn(flowOf(entities))

        // When
        val result = repository.getSessionsByType("CYCLING").first()

        // Then
        assertEquals(2, result.size)
        result.forEach { session ->
            assertEquals(ActivityType.CYCLING, session.activityType)
        }
    }
}
