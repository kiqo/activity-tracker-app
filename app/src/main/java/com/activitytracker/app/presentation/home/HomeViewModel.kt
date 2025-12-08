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
    @ApplicationContext private val context: Context,
    private val logger: com.activitytracker.app.util.Logger
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private val _isAutoDetectionEnabled = MutableStateFlow(false)
    val isAutoDetectionEnabled: StateFlow<Boolean> = _isAutoDetectionEnabled.asStateFlow()

    init {
        logger.d("HomeViewModel initialized")
        loadTodayStatistics()
        observeActiveSessions()
    }
    
    /**
     * Observe active sessions to update UI state.
     * Handles initial transition from Loading to Success.
     */
    private fun observeActiveSessions() {
        logger.d("Starting to observe active sessions")
        viewModelScope.launch {
            activityRepository.getActiveSessions().collect { activeSessions ->
                logger.d("Active sessions updated: count=${activeSessions.size}")
                val manualSession = activeSessions.firstOrNull { it.isManuallyStarted }
                val automaticSession = activeSessions.firstOrNull { !it.isManuallyStarted }
                
                logger.d("Manual session: ${manualSession?.let { "id=${it.id}, type=${it.activityType}" } ?: "none"}")
                logger.d("Automatic session: ${automaticSession?.let { "id=${it.id}, type=${it.activityType}" } ?: "none"}")
                
                val currentState = _uiState.value
                when (currentState) {
                    is HomeUiState.Success -> {
                        logger.d("Updating sessions in Success state")
                        // Update sessions in existing Success state
                        _uiState.value = currentState.copy(
                            manualSession = manualSession,
                            automaticSession = automaticSession
                        )
                    }
                    is HomeUiState.Loading -> {
                        logger.i("Transitioning from Loading to Success state")
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
                        logger.e("Staying in Error state, not updating sessions")
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
        logger.d("loadTodayStatistics")
        viewModelScope.launch {
            try {
                getActivityStatisticsUseCase(TimeInterval.DAILY).collect { statistics ->
                    logger.d("Statistics received: walking=${statistics.walkingDistanceKm}km, cycling=${statistics.cyclingDistanceKm}km, running=${statistics.runningDistanceKm}km, steps=${statistics.totalSteps}")
                    val currentState = _uiState.value
                    // Only update if already in Success state
                    // observeActiveSessions will handle the initial transition from Loading to Success
                    if (currentState is HomeUiState.Success) {
                        logger.d("Updating statistics in Success state")
                        _uiState.value = currentState.copy(
                            todayWalkingKm = statistics.walkingDistanceKm,
                            todayCyclingKm = statistics.cyclingDistanceKm,
                            todayRunningKm = statistics.runningDistanceKm,
                            todaySteps = statistics.totalSteps
                        )
                    } else {
                        logger.d("Not updating statistics, current state: ${currentState::class.simpleName}")
                    }
                    // If state is Loading or Error, do nothing - stay in that state
                }
            } catch (e: Exception) {
                logger.e(e, "Failed to load today's statistics")
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
        logger.i("Starting manual tracking: activityType=$activityType")
        viewModelScope.launch {
            try {
                // Start new manual session (use case handles stopping existing manual session)
                val sessionId = startActivityTrackingUseCase(activityType, isManual = true)

                val currentState = _uiState.value
                logger.i("Manual tracking started successfully: sessionId=$sessionId, HomeUiState=$currentState")
                if (currentState is HomeUiState.Success) {
                    _uiState.value = currentState.copy(error = null)
                }
            } catch (e: Exception) {
                val currentState = _uiState.value
                logger.e(e, "Failed to start manual tracking: activityType=$activityType, HomeUiState=$currentState")

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
        logger.i("Stopping manual tracking")
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is HomeUiState.Success && currentState.manualSession != null) {
                    val sessionId = currentState.manualSession.id
                    logger.d("Stopping manual session: sessionId=$sessionId")
                    stopActivityTrackingUseCase(sessionId)
                    logger.i("Manual tracking stopped successfully: sessionId=$sessionId")
                    // observeActiveSessions will automatically update the UI state
                    // Just reload statistics to show updated data
                    loadTodayStatistics()
                } else {
                    logger.w("No manual session to stop")
                }
            } catch (e: Exception) {
                logger.e(e, "Failed to stop manual tracking")
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
        logger.d("Clearing error state")
        val currentState = _uiState.value
        if (currentState is HomeUiState.Success) {
            _uiState.value = currentState.copy(error = null)
        }
    }
    
    /**
     * Toggle automatic activity detection.
     */
    fun toggleAutoDetection(enabled: Boolean) {
        logger.i("Toggling auto detection: enabled=$enabled")
        _isAutoDetectionEnabled.value = enabled
        
        if (enabled) {
            logger.d("Starting ActivityRecognitionService")
            // Start ActivityRecognitionService
            val intent = Intent(context, ActivityRecognitionService::class.java).apply {
                action = ActivityRecognitionService.ACTION_START_TRACKING
            }
            ContextCompat.startForegroundService(context, intent)
        } else {
            logger.d("Stopping ActivityRecognitionService")
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
