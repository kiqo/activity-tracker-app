# Requirements Document

## Introduction

The Activity Tracker App is a native Android application that automatically detects and tracks user activities including cycling, running, walking, and local commuting. The system uses Google's Activity Recognition API to identify activity types and Location Services to record GPS coordinates during activities. All data is stored locally on the device, and tracked routes are visualized on OpenStreetMap. The application operates entirely offline without requiring any backend services.

## Glossary

- **Activity Tracker App**: The native Android application system being developed
- **Activity Recognition API**: Google Play Services API that detects user physical activities
- **Location Services**: Android system services that provide GPS and network-based location data
- **Activity Session**: A continuous period of a single detected activity type with associated location data
- **Route**: A collection of GPS coordinates representing the path taken during an Activity Session
- **Local Storage**: Device-based data persistence using Android Room database
- **OpenStreetMap**: Open-source map rendering service used for displaying routes

## Requirements

### Requirement 1

**User Story:** As a user, I want the app to automatically detect when I start cycling, running, walking, or commuting, so that I don't have to manually start tracking each activity.

#### Acceptance Criteria

1. WHEN the Activity Tracker App is running in the background, THE Activity Tracker App SHALL continuously monitor for activity changes using the Activity Recognition API
2. WHEN the Activity Recognition API detects a confidence level above 75 percent for cycling, running, walking, or vehicle activity, THE Activity Tracker App SHALL create a new automatically-detected Activity Session
3. WHEN an automatically-detected Activity Session is active and the detected activity type changes with confidence above 75 percent, THE Activity Tracker App SHALL end the current Activity Session and create a new Activity Session for the new activity type
4. WHEN an Activity Session has been inactive for more than 5 minutes, THE Activity Tracker App SHALL automatically end the Activity Session
5. THE Activity Tracker App SHALL allow at most one automatically-detected Activity Session to be active at any time
6. WHEN a new automatically-detected Activity Session is created, THE Activity Tracker App SHALL end any existing automatically-detected Activity Session before starting the new session
7. THE Activity Tracker App SHALL allow at most one manually-started Activity Session to be active at any time
8. WHEN a new manually-started Activity Session is created, THE Activity Tracker App SHALL end any existing manually-started Activity Session before starting the new session
9. THE Activity Tracker App SHALL allow both one manually-started Activity Session and one automatically-detected Activity Session to be active simultaneously

### Requirement 2

**User Story:** As a user, I want my location to be tracked during all activities (both manually started and automatically detected), so that I can see the routes I took on a map.

#### Acceptance Criteria

1. WHEN an Activity Session starts (whether manually started or automatically detected), THE Activity Tracker App SHALL request location updates from Location Services at intervals of 10 seconds or less
2. WHEN Location Services provides a location update during an active Activity Session, THE Activity Tracker App SHALL store the latitude, longitude, altitude, accuracy, and timestamp in Local Storage
3. WHEN location accuracy is below 50 meters, THE Activity Tracker App SHALL include the location point in the Route
4. WHEN location accuracy is 50 meters or greater, THE Activity Tracker App SHALL exclude the location point from the Route but continue monitoring
5. WHEN an Activity Session ends, THE Activity Tracker App SHALL stop requesting location updates from Location Services
6. THE Activity Tracker App SHALL track location for both manually started Activity Sessions and automatically detected Activity Sessions

### Requirement 3

**User Story:** As a user, I want to view my tracked activities on a map, so that I can see where I've been and review my routes.

#### Acceptance Criteria

1. THE Activity Tracker App SHALL display all Activity Sessions in a chronological list showing activity type, date, duration, and distance
2. WHEN a user selects an Activity Session from the list, THE Activity Tracker App SHALL display the associated Route on an OpenStreetMap view
3. WHEN displaying a Route on OpenStreetMap, THE Activity Tracker App SHALL render a continuous line connecting all location points in chronological order
4. WHEN displaying a Route on OpenStreetMap, THE Activity Tracker App SHALL center the map view to show the entire Route within the visible area
5. THE Activity Tracker App SHALL display activity type indicators using distinct colors for cycling, running, walking, and vehicle activities

### Requirement 4

**User Story:** As a user, I want all my activity data stored on my device, so that my privacy is protected and I can access my data without internet connectivity.

#### Acceptance Criteria

1. THE Activity Tracker App SHALL store all Activity Session data in Local Storage using Android Room database
2. THE Activity Tracker App SHALL store all location points associated with Activity Sessions in Local Storage
3. THE Activity Tracker App SHALL function without requiring internet connectivity for activity detection and location tracking
4. WHEN the device has no internet connectivity, THE Activity Tracker App SHALL cache OpenStreetMap tiles for offline viewing of previously loaded map areas
5. THE Activity Tracker App SHALL NOT transmit any activity or location data to external servers

### Requirement 5

**User Story:** As a user, I want to grant or deny location and activity permissions, so that I have control over what data the app can access.

#### Acceptance Criteria

1. WHEN the Activity Tracker App is launched for the first time, THE Activity Tracker App SHALL request location permission with "Allow all the time" option
2. WHEN the Activity Tracker App is launched for the first time, THE Activity Tracker App SHALL request activity recognition permission
3. IF location permission is denied, THEN THE Activity Tracker App SHALL display a message explaining that location access is required for tracking routes
4. IF activity recognition permission is denied, THEN THE Activity Tracker App SHALL display a message explaining that activity recognition is required for automatic activity detection
5. WHEN permissions are granted, THE Activity Tracker App SHALL begin monitoring for activities and tracking location

### Requirement 6

**User Story:** As a user, I want to see the position of my bike on an OpenStreetMap, so that I can quickly locate where I last parked it.

#### Acceptance Criteria

1. THE Activity Tracker App SHALL identify the most recent Activity Session with activity type of cycling
2. WHEN a cycling Activity Session exists, THE Activity Tracker App SHALL retrieve the last location point from that Activity Session
3. THE Activity Tracker App SHALL display a bike marker at the last cycling location on an OpenStreetMap view
4. WHEN the user opens the bike location view, THE Activity Tracker App SHALL center the map on the bike marker location
5. IF no cycling Activity Session exists, THEN THE Activity Tracker App SHALL display a message indicating no bike location is available

### Requirement 7

**User Story:** As a user, I want to see statistics about my activities, so that I can understand my movement patterns and activity levels.

#### Acceptance Criteria

1. THE Activity Tracker App SHALL calculate and display total distance for each Activity Session
2. THE Activity Tracker App SHALL calculate and display total duration for each Activity Session
3. THE Activity Tracker App SHALL calculate and display average speed for each Activity Session where distance is greater than 0 meters
4. THE Activity Tracker App SHALL calculate and display the number of steps for walking and running Activity Sessions
5. THE Activity Tracker App SHALL display aggregate statistics showing distance in kilometers for walking activities within a user-selected time interval
6. THE Activity Tracker App SHALL display aggregate statistics showing distance in kilometers for cycling activities within a user-selected time interval
7. THE Activity Tracker App SHALL display aggregate statistics showing distance in kilometers for running activities within a user-selected time interval
8. THE Activity Tracker App SHALL display aggregate statistics showing total step count for walking and running activities within a user-selected time interval
9. WHERE the user selects a time interval, THE Activity Tracker App SHALL support daily, weekly, and monthly interval options
