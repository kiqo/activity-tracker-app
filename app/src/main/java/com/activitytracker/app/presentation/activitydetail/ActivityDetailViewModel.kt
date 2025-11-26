package com.activitytracker.app.presentation.activitydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.repository.ActivityRepository
import com.activitytracker.app.domain.repository.LocationRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for ActivityDetailScreen.
 * Manages activity session details and route data.
 */
@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActivityDetailUiState>(ActivityDetailUiState.Loading)
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    /**
     * Load activity session and route points.
     */
    fun loadSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                val session = activityRepository.getSessionById(sessionId).first()
                if (session == null) {
                    _uiState.value = ActivityDetailUiState.Error("Session not found")
                    return@launch
                }

                // Get accurate location points for route (accuracy < 50m)
                val locationPoints = locationRepository
                    .getAccurateLocationPointsForSession(sessionId)
                    .first()

                val routePoints = locationPoints.map { point ->
                    LatLng(point.latitude, point.longitude)
                }

                _uiState.value = ActivityDetailUiState.Success(
                    session = session,
                    routePoints = routePoints
                )
            } catch (e: Exception) {
                _uiState.value = ActivityDetailUiState.Error(
                    e.message ?: "Failed to load session"
                )
            }
        }
    }

    /**
     * Delete the activity session and navigate back.
     */
    fun deleteSession(sessionId: Long, navController: NavController) {
        viewModelScope.launch {
            try {
                activityRepository.deleteSession(sessionId)
                navController.navigateUp()
            } catch (e: Exception) {
                _uiState.value = ActivityDetailUiState.Error(
                    e.message ?: "Failed to delete session"
                )
            }
        }
    }
}

/**
 * UI state for ActivityDetailScreen.
 */
sealed class ActivityDetailUiState {
    object Loading : ActivityDetailUiState()
    data class Success(
        val session: ActivitySession,
        val routePoints: List<LatLng>
    ) : ActivityDetailUiState()
    data class Error(val message: String) : ActivityDetailUiState()
}
