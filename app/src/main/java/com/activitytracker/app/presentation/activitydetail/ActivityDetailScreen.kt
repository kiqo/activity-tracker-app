package com.activitytracker.app.presentation.activitydetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.activitytracker.app.domain.model.ActivitySession
import com.activitytracker.app.domain.model.ActivityType
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen displaying detailed activity information with route on Google Maps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    sessionId: Long,
    navController: NavController,
    viewModel: ActivityDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteSession(sessionId, navController) }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ActivityDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ActivityDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Map showing route
                    if (state.routePoints.isNotEmpty()) {
                        RouteMap(
                            routePoints = state.routePoints,
                            activityType = state.session.activityType,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    }

                    // Activity statistics
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ActivityHeader(state.session)
                        ActivityStats(state.session)
                    }
                }
            }
            is ActivityDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteMap(
    routePoints: List<LatLng>,
    activityType: ActivityType,
    modifier: Modifier = Modifier
) {
    // Optimize: Limit polyline points for very long routes (max 500 points)
    val optimizedPoints = remember(routePoints) {
        if (routePoints.size > 500) {
            val step = routePoints.size / 500
            routePoints.filterIndexed { index, _ -> index % step == 0 }
        } else {
            routePoints
        }
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            optimizedPoints.firstOrNull() ?: LatLng(0.0, 0.0),
            15f
        )
    }

    LaunchedEffect(optimizedPoints) {
        if (optimizedPoints.size >= 2) {
            // Calculate center and zoom to show entire route
            val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
            optimizedPoints.forEach { bounds.include(it) }
            val latLngBounds = bounds.build()
            
            // Calculate center point
            val center = latLngBounds.center
            cameraPositionState.position = CameraPosition.fromLatLngZoom(center, 14f)
        }
    }
    
    // Lifecycle management: Clean up map resources
    DisposableEffect(Unit) {
        onDispose {
            // Map resources are automatically cleaned up by Compose
            android.util.Log.d("RouteMap", "Map disposed, resources released")
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapType = MapType.NORMAL,
            isTrafficEnabled = false, // Disable traffic for better performance
            isIndoorEnabled = false // Disable indoor maps for better performance
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false, // Disable toolbar for cleaner UI
            tiltGesturesEnabled = false, // Disable tilt for better performance
            rotationGesturesEnabled = false // Disable rotation for better performance
        )
    ) {
        // Draw route polyline with optimized points
        if (optimizedPoints.size >= 2) {
            Polyline(
                points = optimizedPoints,
                color = getActivityColor(activityType),
                width = 8f
            )
        }

        // Start marker (use original first point for accuracy)
        if (routePoints.isNotEmpty()) {
            Marker(
                state = MarkerState(position = routePoints.first()),
                title = "Start"
            )
        }

        // End marker
        if (routePoints.size > 1) {
            Marker(
                state = MarkerState(position = routePoints.last()),
                title = "End"
            )
        }
    }
}

@Composable
private fun ActivityHeader(session: ActivitySession) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getActivityIcon(session.activityType),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = getActivityColor(session.activityType)
        )
        Column {
            Text(
                text = session.activityType.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = formatDate(session.startTime),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityStats(session: ActivitySession) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatRow(
                icon = Icons.Default.Straighten,
                label = "Distance",
                value = formatDistance(session.totalDistance)
            )
            StatRow(
                icon = Icons.Default.Schedule,
                label = "Duration",
                value = formatDuration(session.startTime, session.endTime)
            )
            StatRow(
                icon = Icons.Default.Speed,
                label = "Average Speed",
                value = formatSpeed(session.averageSpeed)
            )
            if (session.stepCount > 0) {
                StatRow(
                    icon = Icons.Default.DirectionsWalk,
                    label = "Steps",
                    value = session.stepCount.toString()
                )
            }
        }
    }
}

@Composable
private fun StatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun getActivityIcon(activityType: ActivityType) = when (activityType) {
    ActivityType.CYCLING -> Icons.Default.DirectionsBike
    ActivityType.RUNNING -> Icons.Default.DirectionsRun
    ActivityType.WALKING -> Icons.Default.DirectionsWalk
    ActivityType.IN_VEHICLE -> Icons.Default.DirectionsCar
}

@Composable
private fun getActivityColor(activityType: ActivityType) = when (activityType) {
    ActivityType.CYCLING -> androidx.compose.ui.graphics.Color(0xFF2196F3) // Blue
    ActivityType.RUNNING -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
    ActivityType.WALKING -> androidx.compose.ui.graphics.Color(0xFFFF9800) // Orange
    ActivityType.IN_VEHICLE -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDistance(meters: Double): String {
    return if (meters < 1000) {
        "${meters.toInt()} m"
    } else {
        "%.2f km".format(meters / 1000)
    }
}

private fun formatDuration(startTime: Long, endTime: Long?): String {
    if (endTime == null) return "Active"
    val durationMs = endTime - startTime
    val seconds = (durationMs / 1000).toInt()
    val minutes = seconds / 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    val remainingSeconds = seconds % 60
    
    return when {
        hours > 0 -> "${hours}h ${remainingMinutes}m"
        minutes > 0 -> "${minutes}m ${remainingSeconds}s"
        else -> "${seconds}s"
    }
}

private fun formatSpeed(metersPerSecond: Double): String {
    val kmPerHour = metersPerSecond * 3.6
    return "%.1f km/h".format(kmPerHour)
}
