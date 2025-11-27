package com.activitytracker.app.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.model.TimeInterval
import com.activitytracker.app.domain.usecase.GetActivityStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatisticsUiState(
    val selectedInterval: TimeInterval = TimeInterval.DAILY,
    val walkingDistanceKm: Double = 0.0,
    val cyclingDistanceKm: Double = 0.0,
    val runningDistanceKm: Double = 0.0,
    val totalSteps: Int = 0,
    val walkingCount: Int = 0,
    val cyclingCount: Int = 0,
    val runningCount: Int = 0,
    val vehicleCount: Int = 0,
    val totalActivities: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getActivityStatisticsUseCase: GetActivityStatisticsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    fun selectTimeInterval(interval: TimeInterval) {
        _uiState.value = _uiState.value.copy(selectedInterval = interval)
        loadStatistics()
    }
    
    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                getActivityStatisticsUseCase(
                    timeInterval = _uiState.value.selectedInterval
                ).collect { statistics ->
                    _uiState.value = _uiState.value.copy(
                        walkingDistanceKm = statistics.walkingDistanceKm,
                        cyclingDistanceKm = statistics.cyclingDistanceKm,
                        runningDistanceKm = statistics.runningDistanceKm,
                        totalSteps = statistics.totalSteps,
                        walkingCount = statistics.walkingCount,
                        cyclingCount = statistics.cyclingCount,
                        runningCount = statistics.runningCount,
                        vehicleCount = statistics.vehicleCount,
                        totalActivities = statistics.walkingCount + 
                                        statistics.cyclingCount + 
                                        statistics.runningCount + 
                                        statistics.vehicleCount,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
