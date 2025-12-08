# Activity Tracker App - Setup Guide

## Prerequisites

Before you begin, ensure you have the following installed:

1. **Android Studio** (latest stable version)
   - Download from: https://developer.android.com/studio

2. **JDK 17** (required for Gradle 8.2)
   - Android Studio includes a JDK, or install separately

3. **Android SDK** with the following components:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android Emulator (for testing without a physical device)

## Initial Setup Steps

### 1. Configure local.properties

Create a `local.properties` file in the project root (copy from `local.properties.example`):

```properties
sdk.dir=/path/to/Android/sdk
MAPS_API_KEY=your_google_maps_api_key_here
```

**Finding your Android SDK path:**
- **macOS/Linux**: Usually `~/Library/Android/sdk` or `~/Android/Sdk`
- **Windows**: Usually `C:\Users\YourUsername\AppData\Local\Android\Sdk`

### 2. Get Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable **Maps SDK for Android**:
   - Navigate to "APIs & Services" > "Library"
   - Search for "Maps SDK for Android"
   - Click "Enable"
4. Create credentials:
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "API Key"
   - Restrict the key to Android apps (recommended)
   - Add your app's package name: `com.activitytracker.app`
   - Add your SHA-1 fingerprint (get from Android Studio or keystore)
5. Copy the API key to `local.properties`

**Getting SHA-1 fingerprint for debug:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### 3. Open Project in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the project directory
4. Click "OK"
5. Wait for Gradle sync to complete

### 4. Sync Gradle

Android Studio should automatically sync Gradle. If not:
- Click "File" > "Sync Project with Gradle Files"
- Or click the elephant icon in the toolbar

### 5. Build the Project

```bash
./gradlew build
```

Or in Android Studio:
- Click "Build" > "Make Project"
- Or press Ctrl+F9 (Windows/Linux) or Cmd+F9 (macOS)

## Running the App

### On an Emulator

1. Create an AVD (Android Virtual Device):
   - Click "Tools" > "Device Manager"
   - Click "Create Device"
   - Select a device (e.g., Pixel 6)
   - Select system image (API 34 recommended)
   - Click "Finish"

2. Run the app:
   - Select the emulator from the device dropdown
   - Click the green "Run" button
   - Or press Shift+F10 (Windows/Linux) or Ctrl+R (macOS)

**Note**: For testing location features, you'll need to set mock locations in the emulator.

### On a Physical Device

1. Enable Developer Options on your device:
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings > Developer Options
   - Enable "USB Debugging"

2. Connect device via USB

3. Run the app:
   - Select your device from the device dropdown
   - Click the green "Run" button

## Verifying the Setup

After running the app, you should see:

1. The app launches successfully
2. A blank screen with Material 3 theme (HomeScreen will be implemented in later tasks)
3. No build errors in the Gradle console

## Troubleshooting

### Gradle Sync Failed

**Issue**: Gradle sync fails with dependency resolution errors

**Solution**:
- Check your internet connection
- Try "File" > "Invalidate Caches" > "Invalidate and Restart"
- Delete `.gradle` folder and sync again

### SDK Not Found

**Issue**: Error about Android SDK not found

**Solution**:
- Verify `sdk.dir` path in `local.properties`
- Ensure Android SDK is installed via Android Studio SDK Manager

### Maps API Key Issues

**Issue**: Map doesn't load or shows "Authorization failure"

**Solution**:
- Verify API key is correct in `local.properties`
- Ensure Maps SDK for Android is enabled in Google Cloud Console
- Check API key restrictions match your app's package name and SHA-1

### Build Tools Version

**Issue**: Build fails due to missing build tools

**Solution**:
- Open SDK Manager in Android Studio
- Install Android SDK Build-Tools 34.0.0
- Sync Gradle again

### KSP Errors

**Issue**: Errors related to Kotlin Symbol Processing (KSP)

**Solution**:
- Ensure KSP version matches Kotlin version
- Clean and rebuild: `./gradlew clean build`

## Next Steps

Once the project builds successfully:

1. Review the project structure in `README.md`
2. Proceed to implement Task 2: Data layer with Room database
3. Follow the implementation plan in `.kiro/specs/activity-tracker-app/tasks.md`

## Useful Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install debug APK on connected device
./gradlew installDebug

# Run unit tests
./gradlew test -s --info

# Run instrumented tests
./gradlew connectedAndroidTest

# Check for dependency updates
./gradlew dependencyUpdates
```

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Google Maps Platform](https://developers.google.com/maps/documentation/android-sdk)
