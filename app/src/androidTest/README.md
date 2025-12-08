# Instrumented Tests

This directory contains **instrumented tests** that run on a real Android device or emulator.

## What are Instrumented Tests?

Instrumented tests run on actual Android hardware and have access to:
- Real Android framework (Services, Activities, Context)
- Real device features (GPS, sensors, notifications)
- Real Google Play Services
- Actual database operations

## Prerequisites

### 1. Enable Developer Options on Your Device
1. Go to **Settings** → **About Phone**
2. Tap **Build Number** 7 times
3. Go back to **Settings** → **Developer Options**
4. Enable **USB Debugging**

### 2. Connect Your Device
```bash
# Check if device is connected
adb devices

# Should show:
# List of devices attached
# ABC123XYZ    device
```

### 3. Enable Location Services
- Go to **Settings** → **Location**
- Turn on **Location**
- Set to **High Accuracy** mode

### 4. Install Google Play Services
- Ensure your device has Google Play Services installed and updated

## Running Instrumented Tests

### Run All Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run Specific Test Class
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.activitytracker.app.services.LocationTrackingServiceInstrumentedTest
```

### Run Single Test Method
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.activitytracker.app.services.LocationTrackingServiceInstrumentedTest#serviceStartsSuccessfully
```

### From Android Studio
1. Open the test file
2. Click the green play button next to the test class or method
3. Select your connected device
4. Tests will run on your device

## Test Reports

After running tests, view the HTML report:
```bash
open app/build/reports/androidTests/connected/index.html
```

## Troubleshooting

### Device Not Found
```bash
# Check USB connection
adb devices

# Restart adb server
adb kill-server
adb start-server
```

### Permission Denied
- Ensure USB debugging is enabled
- Check if device shows authorization dialog
- Try revoking and re-granting USB debugging authorization

### Tests Fail Due to Location
- Enable location services on device
- Ensure GPS has signal (go outside or near window)
- Grant location permissions when prompted

### Google Play Services Error
- Update Google Play Services on device
- Ensure device has Play Services (not all devices do)

### Tests Take Too Long
- Instrumented tests are slower than unit tests (this is normal)
- Each test can take 5-30 seconds
- Full suite might take several minutes

### Build Errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug
./gradlew assembleDebugAndroidTest
```

## Best Practices

### Keep Device Awake
- Go to **Developer Options**
- Enable **Stay Awake** (screen stays on while charging)

### Disable Animations (Faster Tests)
- Go to **Developer Options**
- Set all animation scales to **0.5x** or **Animation off**:
  - Window animation scale
  - Transition animation scale
  - Animator duration scale

### Use Test Device
- Use a dedicated test device if possible
- Don't use your primary phone (tests might interfere with normal usage)

### Mock Location (Optional)
For more reliable tests, you can enable mock locations:
1. **Developer Options** → **Select mock location app**
2. Choose your app
3. Tests can then inject fake GPS coordinates

## Continuous Integration

For CI/CD pipelines, use Firebase Test Lab or similar:

```bash
# Example: Firebase Test Lab
gcloud firebase test android run \
  --type instrumentation \
  --app app/build/outputs/apk/debug/app-debug.apk \
  --test app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
  --device model=Pixel2,version=28,locale=en,orientation=portrait
```

## Writing New Instrumented Tests

### Test Structure
```kotlin
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class MyInstrumentedTest {
    
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)
    
    @get:Rule(order = 1)
    val permissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    @Before
    fun setup() {
        hiltRule.inject()
    }
    
    @Test
    fun myTest() {
        // Test code
    }
}
```

### Key Annotations
- `@HiltAndroidTest` - For Hilt dependency injection
- `@RunWith(AndroidJUnit4::class)` - Android test runner
- `@LargeTest` - Marks as slow/integration test
- `@Before` / `@After` - Setup and teardown

### Useful APIs
```kotlin
// Get application context
val context = ApplicationProvider.getApplicationContext<Context>()

// Start service
context.startForegroundService(intent)

// Wait for async operations
runBlocking { delay(1000) }

// Check if service is running
isServiceRunning(context, MyService::class.java)
```

## Resources

- [Android Testing Documentation](https://developer.android.com/training/testing)
- [Instrumented Tests Guide](https://developer.android.com/training/testing/instrumented-tests)
- [Hilt Testing](https://developer.android.com/training/dependency-injection/hilt-testing)
- [Test Lab](https://firebase.google.com/docs/test-lab)
