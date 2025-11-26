# Design Document

## Overview

The Activity Tracker App is a native Android application built using Kotlin and Jetpack Compose for the UI layer. The architecture follows Clean Architecture principles with clear separation between data, domain, and presentation layers. The app uses Google Play Services for activity recognition and location tracking, Room database for local persistence, and Google Maps SDK for Android for map rendering. The application runs entirely on-device without any backend services, requiring only internet connectivity for map tile downloads.

## Architecture

### Privacy and Offline-First Design

The application is designed with privacy and offline functionality as core principles (Requirement 4):

- **Local-Only Data Storage:** All activity sessions and location points stored exclusively in Room database on device (Requirements 4.1, 4.2)
- **No Backend Communication:** Zero data transmission to external servers (Requirement 4.5)
- **Offline Operation:** Full functionality for activity detection and location tracking without internet (Requirement 4.3)
- **Map Tile Caching:** Google Maps tiles cached automatically by SDK for offline route viewing (Requirement 4.4)

**Design Rationale:** This architecture ensures user privacy by eliminating any data transmission risk. The offline-first approach also improves reliability and performance since the app doesn't depend on network connectivity for core functionality. The only network usage is optional map tile downloads, which are cached for subsequent offline use.

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  (Jetpack Compose UI + ViewModels)                      │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  (Use Cases + Domain Models + Repository Interfaces)    │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  (Room Database + Repository Implementations)           │
└─────────────────────────────────────────────────────────┘
                          │
        ┌─────────────────┴─────────────────┐
        ▼                                   ▼
┌──────────────────┐              ┌──────────────────┐
│  Background      │              │   Map Services   │
│  Services        │              │  (Google Maps)   │
│  - Activity      │              └──────────────────┘
│    Recognition   │
│  - Location      │
│    Tracking      │
└──────────────────┘
```

### Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room
- **Coroutines**: Kotlin Coroutines + Flow
- **Activity Recognition**: Google Play Services Activity Recognition API
- **Location**: Google Play Services Location API (FusedLocationProviderClient)
- **Maps**: Google Maps SDK for Android
- **Permissions**: Accompanist Permissions library

## Components and Interfaces

### 1. Background Services

#### ActivityRecognitionService
Foreground service that monitors activity changes and manages activity sessions.

```kotlin
class ActivityRecognitionService : Service() {
    // Registers for activity recognition updates
    // Creates/ends activity sessions based on confidence thresholds
    // Triggers location tracking when activity detected
}
```

**Key Responsibilities:**
- Register with Activity Recognition API for updates every 30 seconds
- Filter activities with confidence > 75% (Requirement 1.2)
- Create new ActivitySession when activity detected (Requirement 1.2)
- End current session and create new session when activity type changes with confidence > 75% (Requirement 1.3)
- End session after 5 minutes of inactivity (Requirement 1.4)
- Start/stop LocationTrackingService based on session state

#### LocationTrackingService
Foreground service that tracks GPS location during active sessions.

```kotlin
class LocationTrackingService : Service() {
    // Requests location updates every 10 seconds
    // Stores location points with accuracy filtering
}
```

**Key Responsibilities:**
- Request location updates at 10-second intervals using FusedLocationProviderClient (Requirement 2.1)
- Store all location data (latitude, longitude, altitude, accuracy, timestamp) in database (Requirement 2.2)
- Include location points with accuracy < 50 meters in the route (Requirement 2.3)
- Exclude location points with accuracy >= 50 meters from route but continue monitoring (Requirement 2.4)
- Stop location updates when activity session ends (Requirement 2.5)
- Display persistent notification (required for foreground service)

### 2. Data Layer

#### Database Schema

**ActivitySession Entity**
```kotlin
@Entity(tableName = "activity_sessions")
data class ActivitySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityType: String, // CYCLING, RUNNING, WALKING, IN_VEHICLE
    val startTime: Long,
    val endTime: Long?,
    val totalDistance: Double = 0.0, // meters
    val averageSpeed: Double = 0.0, // m/s
    val stepCount: Int = 0
)
```

**LocationPoint Entity**
```kotlin
@Entity(
    tableName = "location_points",
    foreignKeys = [ForeignKey(
        entity = ActivitySessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float,
    val timestamp: Long
)
```

#### Repository Interfaces

```kotlin
interface ActivityRepository {
    fun getAllSessions(): Flow<List<ActivitySession>>
    fun getSessionById(id: Long): Flow<ActivitySession?>
    fun getSessionsInTimeRange(startTime: Long, endTime: Long): Flow<List<ActivitySession>>
    fun getLastCyclingSession(): Flow<ActivitySession?>
    suspend fun insertSession(session: ActivitySession): Long
    suspend fun updateSession(session: ActivitySession)
    suspend fun deleteSession(id: Long)
}

interface LocationRepository {
    fun getLocationPointsForSession(sessionId: Long): Flow<List<LocationPoint>>
    suspend fun insertLocationPoint(point: LocationPoint)
    suspend fun getLastLocationForSession(sessionId: Long): LocationPoint?
}
```

### 3. Domain Layer

#### Use Cases

```kotlin
class StartActivityTrackingUseCase(
    private val activityRepository: ActivityRepository
)

class StopActivityTrackingUseCase(
    private val activityRepository: ActivityRepository,
    private val locationRepository: LocationRepository
)

class GetActivityStatisticsUseCase(
    private val activityRepository: ActivityRepository
)

class GetBikeLocationUseCase(
    private val activityRepository: ActivityRepository,
    private val locationRepository: LocationRepository
)

class CalculateRouteDistanceUseCase(
    private val locationRepository: LocationRepository
)

class EstimateStepCountUseCase()
```

#### Domain Models

```kotlin
data class ActivitySession(
    val id: Long,
    val activityType: ActivityType,
    val startTime: Instant,
    val endTime: Instant?,
    val totalDistance: Double,
    val averageSpeed: Double,
    val stepCount: Int
)

enum class ActivityType {
    CYCLING, RUNNING, WALKING, IN_VEHICLE
}

data class LocationPoint(
    val id: Long,
    val sessionId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double?,
    val accuracy: Float,
    val timestamp: Instant
)

data class ActivityStatistics(
    val walkingDistanceKm: Double,
    val cyclingDistanceKm: Double,
    val runningDistanceKm: Double,
    val totalSteps: Int,
    val timeInterval: TimeInterval
)

enum class TimeInterval {
    DAILY, WEEKLY, MONTHLY
}
```

### 4. Presentation Layer

#### Screens

**HomeScreen**
- Displays current tracking status
- Shows quick stats (today's activities)
- Navigation to other screens
- Start/stop manual tracking button

**ActivityListScreen**
- Chronological list of all activity sessions (Requirement 3.1)
- Filter by activity type
- Shows activity type icon, date, duration, distance for each session (Requirement 3.1)
- Click to view details and route on map (Requirement 3.2)

**ActivityDetailScreen**
- Full activity session details
- Route visualization on Google Maps (Requirement 3.2)
- Route rendered as continuous line connecting location points chronologically (Requirement 3.3)
- Map centered to show entire route (Requirement 3.4)
- Statistics (distance, duration, average speed, steps) (Requirements 7.1-7.4)
- Delete activity option

**BikeLocationScreen**
- Retrieves most recent cycling session (Requirement 6.1)
- Gets last location point from that session (Requirement 6.2)
- Google Maps centered on bike marker location (Requirement 6.4)
- Bike marker icon displayed at last cycling location (Requirement 6.3)
- Message displayed if no cycling session exists (Requirement 6.5)
- Distance from current location to bike
- Directions option (opens Google Maps app for navigation)

**StatisticsScreen**
- Time interval selector: daily, weekly, monthly (Requirement 7.9)
- Distance in kilometers for walking activities (Requirement 7.5)
- Distance in kilometers for cycling activities (Requirement 7.6)
- Distance in kilometers for running activities (Requirement 7.7)
- Total step count for walking and running (Requirement 7.8)
- Charts/graphs for visualization
- Activity type distribution

**PermissionsScreen**
- Initial permission request flow on first launch (Requirements 5.1, 5.2)
- Request location permission with "Allow all the time" option (Requirement 5.1)
- Request activity recognition permission (Requirement 5.2)
- Explanation messages when permissions denied (Requirements 5.3, 5.4)
- Links to app settings if permissions denied
- Begin monitoring when permissions granted (Requirement 5.5)

#### ViewModels

```kotlin
class HomeViewModel(
    private val startActivityTrackingUseCase: StartActivityTrackingUseCase,
    private val stopActivityTrackingUseCase: StopActivityTrackingUseCase
) : ViewModel()

class ActivityListViewModel(
    private val activityRepository: ActivityRepository
) : ViewModel()

class ActivityDetailViewModel(
    private val activityRepository: ActivityRepository,
    private val locationRepository: LocationRepository,
    private val calculateRouteDistanceUseCase: CalculateRouteDistanceUseCase
) : ViewModel()

class BikeLocationViewModel(
    private val getBikeLocationUseCase: GetBikeLocationUseCase
) : ViewModel()

class StatisticsViewModel(
    private val getActivityStatisticsUseCase: GetActivityStatisticsUseCase
) : ViewModel()
```

## Data Models

### Location Accuracy Filtering

Implements Requirements 2.3 and 2.4 for location accuracy handling.

**Filtering Logic:**
- Location points with accuracy < 50 meters: Included in route and used for distance calculations
- Location points with accuracy >= 50 meters: Stored in database but excluded from route rendering and distance calculations

**Design Rationale:** The 50-meter threshold balances between route accuracy and data availability. Poor GPS signals (accuracy >= 50m) often occur indoors or in urban canyons, and including these points would create unrealistic route deviations. However, storing all points allows for future analysis or manual correction if needed. The service continues monitoring even when accuracy is poor, ensuring it captures good data when GPS signal improves.

### Distance Calculation

Distance between consecutive location points calculated using Haversine formula:

```kotlin
fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371000.0 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}
```

### Step Count Estimation

Since Android Activity Recognition API doesn't provide step count directly, estimate based on:
- Walking/Running distance and average stride length
- Average stride: 0.762 meters (2.5 feet)
- Steps = Distance / Stride Length

```kotlin
fun estimateSteps(distanceMeters: Double, activityType: ActivityType): Int {
    return when (activityType) {
        ActivityType.WALKING -> (distanceMeters / 0.762).toInt()
        ActivityType.RUNNING -> (distanceMeters / 0.9).toInt() // Longer stride when running
        else -> 0
    }
}
```

### Activity Session State Machine

This state machine implements Requirements 1.2, 1.3, and 1.4 for automatic activity detection and session management.

```
┌─────────┐
│  IDLE   │
└────┬────┘
     │ Activity detected (confidence > 75%)
     │ → Create new session (Req 1.2)
     ▼
┌─────────────┐
│  TRACKING   │◄──┐
└────┬────────┘   │
     │             │ Same activity continues
     │             │ (confidence > 75%)
     ├─────────────┘
     │
     │ Trigger: Inactivity > 5 min (Req 1.4)
     │      OR Activity type changed (Req 1.3)
     │ → End current session
     │ → If activity changed: Create new session
     ▼
┌─────────┐
│  ENDED  │
└─────────┘
```

**Design Rationale:** The state machine ensures clean session boundaries by ending the current session before starting a new one when activity type changes. This prevents overlapping sessions and maintains data integrity. The 5-minute inactivity timeout balances between capturing complete activities and avoiding unnecessarily long sessions during breaks.

## Error Handling

### Permission Errors

**Location Permission Denied:**
- Display explanation dialog
- Provide button to open app settings
- Disable tracking features until granted

**Activity Recognition Permission Denied:**
- Display explanation dialog
- Provide button to open app settings
- Disable automatic activity detection

**Background Location Permission Denied:**
- Show rationale for "Allow all the time" permission
- Explain limitations of "While using the app" option
- Continue with limited functionality

### Service Errors

**Activity Recognition API Unavailable:**
- Check Google Play Services availability
- Prompt user to update Google Play Services
- Fallback: Manual activity selection

**Location Services Disabled:**
- Detect when location services are off
- Show dialog prompting user to enable location
- Provide direct link to location settings

**GPS Signal Lost:**
- Continue tracking with last known location
- Mark session with "GPS signal lost" indicator
- Resume when signal restored

### Data Errors

**Database Write Failure:**
- Log error for debugging
- Retry write operation up to 3 times
- Show user notification if persistent failure

**Corrupted Session Data:**
- Validate data integrity on read
- Skip corrupted sessions in list view
- Provide option to delete corrupted data

## Testing Strategy

### Unit Tests

**Domain Layer:**
- Use case logic testing
- Distance calculation accuracy
- Step count estimation
- Statistics aggregation
- Time interval filtering

**Data Layer:**
- Repository implementations
- Database queries
- Data mapping between entities and domain models

**ViewModels:**
- State management
- User interaction handling
- Flow transformations

### Integration Tests

**Database Tests:**
- Room database operations
- Foreign key constraints
- Cascade deletions
- Query performance

**Service Tests:**
- Activity recognition callback handling
- Location update processing
- Service lifecycle management

### UI Tests

**Compose UI Tests:**
- Screen navigation
- List rendering
- Map interaction
- Permission flow
- Statistics display

### Manual Testing Scenarios

1. **Activity Detection:**
   - Walk for 5 minutes, verify session created
   - Switch to cycling, verify new session
   - Stop moving for 5 minutes, verify session ends

2. **Location Tracking:**
   - Track route while cycling
   - Verify accuracy filtering
   - Check route visualization on map

3. **Bike Location:**
   - Complete cycling session
   - Navigate to bike location screen
   - Verify marker shows last position

4. **Statistics:**
   - Create multiple activities over several days
   - Verify daily/weekly/monthly aggregations
   - Check distance and step calculations

5. **Offline Functionality:**
   - Enable airplane mode
   - Verify activity tracking continues
   - Check map tiles cached for offline viewing

6. **Battery Impact:**
   - Monitor battery usage over 24 hours
   - Verify foreground service notification
   - Check location update frequency

### Performance Considerations

**Battery Optimization:**
- Use batched location updates when possible
- Reduce update frequency when stationary
- Stop location updates when no activity detected
- Use efficient database queries with proper indexing

**Memory Management:**
- Limit location points loaded in memory
- Use paging for activity list
- Release map resources when not visible
- Clear old cached map tiles

**Database Performance:**
- Index on sessionId for location points
- Index on startTime for time-based queries
- Use transactions for bulk inserts
- Implement database migration strategy

## Google Maps Integration

### Google Maps SDK Configuration

**API Key Setup:**
- Obtain API key from Google Cloud Console
- Enable Maps SDK for Android
- Add API key to AndroidManifest.xml
- Configure billing account (free tier: $200/month credit, ~28,000 map loads)

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="${MAPS_API_KEY}"/>
```

**Gradle Dependencies:**
```kotlin
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.maps.android:maps-compose:4.3.0")
```

### Map Features

**Route Rendering:**
- Use Polyline for route paths
- Color-code by activity type (Requirement 3.5):
  - Cycling: Blue (#2196F3)
  - Running: Green (#4CAF50)
  - Walking: Orange (#FF9800)
  - Vehicle: Red (#F44336)
- Line width: 8dp
- Add start marker (green) and end marker (red)

**Bike Location Marker:**
- Custom bike icon marker
- Info window showing last update time
- Tap to show distance from current location
- Option to open in Google Maps for navigation

**Map Controls:**
- Zoom controls (enabled)
- Compass (enabled)
- My Location button (enabled)
- Map toolbar (enabled for marker interactions)
- Map type selector (normal, satellite, terrain, hybrid)

**Map Styling:**
- Use default Google Maps style
- Enable indoor maps where available
- Show traffic layer option
- Enable 3D buildings

### Compose Integration

Use `GoogleMap` composable from `maps-compose` library:

```kotlin
@Composable
fun RouteMapView(
    route: List<LocationPoint>,
    activityType: ActivityType
) {
    val cameraPositionState = rememberCameraPositionState {
        // Center on route bounds (Requirement 3.4)
        position = CameraPosition.fromLatLngBounds(
            route.toBounds(), 
            100 // padding
        )
    }
    
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
            compassEnabled = true
        )
    ) {
        // Polyline for route (Requirement 3.3)
        Polyline(
            points = route.map { LatLng(it.latitude, it.longitude) },
            color = getColorForActivity(activityType),
            width = 8f
        )
        
        // Start marker
        Marker(
            state = MarkerState(position = route.first().toLatLng()),
            title = "Start",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
        )
        
        // End marker
        Marker(
            state = MarkerState(position = route.last().toLatLng()),
            title = "End",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        )
    }
}
```

### Map Tile Caching

**Automatic Caching:**
- Google Maps SDK automatically caches tiles
- Cache managed by Google Play Services
- No manual configuration required
- Tiles available offline after initial load

**Limitations:**
- Cache size managed by system
- No guaranteed offline availability
- Requires initial internet connection to load tiles

**Design Rationale:** While Google Maps doesn't provide the same level of offline control as OSMDroid, the automatic caching and superior performance make it a good choice for personal use. The free tier limits are generous enough for individual usage (~28,000 map loads per month).
