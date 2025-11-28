package com.activitytracker.app.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * Utility object for handling errors and edge cases.
 */
object ErrorHandler {
    
    /**
     * Check if Google Play Services is available on the device.
     * @return ConnectionResult code
     */
    fun checkGooglePlayServices(context: Context): Int {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        return googleApiAvailability.isGooglePlayServicesAvailable(context)
    }
    
    /**
     * Check if Google Play Services is available and show error dialog if not.
     * @return true if available, false otherwise
     */
    fun isGooglePlayServicesAvailable(context: Context): Boolean {
        val resultCode = checkGooglePlayServices(context)
        return resultCode == ConnectionResult.SUCCESS
    }
    
    /**
     * Show a dialog to resolve Google Play Services issues.
     */
    fun showGooglePlayServicesErrorDialog(context: Context, resultCode: Int) {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        if (googleApiAvailability.isUserResolvableError(resultCode)) {
            // This will show a dialog to the user
            val activity = context as? android.app.Activity
            if (activity != null) {
                googleApiAvailability.getErrorDialog(
                    activity,
                    resultCode,
                    REQUEST_CODE_GOOGLE_PLAY_SERVICES
                )?.show()
            }
        }
    }
    
    /**
     * Check if location services are enabled on the device.
     */
    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) 
            as? android.location.LocationManager
        return locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true ||
               locationManager?.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) == true
    }
    
    /**
     * Open location settings.
     */
    fun openLocationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
    
    /**
     * Open app settings.
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
    
    /**
     * Get user-friendly error message for common errors.
     */
    fun getErrorMessage(error: Throwable): String {
        return when (error) {
            is SecurityException -> "Permission denied. Please grant the required permissions."
            is IllegalStateException -> "Service is not available. Please try again."
            is java.io.IOException -> "Network error. Please check your connection."
            else -> error.message ?: "An unexpected error occurred."
        }
    }
    
    private const val REQUEST_CODE_GOOGLE_PLAY_SERVICES = 9000
}

/**
 * Data class representing an error state.
 */
data class ErrorState(
    val message: String,
    val isRecoverable: Boolean = true,
    val action: ErrorAction? = null
)

/**
 * Sealed class representing possible error actions.
 */
sealed class ErrorAction {
    object OpenSettings : ErrorAction()
    object OpenLocationSettings : ErrorAction()
    object Retry : ErrorAction()
    object Dismiss : ErrorAction()
}
