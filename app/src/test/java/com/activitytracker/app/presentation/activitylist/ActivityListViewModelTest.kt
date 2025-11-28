package com.activitytracker.app.presentation.activitylist

import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.repository.ActivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityListViewModelTest {

    private lateinit var viewModel: ActivityListViewModel
    private lateinit var activityRepository: ActivityRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads all activities without filter`() = runTest {
        // Given
        val sessions = listOf(
            ActivitySession(1L, ActivityType.CYCLING, 1000L, 2000L, 5000.0, 5.0, 0),
            ActivitySession(2L, ActivityType.RUNNING, 3000L, 4000L, 3000.0, 3.0, 4000),
            ActivitySession(3L, ActivityType.WALKING, 5000L, 6000L, 1000.0, 1.0, 1500)
        )
        whenever(activityRepository.getAllSessions())
            .thenReturn(flow { emit(sessions) })

        // When
        viewModel = ActivityListViewModel(activityRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityListUiState.Success
        assertEquals(3, state.sessions.size)
        assertNull(viewModel.selectedFilter.value)
    }

    @Test
    fun `setFilter filters activities by type`() = runTest {
        // Given
        val sessions = listOf(
            ActivitySession(1L, ActivityType.CYCLING, 1000L, 2000L, 5000.0, 5.0, 0),
            ActivitySession(2L, ActivityType.RUNNING, 3000L, 4000L, 3000.0, 3.0, 4000),
            ActivitySession(3L, ActivityType.CYCLING, 5000L, 6000L, 8000.0, 8.0, 0)
        )
        whenever(activityRepository.getAllSessions())
            .thenReturn(flow { emit(sessions) })

        viewModel = ActivityListViewModel(activityRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.setFilter(ActivityType.CYCLING)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityListUiState.Success
        assertEquals(2, state.sessions.size)
        assertTrue(state.sessions.all { it.activityType == ActivityType.CYCLING })
        assertEquals(ActivityType.CYCLING, viewModel.selectedFilter.value)
    }

    @Test
    fun `setFilter to null shows all activities`() = runTest {
        // Given
        val sessions = listOf(
            ActivitySession(1L, ActivityType.CYCLING, 1000L, 2000L, 5000.0, 5.0, 0),
            ActivitySession(2L, ActivityType.RUNNING, 3000L, 4000L, 3000.0, 3.0, 4000),
            ActivitySession(3L, ActivityType.WALKING, 5000L, 6000L, 1000.0, 1.0, 1500)
        )
        whenever(activityRepository.getAllSessions())
            .thenReturn(flow { emit(sessions) })

        viewModel = ActivityListViewModel(activityRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.setFilter(ActivityType.CYCLING)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.setFilter(null)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityListUiState.Success
        assertEquals(3, state.sessions.size)
        assertNull(viewModel.selectedFilter.value)
    }

    @Test
    fun `empty activity list shows success with empty list`() = runTest {
        // Given
        whenever(activityRepository.getAllSessions())
            .thenReturn(flow { emit(emptyList()) })

        // When
        viewModel = ActivityListViewModel(activityRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityListUiState.Success
        assertTrue(state.sessions.isEmpty())
    }

    @Test
    fun `repository error shows Error state`() = runTest {
        // Given
        whenever(activityRepository.getAllSessions())
            .thenReturn(flow { throw Exception("Database error") })

        // When
        viewModel = ActivityListViewModel(activityRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityListUiState.Error
        assertEquals("Database error", state.message)
    }

    @Test
    fun `filter with no matching activities shows empty list`() = runTest {
        // Given
        val sessions = listOf(
            ActivitySession(1L, ActivityType.CYCLING, 1000L, 2000L, 5000.0, 5.0, 0),
            ActivitySession(2L, ActivityType.CYCLING, 3000L, 4000L, 3000.0, 3.0, 0)
        )
        whenever(activityRepository.getAllSessions())
            .thenReturn(flow { emit(sessions) })

        viewModel = ActivityListViewModel(activityRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.setFilter(ActivityType.RUNNING)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as ActivityListUiState.Success
        assertTrue(state.sessions.isEmpty())
    }
}
