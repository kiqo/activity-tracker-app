package com.activitytracker.app.presentation.activitylist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.activitytracker.app.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen displaying chronological list of all activity sessions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityListScreen(
    navController: NavController,
    viewModel: ActivityListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity History") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            FilterChips(
                selectedFilter = selectedFilter,
                onFilterSelected = { viewModel.setFilter(it) }
            )

            // Activity list
            when (val state = uiState) {
                is ActivityListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ActivityListUiState.Success -> {
                    if (state.sessions.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.sessions) { session ->
                                ActivityListItem(
                                    session = session,
                                    onClick = {
                                        navController.navigate(
                                            Screen.ActivityDetail.createRoute(session.id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                is ActivityListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChips(
    selectedFilter: ActivityType?,
    onFilterSelected: (ActivityType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
            label = { Text("All") }
        )
        FilterChip(
            selected = selectedFilter == ActivityType.CYCLING,
            onClick = { onFilterSelected(ActivityType.CYCLING) },
            label = { Text("Cycling") },
            leadingIcon = { Icon(Icons.Default.DirectionsBike, null, Modifier.size(18.dp)) }
        )
        FilterChip(
            selected = selectedFilter == ActivityType.RUNNING,
            onClick = { onFilterSelected(ActivityType.RUNNING) },
            label = { Text("Running") },
            leadingIcon = { Icon(Icons.Default.DirectionsRun, null, Modifier.size(18.dp)) }
        )
        FilterChip(
            selected = selectedFilter == ActivityType.WALKING,
            onClick = { onFilterSelected(ActivityType.WALKING) },
            label = { Text("Walking") },
            leadingIcon = { Icon(Icons.Default.DirectionsWalk, null, Modifier.size(18.dp)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityListItem(
    session: ActivitySession,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon
            Icon(
                imageVector = getActivityIcon(session.activityType),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = getActivityColor(session.activityType)
            )

            // Activity details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.activityType.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatDate(session.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = formatDistance(session.totalDistance),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatDuration(session.startTime, session.endTime),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsRun,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "No activities yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Start tracking to see your activities here",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
    ActivityType.CYCLING -> MaterialTheme.colorScheme.primary
    ActivityType.RUNNING -> MaterialTheme.colorScheme.tertiary
    ActivityType.WALKING -> MaterialTheme.colorScheme.secondary
    ActivityType.IN_VEHICLE -> MaterialTheme.colorScheme.error
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
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
    val minutes = (durationMs / 1000 / 60).toInt()
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    
    return if (hours > 0) {
        "${hours}h ${remainingMinutes}m"
    } else {
        "${minutes}m"
    }
}
