# Implementation Plan

- [x] 1. Set up Android project structure and dependencies
  - Create new Android project with Kotlin and Jetpack Compose
  - Configure Hilt for dependency injection
  - Add Google Play Services dependencies (Activity Recognition, Location, Maps)
  - Add Room database dependencies
  - Add maps-compose library for Google Maps integration
  - Configure AndroidManifest.xml with required permissions and services
  - _Requirements: All requirements depend on proper project setup_

- [x] 2. Implement data layer with Room database
  - [x] 2.1 Create database entities and DAOs
    - Define ActivitySessionEntity with all fields (id, activityType, startTime, endTime, totalDistance, averageSpeed, stepCount)
    - Define LocationPointEntity with foreign key relationship to ActivitySessionEntity
    - Create ActivitySessionDao with queries for CRUD operations and time-based filtering
    - Create LocationPointDao with queries for session-based location retrieval
    - Create AppDatabase class with Room configuration
    - _Requirements: 4.1, 4.2_

  - [x] 2.2 Implement repository interfaces and implementations
    - Create ActivityRepository interface with Flow-based methods
    - Create LocationRepository interface with Flow-based methods
    - Implement ActivityRepositoryImpl with Room DAO integration
    - Implement LocationRepositoryImpl with Room DAO integration
    - Add database indexing for performance optimization
    - _Requirements: 4.1, 4.2_

- [x] 3. Implement domain layer with use cases and models
  - [x] 3.1 Create domain models
    - Define ActivitySession data class
    - Define ActivityType enum (CYCLING, RUNNING, WALKING, IN_VEHICLE)
    - Define LocationPoint data class
    - Define ActivityStatistics data class
    - Define TimeInterval enum (DAILY, WEEKLY, MONTHLY)
    - Create mapper functions between entities and domain models
    - _Requirements: All requirements use these domain models_

  - [x] 3.2 Implement core use cases
    - Create StartActivityTrackingUseCase for initiating activity sessions
    - Create StopActivityTrackingUseCase for ending sessions and calculating final statistics
    - Create GetBikeLocationUseCase to retrieve last cycling location
    - Create CalculateRouteDistanceUseCase using Haversine formula
    - Create EstimateStepCountUseCase based on distance and stride length
    - Create GetActivityStatisticsUseCase for time-based aggregations
    - _Requirements: 1.2, 1.3, 1.4, 2.5, 6.1, 6.2, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8_

- [x] 4. Implement background services for activity tracking
  - [x] 4.1 Create ActivityRecognitionService
    - Implement foreground service with persistent notification
    - Register with Activity Recognition API for 30-second updates
    - Filter activities by confidence threshold (>75%)
    - Create new ActivitySession when activity detected
    - End current session and create new one when activity type changes
    - Implement 5-minute inactivity timeout logic
    - Start/stop LocationTrackingService based on session state
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [x] 4.2 Create LocationTrackingService
    - Implement foreground service with persistent notification
    - Request location updates every 10 seconds using FusedLocationProviderClient
    - Store all location data (latitude, longitude, altitude, accuracy, timestamp) in database
    - Implement accuracy filtering logic (include <50m, exclude >=50m from route)
    - Stop location updates when session ends
    - Handle GPS signal loss gracefully
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 5. Implement permission handling
  - [x] 5.1 Create PermissionsScreen with Compose
    - Build UI for initial permission request flow
    - Request location permission with "Allow all the time" option
    - Request activity recognition permission
    - Display explanation messages when permissions denied
    - Provide buttons to open app settings
    - Trigger service start when permissions granted
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [x] 5.2 Implement permission state management
    - Create PermissionsViewModel to handle permission states
    - Check permission status on app launch
    - Handle permission result callbacks
    - Navigate to appropriate screen based on permission status
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 6. Implement HomeScreen and navigation
  - [x] 6.1 Create HomeScreen UI
    - Display current tracking status (active/inactive)
    - Show today's activity summary (distance, duration by type)
    - Add navigation buttons to other screens
    - Implement manual start/stop tracking button
    - Display foreground service status
    - _Requirements: General app navigation_

  - [x] 6.2 Set up Compose navigation
    - Define navigation routes for all screens
    - Implement NavHost with screen destinations
    - Create navigation helper functions
    - Handle deep linking if needed
    - _Requirements: General app navigation_

  - [x] 6.3 Create HomeViewModel
    - Fetch today's activity statistics
    - Manage tracking service state
    - Handle start/stop tracking actions
    - Expose UI state as StateFlow
    - _Requirements: General app navigation_

- [ ] 7. Implement ActivityListScreen
  - [ ] 7.1 Create ActivityListScreen UI
    - Display chronological list of all activity sessions
    - Show activity type icon, date, duration, and distance for each item
    - Implement filter by activity type
    - Add click handler to navigate to detail screen
    - Implement pull-to-refresh
    - _Requirements: 3.1_

  - [ ] 7.2 Create ActivityListViewModel
    - Fetch all activity sessions from repository
    - Implement filtering logic by activity type
    - Sort sessions chronologically (newest first)
    - Expose filtered list as StateFlow
    - _Requirements: 3.1_

- [ ] 8. Implement ActivityDetailScreen with Google Maps
  - [ ] 8.1 Create ActivityDetailScreen UI
    - Display full activity session details (type, date, duration, distance, speed, steps)
    - Integrate GoogleMap composable for route visualization
    - Implement delete activity button with confirmation dialog
    - Show loading state while fetching location points
    - _Requirements: 3.2, 7.1, 7.2, 7.3, 7.4_

  - [ ] 8.2 Implement route rendering on Google Maps
    - Fetch location points for selected session
    - Filter points by accuracy (<50m) for route display
    - Render Polyline connecting points chronologically
    - Color-code polyline by activity type (blue=cycling, green=running, orange=walking, red=vehicle)
    - Add start marker (green) and end marker (red)
    - Center camera to show entire route with padding
    - _Requirements: 3.2, 3.3, 3.4, 3.5_

  - [ ] 8.3 Create ActivityDetailViewModel
    - Fetch activity session by ID
    - Fetch location points for session
    - Calculate route bounds for camera positioning
    - Handle delete activity action
    - Expose session details and route as StateFlow
    - _Requirements: 3.2, 3.3, 3.4, 3.5, 7.1, 7.2, 7.3, 7.4_

- [ ] 9. Implement BikeLocationScreen
  - [ ] 9.1 Create BikeLocationScreen UI
    - Integrate GoogleMap composable centered on bike location
    - Display custom bike marker icon at last cycling position
    - Show distance from current location to bike
    - Add button to open Google Maps for navigation
    - Display message if no cycling session exists
    - _Requirements: 6.3, 6.4, 6.5_

  - [ ] 9.2 Create BikeLocationViewModel
    - Fetch most recent cycling session
    - Retrieve last location point from that session
    - Calculate distance from current location to bike
    - Handle case when no cycling session exists
    - Expose bike location and status as StateFlow
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 10. Implement StatisticsScreen
  - [ ] 10.1 Create StatisticsScreen UI
    - Display time interval selector (daily, weekly, monthly)
    - Show distance in km for walking, cycling, and running
    - Display total step count
    - Add charts/graphs for visual representation
    - Show activity type distribution
    - Implement date range picker for custom intervals
    - _Requirements: 7.5, 7.6, 7.7, 7.8, 7.9_

  - [ ] 10.2 Create StatisticsViewModel
    - Fetch activity sessions for selected time interval
    - Calculate aggregate statistics (distance by type, total steps)
    - Convert distances from meters to kilometers
    - Sum step counts for walking and running activities
    - Handle time interval changes
    - Expose statistics as StateFlow
    - _Requirements: 7.5, 7.6, 7.7, 7.8, 7.9_

- [ ] 11. Configure Google Maps API
  - [ ] 11.1 Set up Google Cloud project and API key
    - Create Google Cloud project
    - Enable Maps SDK for Android
    - Generate API key with Android restrictions
    - Configure billing account (free tier)
    - Add API key to local.properties and AndroidManifest.xml
    - _Requirements: 3.2, 6.3_

  - [ ] 11.2 Implement map utility functions
    - Create helper function to convert LocationPoint to LatLng
    - Implement function to calculate LatLngBounds from route points
    - Create function to get color for activity type
    - Implement custom marker icon creation for bike
    - _Requirements: 3.2, 3.3, 3.4, 3.5, 6.3_

- [ ] 12. Implement error handling and edge cases
  - [ ] 12.1 Handle permission errors
    - Detect when permissions are denied
    - Show explanation dialogs with rationale
    - Provide links to app settings
    - Disable features when permissions unavailable
    - _Requirements: 5.3, 5.4_

  - [ ] 12.2 Handle service errors
    - Check Google Play Services availability
    - Detect when location services are disabled
    - Handle GPS signal loss during tracking
    - Implement retry logic for transient failures
    - Show user-friendly error messages
    - _Requirements: 1.1, 2.1_

  - [ ] 12.3 Handle data errors
    - Implement database write failure retry logic
    - Validate data integrity on read operations
    - Handle corrupted session data gracefully
    - Provide option to delete corrupted data
    - _Requirements: 4.1, 4.2_

- [ ] 13. Optimize performance and battery usage
  - [ ] 13.1 Implement battery optimizations
    - Use batched location updates when possible
    - Reduce update frequency when device is stationary
    - Stop location updates when no activity detected
    - Configure efficient wake locks for services
    - _Requirements: 2.1_

  - [ ] 13.2 Optimize database performance
    - Add indexes on sessionId and startTime columns
    - Use database transactions for bulk inserts
    - Implement paging for activity list
    - Limit location points loaded in memory
    - _Requirements: 4.1, 4.2_

  - [ ] 13.3 Optimize map rendering
    - Release map resources when screen not visible
    - Limit polyline points for very long routes
    - Use appropriate map tile quality settings
    - Implement map lifecycle management
    - _Requirements: 3.2, 3.3_

- [ ] 14. Write unit tests for core functionality
  - [x] 14.1 Test domain layer
    - Write tests for distance calculation (Haversine formula)
    - Test step count estimation logic
    - Test statistics aggregation calculations
    - Test time interval filtering
    - Verify use case business logic
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8_

  - [x] 14.2 Test data layer
    - Write Room database tests for CRUD operations
    - Test foreign key constraints and cascade deletes
    - Verify repository implementations
    - Test data mapping between entities and domain models
    - _Requirements: 4.1, 4.2_

  - [ ] 14.3 Test ViewModels
    - Test state management and UI state updates
    - Verify Flow transformations
    - Test user interaction handling
    - Mock repository dependencies
    - _Requirements: All UI-related requirements_
