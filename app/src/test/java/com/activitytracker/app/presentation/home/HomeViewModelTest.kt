package com.activitytracker.app.presentation.home

import com.activitytracker.app.domain.model.ActivityStatistics
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.model.TimeInterval
import com.activitytracker.app.domain.usecase.GetActivityStatisticsUseCase
import com.activitytracker.app.domain.usecase.StartActivityTrackingUseCase
import com.activitytracker.app.domain.usecase.StopActivityTrackingUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var startActivityTrackingUseCase: StartActivityTrackingUseCase
    private lateinit var stopActivityTrackingUseCase: StopActivityTrackingUseCase
    private lateinit var getActivityStatisticsUseCase: GetActivityStatisticsUseCase
    private lateinit var context: android.content.Context
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        startActivityTrackingUseCase = mock()
        stopActivityTrackingUseCase = mock()
        getActivityStatisticsUseCase = mock()
        context = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads today statistics`() = runTest {
        // Given
        val statistics = ActivityStatistics(
            walkingDistanceKm = 2.5,
            cyclingDistanceKm = 10.0,
            runningDistanceKm = 5.0,
            totalSteps = 3000,
            walkingCount = 2,
            cyclingCount = 1,
            runningCount = 1,
            vehicleCount = 0,
            timeInterval = TimeInterval.DAILY
        )
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { emit(statistics) })

        // When
        viewModel = HomeViewModel(
            startActivityTrackingUseCase,
            stopActivityTrackingUseCase,
            getActivityStatisticsUseCase,
            context
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as HomeUiState.Success
        assertEquals(2.5, state.todayWalkingKm)
        assertEquals(10.0, state.todayCyclingKm)
        assertEquals(5.0, state.todayRunningKm)
        assertEquals(3000, state.todaySteps)
        assertFalse(state.isTracking)
        assertNull(state.currentSessionId)
    }

    @Test
    fun `startTracking updates state with session id`() = runTest {
        // Given
        val statistics = ActivityStatistics(
            walkingDistanceKm = 2.5,
            cyclingDistanceKm = 10.0,
            runningDistanceKm = 5.0,
            totalSteps = 3000,
            walkingCount = 2,
            cyclingCount = 1,
            runningCount = 1,
            vehicleCount = 0,
            timeInterval = TimeInterval.DAILY
        )
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { emit(statistics) })
        whenever(startActivityTrackingUseCase(ActivityType.CYCLING))
            .thenReturn(123L)

        viewModel = HomeViewModel(
            startActivityTrackingUseCase,
            stopActivityTrackingUseCase,
            getActivityStatisticsUseCase,
            context
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.startTracking(ActivityType.CYCLING)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as HomeUiState.Success
        assertTrue(state.isTracking)
        assertEquals(123L, state.currentSessionId)
        assertNull(state.error)
    }

    @Test
    fun `stopTracking updates state and reloads statistics`() = runTest {
        // Given
        val initialStats = ActivityStatistics(
            walkingDistanceKm = 2.5,
            cyclingDistanceKm = 10.0,
            runningDistanceKm = 5.0,
            totalSteps = 3000,
            walkingCount = 2,
            cyclingCount = 1,
            runningCount = 1,
            vehicleCount = 0,
            timeInterval = TimeInterval.DAILY
        )
        val updatedStats = ActivityStatistics(
            walkingDistanceKm = 2.5,
            cyclingDistanceKm = 15.0,
            runningDistanceKm = 5.0,
            totalSteps = 3000,
            walkingCount = 2,
            cyclingCount = 2,
            runningCount = 1,
            vehicleCount = 0,
            timeInterval = TimeInterval.DAILY
        )
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { emit(initialStats) })
            .thenReturn(flow { emit(updatedStats) })
        whenever(startActivityTrackingUseCase(ActivityType.CYCLING))
            .thenReturn(123L)

        viewModel = HomeViewModel(
            startActivityTrackingUseCase,
            stopActivityTrackingUseCase,
            getActivityStatisticsUseCase,
            context
        )
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.startTracking(ActivityType.CYCLING)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.stopTracking()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as HomeUiState.Success
        assertFalse(state.isTracking)
        assertNull(state.currentSessionId)
        verify(stopActivityTrackingUseCase).invoke(123L)
    }

    @Test
    fun `startTracking error sets error state`() = runTest {
        // Given
        val statistics = ActivityStatistics(
            walkingDistanceKm = 2.5,
            cyclingDistanceKm = 10.0,
            runningDistanceKm = 5.0,
            totalSteps = 3000,
            walkingCount = 2,
            cyclingCount = 1,
            runningCount = 1,
            vehicleCount = 0,
            timeInterval = TimeInterval.DAILY
        )
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { emit(statistics) })
        whenever(startActivityTrackingUseCase(ActivityType.CYCLING))
            .thenThrow(RuntimeException("Failed to start tracking"))

        viewModel = HomeViewModel(
            startActivityTrackingUseCase,
            stopActivityTrackingUseCase,
            getActivityStatisticsUseCase,
            context,
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.startTracking(ActivityType.CYCLING)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as HomeUiState.Success
        assertTrue(state.error != null)
        assertFalse(state.isTracking)
    }

    @Test
    fun `clearError removes error from state`() = runTest {
        // Given
        val statistics = ActivityStatistics(
            walkingDistanceKm = 2.5,
            cyclingDistanceKm = 10.0,
            runningDistanceKm = 5.0,
            totalSteps = 3000,
            walkingCount = 2,
            cyclingCount = 1,
            runningCount = 1,
            vehicleCount = 0,
            timeInterval = TimeInterval.DAILY
        )
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { emit(statistics) })
        whenever(startActivityTrackingUseCase(ActivityType.CYCLING))
            .thenThrow(RuntimeException("Failed"))

        viewModel = HomeViewModel(
            startActivityTrackingUseCase,
            stopActivityTrackingUseCase,
            getActivityStatisticsUseCase,
            context
        )
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.startTracking(ActivityType.CYCLING)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        val state = viewModel.uiState.value as HomeUiState.Success
        assertNull(state.error)
    }

    @Test
    fun `loading statistics error sets Error state`() = runTest {
        // Given
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { throw Exception("Database error") })

        // When
        viewModel = HomeViewModel(
            startActivityTrackingUseCase,
            stopActivityTrackingUseCase,
            getActivityStatisticsUseCase,
            context
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is HomeUiState.Error)
    }
}
