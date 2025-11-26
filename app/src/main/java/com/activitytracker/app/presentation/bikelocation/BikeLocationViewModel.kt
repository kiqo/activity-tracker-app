package com.activitytracker.app.presentation.bikelocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activitytracker.app.domain.usecase.GetBikeLocationUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.*

/**
 * ViewModel for BikeLocationScreen.
 * Manages bike location retrieval and distance calculation.
 */
@HiltViewModel
class BikeLocationViewModel @Inject constructor(
    private val getBikeLocationUseCase: GetBikeLocationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BikeLocationUiState>(BikeLocationUiState.Loading)
    val uiState: StateFlow<BikeLocationUiState> = _uiState.asStateFlow()

    /**
     * Load the last bike location.
     */
    fun loadBikeLocation() {
        viewModelScope.launch {
            try {
                val bikeLocation = getBikeLocationUseCase()
                
                if (bikeLocation == null) {
                    _uiState.value = BikeLocationUiState.NoBikeFound
                    return@launch
                }

                val latLng = LatLng(bikeLocation.latitude, bikeLocation.longitude)
                val lastParkedTime = formatTime(bikeLocation.timestamp)
                
                // TODO: Get current location to calculate distance
                // For now, we'll show null distance
                val distanceFromCurrent: String? = null

                _uiState.value = BikeLocationUiState.Success(
                    bikeLocation = latLng,
                    lastParkedTime = lastParkedTime,
                    distanceFromCurrent = distanceFromCurrent
                )
            } catch (e: Exception) {
                _uiState.value = BikeLocationUiState.Error(
                    e.message ?: "Failed to load bike location"
                )
            }
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        return "Last parked: ${sdf.format(Date(timestamp))}"
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val earthRadiusMeters = 6371000.0
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distanceMeters = earthRadiusMeters * c
        
        return if (distanceMeters < 1000) {
            "${distanceMeters.toInt()} m"
        } else {
            "%.2f km".format(distanceMeters / 1000)
        }
    }
}

/**
 * UI state for BikeLocationScreen.
 */
sealed class BikeLocationUiState {
    object Loading : BikeLocationUiState()
    data class Success(
        val bikeLocation: LatLng,
        val lastParkedTime: String,
        val distanceFromCurrent: String?
    ) : BikeLocationUiState()
    object NoBikeFound : BikeLocationUiState()
    data class Error(val message: String) : BikeLocationUiState()
}
