package com.activitytracker.app.presentation.statistics

import com.activitytracker.app.domain.model.ActivityStatistics
import com.activitytracker.app.domain.model.TimeInterval
import com.activitytracker.app.domain.usecase.GetActivityStatisticsUseCase
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private lateinit var viewModel: StatisticsViewModel
    private lateinit var getActivityStatisticsUseCase: GetActivityStatisticsUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getActivityStatisticsUseCase = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads daily statistics by default`() = runTest {
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
        viewModel = StatisticsViewModel(getActivityStatisticsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(TimeInterval.DAILY, state.selectedInterval)
        assertEquals(2.5, state.walkingDistanceKm)
        assertEquals(10.0, state.cyclingDistanceKm)
        assertEquals(5.0, state.runningDistanceKm)
        assertEquals(3000, state.totalSteps)
        assertEquals(2, state.walkingCount)
        assertEquals(1, state.cyclingCount)
        assertEquals(1, state.runningCount)
        assertEquals(0, state.vehicleCount)
        assertEquals(4, state.totalActivities)
        assertFalse(state.isLoading)
    }

    @Test
    fun `selectTimeInterval changes interval and reloads statistics`() = runTest {
        // Given
        val dailyStats = ActivityStatistics(
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
        val weeklyStats = ActivityStatistics(
            walkingDistanceKm = 15.0,
            cyclingDistanceKm = 50.0,
            runningDistanceKm = 25.0,
            totalSteps = 20000,
            walkingCount = 10,
            cyclingCount = 5,
            runningCount = 7,
            vehicleCount = 2,
            timeInterval = TimeInterval.WEEKLY
        )
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { emit(dailyStats) })
        whenever(getActivityStatisticsUseCase(TimeInterval.WEEKLY))
            .thenReturn(flow { emit(weeklyStats) })

        viewModel = StatisticsViewModel(getActivityStatisticsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectTimeInterval(TimeInterval.WEEKLY)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(TimeInterval.WEEKLY, state.selectedInterval)
        assertEquals(15.0, state.walkingDistanceKm)
        assertEquals(50.0, state.cyclingDistanceKm)
        assertEquals(25.0, state.runningDistanceKm)
        assertEquals(20000, state.totalSteps)
        assertEquals(24, state.totalActivities)
        verify(getActivityStatisticsUseCase).invoke(TimeInterval.WEEKLY)
    }

    @Test
    fun `loading state is set correctly during statistics fetch`() = runTest {
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
        viewModel = StatisticsViewModel(getActivityStatisticsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - loading should be false after completion
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `error during statistics fetch sets loading to false`() = runTest {
        // Given
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { throw Exception("Database error") })

        // When
        viewModel = StatisticsViewModel(getActivityStatisticsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `totalActivities is calculated correctly`() = runTest {
        // Given
        val statistics = ActivityStatistics(
            walkingDistanceKm = 2.5,
            cyclingDistanceKm = 10.0,
            runningDistanceKm = 5.0,
            totalSteps = 3000,
            walkingCount = 3,
            cyclingCount = 2,
            runningCount = 4,
            vehicleCount = 1,
            timeInterval = TimeInterval.DAILY
        )
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { emit(statistics) })

        // When
        viewModel = StatisticsViewModel(getActivityStatisticsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(10, viewModel.uiState.value.totalActivities)
    }

    @Test
    fun `monthly interval loads correct statistics`() = runTest {
        // Given
        val monthlyStats = ActivityStatistics(
            walkingDistanceKm = 50.0,
            cyclingDistanceKm = 200.0,
            runningDistanceKm = 100.0,
            totalSteps = 80000,
            walkingCount = 30,
            cyclingCount = 20,
            runningCount = 25,
            vehicleCount = 10,
            timeInterval = TimeInterval.MONTHLY
        )
        whenever(getActivityStatisticsUseCase(TimeInterval.DAILY))
            .thenReturn(flow { emit(monthlyStats) })
        whenever(getActivityStatisticsUseCase(TimeInterval.MONTHLY))
            .thenReturn(flow { emit(monthlyStats) })

        viewModel = StatisticsViewModel(getActivityStatisticsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.selectTimeInterval(TimeInterval.MONTHLY)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(TimeInterval.MONTHLY, state.selectedInterval)
        assertEquals(50.0, state.walkingDistanceKm)
        assertEquals(200.0, state.cyclingDistanceKm)
        assertEquals(100.0, state.runningDistanceKm)
        assertEquals(85, state.totalActivities)
    }
}
