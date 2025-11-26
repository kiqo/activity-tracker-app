package com.activitytracker.app.presentation.activitylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for ActivityListScreen.
 * Manages activity session list and filtering.
 */
@HiltViewModel
class ActivityListViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<ActivityType?>(null)
    val selectedFilter: StateFlow<ActivityType?> = _selectedFilter.asStateFlow()

    private val _uiState = MutableStateFlow<ActivityListUiState>(ActivityListUiState.Loading)
    val uiState: StateFlow<ActivityListUiState> = _uiState.asStateFlow()

    init {
        loadActivities()
    }

    /**
     * Load all activity sessions and apply filter.
     */
    private fun loadActivities() {
        viewModelScope.launch {
            try {
                combine(
                    activityRepository.getAllSessions(),
                    _selectedFilter
                ) { sessions, filter ->
                    if (filter == null) {
                        sessions
                    } else {
                        sessions.filter { it.activityType == filter }
                    }
                }.collect { filteredSessions ->
                    _uiState.value = ActivityListUiState.Success(filteredSessions)
                }
            } catch (e: Exception) {
                _uiState.value = ActivityListUiState.Error(
                    e.message ?: "Failed to load activities"
                )
            }
        }
    }

    /**
     * Set activity type filter.
     */
    fun setFilter(activityType: ActivityType?) {
        _selectedFilter.value = activityType
    }
}

/**
 * UI state for ActivityListScreen.
 */
sealed class ActivityListUiState {
    object Loading : ActivityListUiState()
    data class Success(val sessions: List<ActivitySession>) : ActivityListUiState()
    data class Error(val message: String) : ActivityListUiState()
}
