# Activity Tracker App

A native Android application that automatically detects and tracks user activities including cycling, running, walking, and local commuting. The app uses Google's Activity Recognition API to identify activity types and Location Services to record GPS coordinates during activities.

## Features

- **Automatic Activity Detection**: Automatically detects when you start cycling, running, walking, or commuting
- **GPS Route Tracking**: Records your location during activities with 10-second intervals
- **Route Visualization**: View your tracked routes on Google Maps with color-coded activity types
- **Activity Statistics**: See detailed statistics including distance, duration, speed, and step count
- **Bike Location Finder**: Quickly locate where you last parked your bike
- **Privacy-First**: All data stored locally on your device with no backend services
- **Offline Support**: Full functionality without internet connectivity (except map tile downloads)

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room
- **Coroutines**: Kotlin Coroutines + Flow
- **Activity Recognition**: Google Play Services Activity Recognition API
- **Location**: Google Play Services Location API (FusedLocationProviderClient)
- **Maps**: Google Maps SDK for Android

## Requirements

- Android 10 (API 29) or higher
- Google Play Services installed
- Location permission (Allow all the time)
- Activity recognition permission
- Internet connection for map tile downloads (optional for offline use)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ActivityTracker
```

### 2. Configure Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **Maps SDK for Android**
4. Create an API key with Android restrictions
5. Copy `local.properties.example` to `local.properties`
6. Add your API key to `local.properties`:

```properties
sdk.dir=/path/to/Android/sdk
MAPS_API_KEY=your_google_maps_api_key_here
```

**Note**: `local.properties` is gitignored to keep your API key secure.

### 3. Build and Run

Open the project in Android Studio and:

1. Sync Gradle files
2. Build the project
3. Run on an emulator or physical device

Or use the command line:

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/activitytracker/app/
│   │   ├── data/              # Data layer (Room, repositories)
│   │   ├── domain/            # Domain layer (use cases, models)
│   │   ├── presentation/      # UI layer (Compose screens, ViewModels)
│   │   ├── services/          # Background services
│   │   ├── di/                # Dependency injection modules
│   │   ├── ui/theme/          # Compose theme
│   │   ├── MainActivity.kt
│   │   └── ActivityTrackerApplication.kt
│   ├── res/                   # Resources (layouts, strings, etc.)
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Permissions

The app requires the following permissions:

- `ACCESS_FINE_LOCATION`: For precise GPS tracking
- `ACCESS_COARSE_LOCATION`: For approximate location
- `ACCESS_BACKGROUND_LOCATION`: For tracking when app is in background
- `ACTIVITY_RECOGNITION`: For automatic activity detection
- `FOREGROUND_SERVICE`: For running tracking services
- `FOREGROUND_SERVICE_LOCATION`: For location-based foreground services
- `POST_NOTIFICATIONS`: For displaying tracking notifications (Android 13+)
- `INTERNET`: For downloading map tiles
- `ACCESS_NETWORK_STATE`: For checking network connectivity

## Architecture

The app follows Clean Architecture principles with three main layers:

### Data Layer
- Room database for local storage
- Repository implementations
- Data entities and DAOs

### Domain Layer
- Use cases for business logic
- Domain models
- Repository interfaces

### Presentation Layer
- Jetpack Compose UI
- ViewModels with StateFlow
- Navigation

## Key Components

### Background Services

- **ActivityRecognitionService**: Monitors activity changes and manages sessions
- **LocationTrackingService**: Tracks GPS location during active sessions

### Screens

- **HomeScreen**: Dashboard with current tracking status and quick stats
- **ActivityListScreen**: Chronological list of all tracked activities
- **ActivityDetailScreen**: Detailed view with route visualization on map
- **BikeLocationScreen**: Shows where you last parked your bike
- **StatisticsScreen**: Aggregate statistics by time interval
- **PermissionsScreen**: Initial permission request flow

## Development

### Dependencies

All dependencies are managed in `app/build.gradle.kts`. Key dependencies include:

- Jetpack Compose BOM 2023.10.01
- Hilt 2.48.1
- Room 2.6.1
- Google Play Services Location 21.0.1
- Google Maps Compose 4.3.3
- Kotlin Coroutines 1.7.3

## Building and Installation

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
# If this fails: Plugin your phone and allow USB Debugging
# debug with: adb devices

# Build & install to connected device
./gradlew installDebug

# Altearnatively: Build and install via adb
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Errors and warnings only
adb logcat *:E *:W
```

## Debug

View App logs:
```
# all app logs
adb logcat | grep "com.activitytracker.app"

# Last 500 lines (not live)
adb logcat -d | tail -500

# Only your app's errors
adb logcat -d | grep "com.activitytracker.app" | grep "E/"
```

## Testing

The project includes:

- Unit tests for domain layer logic
- Room database tests
- ViewModel tests with coroutines
- Compose UI tests

Run tests with:

```bash
./gradlew test [--continue]
./gradlew connectedAndroidTest
```

## Privacy & Data

- All activity and location data is stored locally on your device
- No data is transmitted to external servers
- The app only requires internet for downloading map tiles
- You can delete all data by clearing the app's data in Android settings
