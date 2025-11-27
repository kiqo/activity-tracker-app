package com.activitytracker.app.presentation.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.activitytracker.app.domain.model.TimeInterval

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time interval selector
            TimeIntervalSelector(
                selectedInterval = uiState.selectedInterval,
                onIntervalSelected = { viewModel.selectTimeInterval(it) }
            )
            
            // Statistics cards
            StatisticsCard(
                title = "Walking",
                distance = uiState.walkingDistanceKm,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            StatisticsCard(
                title = "Cycling",
                distance = uiState.cyclingDistanceKm,
                color = MaterialTheme.colorScheme.primary
            )
            
            StatisticsCard(
                title = "Running",
                distance = uiState.runningDistanceKm,
                color = MaterialTheme.colorScheme.secondary
            )
            
            // Steps card
            StepsCard(steps = uiState.totalSteps)
            
            // Activity distribution
            if (uiState.totalActivities > 0) {
                ActivityDistributionCard(
                    walkingCount = uiState.walkingCount,
                    cyclingCount = uiState.cyclingCount,
                    runningCount = uiState.runningCount,
                    vehicleCount = uiState.vehicleCount
                )
            }
        }
    }
}

@Composable
fun TimeIntervalSelector(
    selectedInterval: TimeInterval,
    onIntervalSelected: (TimeInterval) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Time Period",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeInterval.values().forEach { interval ->
                    FilterChip(
                        selected = selectedInterval == interval,
                        onClick = { onIntervalSelected(interval) },
                        label = { Text(interval.name.lowercase().capitalize()) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsCard(
    title: String,
    distance: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format("%.2f", distance),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "km",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StepsCard(steps: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Total Steps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = steps.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "steps",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ActivityDistributionCard(
    walkingCount: Int,
    cyclingCount: Int,
    runningCount: Int,
    vehicleCount: Int
) {
    val total = walkingCount + cyclingCount + runningCount + vehicleCount
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Activity Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ActivityDistributionItem(
                label = "Walking",
                count = walkingCount,
                total = total,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ActivityDistributionItem(
                label = "Cycling",
                count = cyclingCount,
                total = total,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ActivityDistributionItem(
                label = "Running",
                count = runningCount,
                total = total,
                color = MaterialTheme.colorScheme.secondary
            )
            
            if (vehicleCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                ActivityDistributionItem(
                    label = "Vehicle",
                    count = vehicleCount,
                    total = total,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ActivityDistributionItem(
    label: String,
    count: Int,
    total: Int,
    color: androidx.compose.ui.graphics.Color
) {
    val percentage = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "$count ($percentage%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = if (total > 0) count.toFloat() / total else 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}
