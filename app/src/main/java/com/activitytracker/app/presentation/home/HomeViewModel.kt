package com.activitytracker.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.repository.ActivityRepository
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
 * Supports dual-track session management (manual + automatic).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val startActivityTrackingUseCase: StartActivityTrackingUseCase,
    private val stopActivityTrackingUseCase: StopActivityTrackingUseCase,
    private val getActivityStatisticsUseCase: GetActivityStatisticsUseCase,
    private val activityRepository: ActivityRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _isAutoDetectionEnabled = MutableStateFlow(false)
    val isAutoDetectionEnabled: StateFlow<Boolean> = _isAutoDetectionEnabled.asStateFlow()

    init {
        loadTodayStatistics()
        observeActiveSessions()
    }
    
    /**
     * Observe active sessions to update UI state.
     * Handles initial transition from Loading to Success.
     */
    private fun observeActiveSessions() {
        viewModelScope.launch {
            activityRepository.getActiveSessions().collect { activeSessions ->
                val manualSession = activeSessions.firstOrNull { it.isManuallyStarted }
                val automaticSession = activeSessions.firstOrNull { !it.isManuallyStarted }
                
                val currentState = _uiState.value
                when (currentState) {
                    is HomeUiState.Success -> {
                        // Update sessions in existing Success state
                        _uiState.value = currentState.copy(
                            manualSession = manualSession,
                            automaticSession = automaticSession
                        )
                    }
                    is HomeUiState.Loading -> {
                        // Initial load - transition to Success with sessions and default statistics
                        // Statistics will be updated by loadTodayStatistics flow
                        _uiState.value = HomeUiState.Success(
                            manualSession = manualSession,
                            automaticSession = automaticSession,
                            todayWalkingKm = 0.0,
                            todayCyclingKm = 0.0,
                            todayRunningKm = 0.0,
                            todaySteps = 0,
                            error = null
                        )
                    }
                    is HomeUiState.Error -> {
                        // Stay in error state
                    }
                }
            }
        }
    }

    /**
     * Load today's activity statistics.
     * Only updates statistics if state is already Success (to avoid premature transition from Loading).
     */
    fun loadTodayStatistics() {
        viewModelScope.launch {
            try {
                getActivityStatisticsUseCase(TimeInterval.DAILY).collect { statistics ->
                    val currentState = _uiState.value
                    // Only update if already in Success state
                    // observeActiveSessions will handle the initial transition from Loading to Success
                    if (currentState is HomeUiState.Success) {
                        _uiState.value = currentState.copy(
                            todayWalkingKm = statistics.walkingDistanceKm,
                            todayCyclingKm = statistics.cyclingDistanceKm,
                            todayRunningKm = statistics.runningDistanceKm,
                            todaySteps = statistics.totalSteps
                        )
                    }
                    // If state is Loading or Error, do nothing - stay in that state
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
     * Start tracking a specific activity type manually.
     * The use case will handle stopping any existing manual session.
     */
    fun startTracking(activityType: ActivityType) {
        viewModelScope.launch {
            try {
                // Start new manual session (use case handles stopping existing manual session)
                startActivityTrackingUseCase(activityType, isManual = true)
                
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success) {
                    _uiState.value = currentState.copy(error = null)
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
     * Stop the manual tracking session.
     */
    fun stopManualTracking() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success && currentState.manualSession != null) {
                    stopActivityTrackingUseCase(currentState.manualSession.id)
                    // observeActiveSessions will automatically update the UI state
                    // Just reload statistics to show updated data
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
 * Supports dual-track session management (manual + automatic).
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    
    data class Success(
        val manualSession: ActivitySession?,
        val automaticSession: ActivitySession?,
        val todayWalkingKm: Double,
        val todayCyclingKm: Double,
        val todayRunningKm: Double,
        val todaySteps: Int,
        val error: ErrorState? = null
    ) : HomeUiState()
    
    data class Error(val errorState: ErrorState) : HomeUiState()
}
