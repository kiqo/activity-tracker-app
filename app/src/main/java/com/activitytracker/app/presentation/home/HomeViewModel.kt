package com.activitytracker.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.usecase.GetActivityStatisticsUseCase
import com.activitytracker.app.domain.usecase.StartActivityTrackingUseCase
import com.activitytracker.app.domain.usecase.StopActivityTrackingUseCase
import com.activitytracker.app.domain.model.TimeInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for HomeScreen.
 * Manages tracking state and today's activity statistics.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startActivityTrackingUseCase: StartActivityTrackingUseCase,
    private val stopActivityTrackingUseCase: StopActivityTrackingUseCase,
    private val getActivityStatisticsUseCase: GetActivityStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayStatistics()
    }

    /**
     * Load today's activity statistics.
     */
    fun loadTodayStatistics() {
        viewModelScope.launch {
            try {
                val statistics = getActivityStatisticsUseCase(TimeInterval.DAILY)
                _uiState.value = HomeUiState.Success(
                    isTracking = false,
                    currentSessionId = null,
                    todayWalkingKm = statistics.walkingDistanceKm,
                    todayCyclingKm = statistics.cyclingDistanceKm,
                    todayRunningKm = statistics.runningDistanceKm,
                    todaySteps = statistics.totalSteps
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to load statistics")
            }
        }
    }

    /**
     * Start tracking a specific activity type.
     */
    fun startTracking(activityType: ActivityType) {
        viewModelScope.launch {
            try {
                val sessionId = startActivityTrackingUseCase(activityType)
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    _uiState.value = currentState.copy(
                        isTracking = true,
                        currentSessionId = sessionId
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to start tracking")
            }
        }
    }

    /**
     * Stop the current tracking session.
     */
    fun stopTracking() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success && currentState.currentSessionId != null) {
                    stopActivityTrackingUseCase(currentState.currentSessionId)
                    _uiState.value = currentState.copy(
                        isTracking = false,
                        currentSessionId = null
                    )
                    // Reload statistics to show updated data
                    loadTodayStatistics()
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Failed to stop tracking")
            }
        }
    }
}

/**
 * UI state for HomeScreen.
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    
    data class Success(
        val isTracking: Boolean,
        val currentSessionId: Long?,
        val todayWalkingKm: Double,
        val todayCyclingKm: Double,
        val todayRunningKm: Double,
        val todaySteps: Int
    ) : HomeUiState()
    
    data class Error(val message: String) : HomeUiState()
}
