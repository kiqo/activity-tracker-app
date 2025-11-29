package com.activitytracker.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.usecase.GetActivityStatisticsUseCase
import com.activitytracker.app.domain.usecase.StartActivityTrackingUseCase
import com.activitytracker.app.domain.usecase.StopActivityTrackingUseCase
import com.activitytracker.app.domain.model.TimeInterval
import com.activitytracker.app.presentation.util.ErrorAction
import com.activitytracker.app.presentation.util.ErrorHandler
import com.activitytracker.app.presentation.util.ErrorState
import com.activitytracker.app.services.ActivityRecognitionService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val getActivityStatisticsUseCase: GetActivityStatisticsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _isAutoDetectionEnabled = MutableStateFlow(false)
    val isAutoDetectionEnabled: StateFlow<Boolean> = _isAutoDetectionEnabled.asStateFlow()

    init {
        loadTodayStatistics()
    }

    /**
     * Load today's activity statistics.
     */
    fun loadTodayStatistics() {
        viewModelScope.launch {
            try {
                getActivityStatisticsUseCase(TimeInterval.DAILY).collect { statistics ->
                    val currentState = _uiState.value
                    val isTracking = if (currentState is HomeUiState.Success) currentState.isTracking else false
                    val currentSessionId = if (currentState is HomeUiState.Success) currentState.currentSessionId else null
                    val currentActivityType = if (currentState is HomeUiState.Success) currentState.currentActivityType else null
                    
                    _uiState.value = HomeUiState.Success(
                        isTracking = isTracking,
                        currentSessionId = currentSessionId,
                        currentActivityType = currentActivityType,
                        todayWalkingKm = statistics.walkingDistanceKm,
                        todayCyclingKm = statistics.cyclingDistanceKm,
                        todayRunningKm = statistics.runningDistanceKm,
                        todaySteps = statistics.totalSteps,
                        error = null
                    )
                }
            } catch (e: Exception) {
                val errorState = ErrorState(
                    message = ErrorHandler.getErrorMessage(e),
                    isRecoverable = true,
                    action = ErrorAction.Retry
                )
                _uiState.value = HomeUiState.Error(errorState)
            }
        }
    }

    /**
     * Start tracking a specific activity type.
     * Stops any existing session before starting a new one.
     */
    fun startTracking(activityType: ActivityType) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                
                // Stop existing session if any
                if (currentState is HomeUiState.Success && currentState.currentSessionId != null) {
                    stopActivityTrackingUseCase(currentState.currentSessionId)
                }
                
                // Start new session
                val sessionId = startActivityTrackingUseCase(activityType)
                
                if (currentState is HomeUiState.Success) {
                    _uiState.value = currentState.copy(
                        isTracking = true,
                        currentSessionId = sessionId,
                        currentActivityType = activityType,
                        error = null
                    )
                }
            } catch (e: Exception) {
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    val errorState = ErrorState(
                        message = ErrorHandler.getErrorMessage(e),
                        isRecoverable = true,
                        action = if (e is SecurityException) ErrorAction.OpenSettings else ErrorAction.Retry
                    )
                    _uiState.value = currentState.copy(error = errorState)
                }
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
                        currentSessionId = null,
                        currentActivityType = null,
                        error = null
                    )
                    // Reload statistics to show updated data
                    loadTodayStatistics()
                }
            } catch (e: Exception) {
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    val errorState = ErrorState(
                        message = ErrorHandler.getErrorMessage(e),
                        isRecoverable = true,
                        action = ErrorAction.Retry
                    )
                    _uiState.value = currentState.copy(error = errorState)
                }
            }
        }
    }
    
    /**
     * Clear error state.
     */
    fun clearError() {
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(error = null)
        }
    }
    
    /**
     * Toggle automatic activity detection.
     */
    fun toggleAutoDetection(enabled: Boolean) {
        _isAutoDetectionEnabled.value = enabled
        
        if (enabled) {
            // Start ActivityRecognitionService
            val intent = Intent(context, ActivityRecognitionService::class.java).apply {
                action = ActivityRecognitionService.ACTION_START_TRACKING
            }
            ContextCompat.startForegroundService(context, intent)
        } else {
            // Stop ActivityRecognitionService
            val intent = Intent(context, ActivityRecognitionService::class.java).apply {
                action = ActivityRecognitionService.ACTION_STOP_TRACKING
            }
            context.startService(intent)
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
        val currentActivityType: ActivityType?,
        val todayWalkingKm: Double,
        val todayCyclingKm: Double,
        val todayRunningKm: Double,
        val todaySteps: Int,
        val error: ErrorState? = null
    ) : HomeUiState()
    
    data class Error(val errorState: ErrorState) : HomeUiState()
}
