package com.activitytracker.app.presentation.activitydetail

import androidx.navigation.NavController
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.domain.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityDetailViewModelTest {

    private lateinit var viewModel: ActivityDetailViewModel
    private lateinit var activityRepository: ActivityRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var navController: NavController
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = mock()
        locationRepository = mock()
        navController = mock()
        viewModel = ActivityDetailViewModel(activityRepository, locationRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadSession loads session and route points successfully`() = runTest {
        // Given
        val session = ActivitySession(
            id = 1L,
            activityType = ActivityType.CYCLING,
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 5000.0,
            averageSpeed = 5.0,
            stepCount = 0
        )
        val locationPoints = listOf(
            LocationPoint(1L, 1L, 40.0, -74.0, null, 10f, 1000L),
            LocationPoint(2L, 1L, 40.01, -74.01, null, 15f, 1100L),
            LocationPoint(3L, 1L, 40.02, -74.02, null, 20f, 1200L)
        )

        whenever(activityRepository.getSessionById(1L))
            .thenReturn(flow { emit(session) })
        whenever(locationRepository.getAccurateLocationPointsForSession(1L))
            .thenReturn(flow { emit(locationPoints) })

        // When
        viewModel.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityDetailUiState.Success
        assertEquals(session, state.session)
        assertEquals(3, state.routePoints.size)
        assertEquals(40.0, state.routePoints[0].latitude, 0.001)
        assertEquals(-74.0, state.routePoints[0].longitude, 0.001)
    }

    @Test
    fun `loadSession with null session shows error`() = runTest {
        // Given
        whenever(activityRepository.getSessionById(1L))
            .thenReturn(flow { emit(null) })

        // When
        viewModel.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityDetailUiState.Error
        assertEquals("Session not found", state.message)
    }

    @Test
    fun `loadSession with repository error shows error state`() = runTest {
        // Given
        whenever(activityRepository.getSessionById(1L))
            .thenReturn(flow { throw Exception("Database error") })

        // When
        viewModel.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityDetailUiState.Error
        assertEquals("Database error", state.message)
    }

    @Test
    fun `loadSession with empty route points loads successfully`() = runTest {
        // Given
        val session = ActivitySession(
            id = 1L,
            activityType = ActivityType.WALKING,
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 100.0,
            averageSpeed = 1.0,
            stepCount = 150
        )

        whenever(activityRepository.getSessionById(1L))
            .thenReturn(flow { emit(session) })
        whenever(locationRepository.getAccurateLocationPointsForSession(1L))
            .thenReturn(flow { emit(emptyList()) })

        // When
        viewModel.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityDetailUiState.Success
        assertEquals(session, state.session)
        assertTrue(state.routePoints.isEmpty())
    }

    @Test
    fun `deleteSession deletes and navigates back`() = runTest {
        // Given
        whenever(activityRepository.deleteSession(1L))
            .thenReturn(Unit)

        // When
        viewModel.deleteSession(1L, navController)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(activityRepository).deleteSession(1L)
        verify(navController).navigateUp()
    }

    @Test
    fun `deleteSession with error shows error state`() = runTest {
        // Given
        whenever(activityRepository.deleteSession(1L))
            .thenAnswer { throw Exception("Delete failed") }

        // When
        viewModel.deleteSession(1L, navController)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityDetailUiState.Error
        assertEquals("Delete failed", state.message)
        verify(navController, never()).navigateUp()
    }

    @Test
    fun `loadSession filters location points by accuracy`() = runTest {
        // Given
        val session = ActivitySession(
            id = 1L,
            activityType = ActivityType.RUNNING,
            startTime = 1000L,
            endTime = 2000L,
            totalDistance = 3000.0,
            averageSpeed = 3.0,
            stepCount = 4000
        )
        val locationPoints = listOf(
            LocationPoint(1L, 1L, 40.0, -74.0, null, 10f, 1000L),
            LocationPoint(2L, 1L, 40.01, -74.01, null, 45f, 1100L)
        )

        whenever(activityRepository.getSessionById(1L))
            .thenReturn(flow { emit(session) })
        whenever(locationRepository.getAccurateLocationPointsForSession(1L))
            .thenReturn(flow { emit(locationPoints) })

        // When
        viewModel.loadSession(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityDetailUiState.Success
        assertEquals(2, state.routePoints.size)
        verify(locationRepository).getAccurateLocationPointsForSession(1L)
    }
}
