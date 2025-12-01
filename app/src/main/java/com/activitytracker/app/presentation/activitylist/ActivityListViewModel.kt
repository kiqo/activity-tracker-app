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
 * Session type filter for manual/automatic sessions.
 */
enum class SessionTypeFilter {
    ALL, MANUAL, AUTOMATIC
}

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
    
    private val _sessionTypeFilter = MutableStateFlow(SessionTypeFilter.ALL)
    val sessionTypeFilter: StateFlow<SessionTypeFilter> = _sessionTypeFilter.asStateFlow()

    private val _uiState = MutableStateFlow<ActivityListUiState>(ActivityListUiState.Loading)
    val uiState: StateFlow<ActivityListUiState> = _uiState.asStateFlow()

    init {
        loadActivities()
    }

    /**
     * Load all activity sessions and apply filters.
     */
    private fun loadActivities() {
        viewModelScope.launch {
            try {
                combine(
                    activityRepository.getAllSessions(),
                    _selectedFilter,
                    _sessionTypeFilter
                ) { sessions, activityFilter, sessionTypeFilter ->
                    var filtered = sessions
                    
                    // Apply activity type filter
                    if (activityFilter != null) {
                        filtered = filtered.filter { it.activityType == activityFilter }
                    }
                    
                    // Apply session type filter (manual/automatic)
                    filtered = when (sessionTypeFilter) {
                        SessionTypeFilter.MANUAL -> filtered.filter { it.isManuallyStarted }
                        SessionTypeFilter.AUTOMATIC -> filtered.filter { !it.isManuallyStarted }
                        SessionTypeFilter.ALL -> filtered
                    }
                    
                    filtered
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
    
    /**
     * Set session type filter (manual/automatic).
     */
    fun setSessionTypeFilter(filter: SessionTypeFilter) {
        _sessionTypeFilter.value = filter
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
