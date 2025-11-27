package com.activitytracker.app.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.activitytracker.app.presentation.util.ErrorAction
import com.activitytracker.app.presentation.util.ErrorState

/**
 * Reusable error dialog component.
 */
@Composable
fun ErrorDialog(
    errorState: ErrorState,
    onDismiss: () -> Unit,
    onAction: (ErrorAction) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(errorState.message) },
        confirmButton = {
            when (errorState.action) {
                ErrorAction.OpenSettings -> {
                    TextButton(onClick = { onAction(ErrorAction.OpenSettings) }) {
                        Text("Open Settings")
                    }
                }
                ErrorAction.OpenLocationSettings -> {
                    TextButton(onClick = { onAction(ErrorAction.OpenLocationSettings) }) {
                        Text("Enable Location")
                    }
                }
                ErrorAction.Retry -> {
                    TextButton(onClick = { onAction(ErrorAction.Retry) }) {
                        Text("Retry")
                    }
                }
                ErrorAction.Dismiss, null -> {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            }
        },
        dismissButton = if (errorState.action != null && errorState.action != ErrorAction.Dismiss) {
            {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        } else null
    )
}

/**
 * Permission denied error dialog.
 */
@Composable
fun PermissionDeniedDialog(
    permissionName: String,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { 
            Text("$permissionName permission is required for this feature. Please grant the permission in app settings.") 
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Location services disabled dialog.
 */
@Composable
fun LocationDisabledDialog(
    onDismiss: () -> Unit,
    onEnableLocation: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Services Disabled") },
        text = { 
            Text("Location services are disabled. Please enable location services to track your activities.") 
        },
        confirmButton = {
            TextButton(onClick = onEnableLocation) {
                Text("Enable Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Google Play Services unavailable dialog.
 */
@Composable
fun GooglePlayServicesDialog(
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Google Play Services Required") },
        text = { 
            Text("This app requires Google Play Services to function properly. Please update Google Play Services.") 
        },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
