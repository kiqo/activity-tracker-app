package com.activitytracker.app.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.activitytracker.app.domain.model.ActivityType
import com.activitytracker.app.presentation.navigation.Screen

/**
 * Home screen displaying current tracking status and navigation to other screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Tracker") }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as HomeUiState.Error).errorState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is HomeUiState.Success -> {
                val state = uiState as HomeUiState.Success
                val isAutoDetectionEnabled by viewModel.isAutoDetectionEnabled.collectAsState()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Automatic Detection Toggle and Status
                    AutoDetectionCard(
                        isEnabled = isAutoDetectionEnabled,
                        automaticSession = state.automaticSession,
                        onToggle = { viewModel.toggleAutoDetection(it) }
                    )
                    
                    // Manual Tracking Controls
                    ManualTrackingCard(
                        manualSession = state.manualSession,
                        onStartTracking = { activityType -> viewModel.startTracking(activityType) },
                        onStopTracking = { viewModel.stopManualTracking() }
                    )
                    
                    // Today's Statistics
                    TodayStatisticsCard(
                        walkingKm = state.todayWalkingKm,
                        cyclingKm = state.todayCyclingKm,
                        runningKm = state.todayRunningKm,
                        steps = state.todaySteps
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Navigation buttons
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium
                    )

                    NavigationButton(
                        icon = Icons.Default.List,
                        title = "Activity History",
                        description = "View all tracked activities",
                        onClick = { navController.navigate(Screen.ActivityList.route) }
                    )

                    NavigationButton(
                        icon = Icons.Default.DirectionsBike,
                        title = "Find My Bike",
                        description = "Locate where you parked",
                        onClick = { navController.navigate(Screen.BikeLocation.route) }
                    )

                    NavigationButton(
                        icon = Icons.Default.BarChart,
                        title = "Statistics",
                        description = "View your activity stats",
                        onClick = { navController.navigate(Screen.Statistics.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun AutoDetectionCard(
    isEnabled: Boolean,
    automaticSession: com.activitytracker.app.domain.model.ActivitySession?,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Automatic Detection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEnabled) "Monitoring activities in background" else "Tap to enable automatic tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }
            
            // Show automatic session status if active
            if (automaticSession != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (automaticSession.activityType) {
                            ActivityType.IN_VEHICLE -> Icons.Default.DirectionsCar
                            ActivityType.CYCLING -> Icons.Default.DirectionsBike
                            ActivityType.WALKING -> Icons.Default.DirectionsWalk
                            ActivityType.RUNNING -> Icons.Default.DirectionsRun
                            else -> Icons.Default.DirectionsWalk
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-detected: ${automaticSession.activityType.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tracking in progress...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ManualTrackingCard(
    manualSession: com.activitytracker.app.domain.model.ActivitySession?,
    onStartTracking: (ActivityType) -> Unit,
    onStopTracking: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Manual Tracking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (manualSession != null) {
                // Show active manual session and stop button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Show which activity is active
                    Button(
                        onClick = { /* Already active */ },
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (manualSession.activityType) {
                                ActivityType.WALKING -> MaterialTheme.colorScheme.tertiary
                                ActivityType.CYCLING -> MaterialTheme.colorScheme.primary
                                ActivityType.RUNNING -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            disabledContainerColor = when (manualSession.activityType) {
                                ActivityType.WALKING -> MaterialTheme.colorScheme.tertiary
                                ActivityType.CYCLING -> MaterialTheme.colorScheme.primary
                                ActivityType.RUNNING -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (manualSession.activityType) {
                                    ActivityType.IN_VEHICLE -> Icons.Default.DirectionsCar
                                    ActivityType.CYCLING -> Icons.Default.DirectionsBike
                                    ActivityType.WALKING -> Icons.Default.DirectionsWalk
                                    ActivityType.RUNNING -> Icons.Default.DirectionsRun
                                    else -> Icons.Default.DirectionsWalk
                                },
                                contentDescription = null
                            )
                            Text(
                                text = when (manualSession.activityType) {
                                    ActivityType.IN_VEHICLE -> "Driving"
                                    ActivityType.CYCLING -> "Cycling"
                                    ActivityType.WALKING -> "Walking"
                                    ActivityType.RUNNING -> "Running"
                                    else -> "Active"
                                },
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    // Stop button
                    Button(
                        onClick = onStopTracking,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Text("Stop", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Activity type buttons when not tracking
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Walking button
                    Button(
                        onClick = { onStartTracking(ActivityType.WALKING) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DirectionsWalk, contentDescription = null)
                            Text("Walk", fontSize = 12.sp)
                        }
                    }
                    
                    // Cycling button
                    Button(
                        onClick = { onStartTracking(ActivityType.CYCLING) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DirectionsBike, contentDescription = null)
                            Text("Bike", fontSize = 12.sp)
                        }
                    }
                    
                    // Running button
                    Button(
                        onClick = { onStartTracking(ActivityType.RUNNING) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null)
                            Text("Run", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayStatisticsCard(
    walkingKm: Double,
    cyclingKm: Double,
    runningKm: Double,
    steps: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Today's Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Walking
            StatisticRow(
                icon = Icons.Default.DirectionsWalk,
                label = "Walking",
                value = String.format("%.1f km", walkingKm),
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cycling
            StatisticRow(
                icon = Icons.Default.DirectionsBike,
                label = "Cycling",
                value = String.format("%.1f km", cyclingKm),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Running
            StatisticRow(
                icon = Icons.Default.DirectionsRun,
                label = "Running",
                value = String.format("%.1f km", runningKm),
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Steps
            StatisticRow(
                icon = Icons.Default.DirectionsWalk,
                label = "Steps",
                value = steps.toString(),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun StatisticRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
