package com.activitytracker.app.presentation.bikelocation

import com.activitytracker.app.domain.model.LocationPoint
import com.activitytracker.app.domain.usecase.GetBikeLocationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BikeLocationViewModelTest {

    private lateinit var viewModel: BikeLocationViewModel
    private lateinit var getBikeLocationUseCase: GetBikeLocationUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getBikeLocationUseCase = mock()
        viewModel = BikeLocationViewModel(getBikeLocationUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBikeLocation with valid location shows success state`() = runTest {
        // Given
        val locationPoint = LocationPoint(
            id = 1L,
            sessionId = 1L,
            latitude = 40.7128,
            longitude = -74.0060,
            altitude = null,
            accuracy = 10f,
            timestamp = 1609459200000L // Jan 1, 2021
        )
        whenever(getBikeLocationUseCase()).thenReturn(locationPoint)

        // When
        viewModel.loadBikeLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as BikeLocationUiState.Success
        assertEquals(40.7128, state.bikeLocation.latitude, 0.0001)
        assertEquals(-74.0060, state.bikeLocation.longitude, 0.0001)
        assertTrue(state.lastParkedTime.contains("Last parked:"))
        assertNull(state.distanceFromCurrent)
    }

    @Test
    fun `loadBikeLocation with null location shows NoBikeFound state`() = runTest {
        // Given
        whenever(getBikeLocationUseCase()).thenReturn(null)

        // When
        viewModel.loadBikeLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.uiState.value is BikeLocationUiState.NoBikeFound)
    }

    @Test
    fun `loadBikeLocation with error shows Error state`() = runTest {
        // Given
        whenever(getBikeLocationUseCase())
            .thenAnswer { throw Exception("Database error") }

        // When
        viewModel.loadBikeLocation()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value as BikeLocationUiState.Error
        assertEquals("Database error", state.message)
    }

    @Test
    fun `initial state is Loading`() {
        // Then
        assertTrue(viewModel.uiState.value is BikeLocationUiState.Loading)
    }
}
