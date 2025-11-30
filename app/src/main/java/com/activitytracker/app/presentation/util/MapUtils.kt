package com.activitytracker.app.presentation.util

import androidx.compose.ui.graphics.Color
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.domain.model.LocationPoint
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

/**
 * Utility functions for Google Maps integration.
 */
object MapUtils {
    
    /**
     * Convert a LocationPoint to a LatLng for Google Maps.
     */
    fun LocationPoint.toLatLng(): LatLng {
        return LatLng(this.latitude, this.longitude)
    }
    
    /**
     * Convert a list of LocationPoints to LatLng list.
     */
    fun List<LocationPoint>.toLatLngList(): List<LatLng> {
        return this.map { it.toLatLng() }
    }
    
    /**
     * Calculate LatLngBounds from a list of location points.
     * Returns null if the list is empty.
     */
    fun calculateBounds(points: List<LocationPoint>): LatLngBounds? {
        if (points.isEmpty()) return null
        
        val builder = LatLngBounds.Builder()
        points.forEach { point ->
            builder.include(point.toLatLng())
        }
        return builder.build()
    }
    
    /**
     * Get the color for an activity type.
     * - Cycling: Blue
     * - Running: Green
     * - Walking: Orange
     * - Vehicle: Red
     */
    fun getColorForActivityType(activityType: ActivityType): Color {
        return when (activityType) {
            ActivityType.IN_VEHICLE -> Color(0xFFF44336) // Red
            ActivityType.CYCLING -> Color(0xFF2196F3) // Blue
            ActivityType.ON_FOOT -> Color(0xFFFF9800) // Orange
            ActivityType.STILL -> Color(0xFF9E9E9E) // Gray
            ActivityType.UNKNOWN -> Color(0xFF607D8B) // Blue Gray
            ActivityType.TILTING -> Color(0xFF9C27B0) // Purple
            ActivityType.WALKING -> Color(0xFFFF9800) // Orange
            ActivityType.RUNNING -> Color(0xFF4CAF50) // Green
        }
    }
    
    /**
     * Get the center point from a list of location points.
     * Returns null if the list is empty.
     */
    fun getCenterPoint(points: List<LocationPoint>): LatLng? {
        if (points.isEmpty()) return null
        
        val bounds = calculateBounds(points) ?: return null
        return bounds.center
    }
    
    /**
     * Calculate an appropriate zoom level based on the bounds.
     * This is a simplified calculation - Google Maps will handle the actual zoom.
     */
    fun calculateZoomLevel(bounds: LatLngBounds): Float {
        val latDiff = bounds.northeast.latitude - bounds.southwest.latitude
        val lngDiff = bounds.northeast.longitude - bounds.southwest.longitude
        val maxDiff = maxOf(latDiff, lngDiff)
        
        return when {
            maxDiff > 10 -> 5f
            maxDiff > 5 -> 7f
            maxDiff > 2 -> 9f
            maxDiff > 1 -> 11f
            maxDiff > 0.5 -> 13f
            maxDiff > 0.1 -> 15f
            else -> 17f
        }
    }
    
    /**
     * Filter location points by accuracy threshold.
     * Only includes points with accuracy less than the threshold.
     */
    fun filterByAccuracy(points: List<LocationPoint>, accuracyThreshold: Float = 50f): List<LocationPoint> {
        return points.filter { it.accuracy < accuracyThreshold }
    }
}
