---
title: Android Development Standards
inclusion: always
---

# Android Development Standards

## Dependency Management
- Use latest stable versions of Android and Kotlin libraries
- Leverage Gradle version catalogs (`libs.versions.toml`) for centralized dependency management
- Verify compatibility using Context7 MCP server before adding dependencies
- Justify each new dependency with clear business or technical value
- Prefer AndroidX libraries and Jetpack components
- Favor Kotlin-first libraries over Java alternatives
- Document version constraints and minimum SDK requirements
- Remove unused dependencies regularly through Gradle dependency analysis
- Use Gradle's dependency locking for reproducible builds
- Keep Gradle wrapper updated to latest stable version

## Architecture Standards (MVVM Pattern)
- **Model Layer**: Domain models, data sources, repositories
  - Keep models immutable using Kotlin data classes
  - Implement repository pattern for data access abstraction
  - Use Room for local database with proper TypeConverters
  - Implement data source interfaces for remote APIs (Retrofit/Ktor)
- **ViewModel Layer**: Business logic and state management
  - Extend AndroidX ViewModel for lifecycle awareness
  - Use StateFlow or LiveData for reactive state management
  - Keep ViewModels platform-agnostic (no Android framework dependencies except AndroidX)
  - Implement proper coroutine scoping with viewModelScope
  - Handle configuration changes through ViewModel preservation
- **View Layer**: UI components (Activities, Fragments, Composables)
  - Use Jetpack Compose for modern declarative UI
  - Keep views thin - delegate logic to ViewModels
  - Observe state changes reactively
  - Handle UI events through sealed classes or lambda callbacks
- **Dependency Injection**: Use Hilt for compile-time DI
  - Define modules at appropriate scopes (@Singleton, @ViewModelScoped)
  - Inject repositories into ViewModels
  - Inject data sources into repositories

## Code Quality Standards
- Never create duplicate files with suffixes like `_fixed`, `_clean`, `_backup`, etc.
- Work iteratively on existing files (version control handles history)
- Follow Kotlin coding conventions and idiomatic practices
  - Use extension functions for utility methods
  - Prefer sealed classes for restricted hierarchies
  - Leverage scope functions (let, apply, run, also, with) appropriately
  - Use nullable types explicitly and handle nullability safely
- Use meaningful variable and function names following camelCase
- Keep functions small and focused on single responsibilities
- Implement proper error handling with Result/Either types or sealed class hierarchies
- Avoid `!!` null assertion operator - use safe calls or elvis operator
- Document public APIs with KDoc comments

## Logging Standards

### Framework Choice
- Use a logging abstraction interface that uses **Timber** library for structured logging (preferred over Android Log)
  - Automatic tagging with class name
  - Easy to disable logging in release builds
  - Extensible with custom log trees
  - Better performance than manual tag management
- Add dependency: `implementation("com.jakewharton.timber:timber:5.0.1")`

### Setup Timber
```kotlin
// In Application class
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsTree()) // Custom tree for production
        }
    }
}
```

### Log Levels - When to Use Each
- **Timber.v()** (VERBOSE): Detailed diagnostic information, disabled in release
- **Timber.d()** (DEBUG): Development debugging information
- **Timber.i()** (INFO): General informational messages (user actions, flow)
- **Timber.w()** (WARN): Warning messages for recoverable issues
- **Timber.e()** (ERROR): Error messages with exceptions
  - Always include throwable: `Timber.e(exception, "Error message")`

### Logging Guidelines
```kotlin
// ✅ GOOD - Clear, contextual logging
Timber.d("User logged in: userId=${user.id}")
Timber.i("Fetching user data from API")
Timber.w("Cache miss for user ${userId}, fetching from network")
Timber.e(exception, "Failed to parse response for endpoint: $endpoint")

// ❌ BAD - Vague or missing context
Timber.d("Success")
Timber.e("Error occurred")
Log.d("TAG", "Something happened") // Don't use Android Log directly
```

### What to Log
- **User actions**: Login, logout, navigation, button clicks (INFO)
- **Network operations**: API calls start/end, response codes (DEBUG/INFO)
- **Data operations**: Database queries, cache hits/misses (DEBUG)
- **State changes**: ViewModel state transitions (DEBUG)
- **Errors and exceptions**: All caught exceptions with context (ERROR)
- **Performance metrics**: Slow operations, loading times (WARN if slow)

### What NOT to Log
- **Sensitive data**: Passwords, tokens, PII, credit card info
- **Large payloads**: Full JSON responses (log summary instead)
- **In tight loops**: Can cause performance issues
- **Production verbose logs**: Remove verbose/debug logs from release builds

### Production Logging
```kotlin
// Custom Timber tree for production
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return // Don't log debug info in production
        }
        
        // Send to crash reporting
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("priority", priority)
            setCustomKey("tag", tag ?: "")
            setCustomKey("message", message)
            t?.let { recordException(it) }
        }
    }
}
```

### Structured Logging
```kotlin
// Use consistent tagging
class UserViewModel : ViewModel() {
    init {
        Timber.tag("UserViewModel")
    }
    
    fun fetchUser() {
        Timber.d("fetchUser: Starting user fetch for id=$userId")
        // ...
    }
}

// Add correlation IDs for tracking flows
fun loginUser(email: String) {
    val requestId = UUID.randomUUID().toString().substring(0, 8)
    Timber.d("Login flow started: requestId=$requestId, email=$email")
    
    // Pass requestId through the flow
    repository.login(email, requestId)
        .onSuccess {
            Timber.i("Login successful: requestId=$requestId")
        }
        .onFailure {
            Timber.e(it, "Login failed: requestId=$requestId")
        }
}
```

### Tracking Logs During Development
```bash
# Filter by your app package in terminal
adb logcat --pid=$(adb shell pidof -s com.yourcompany.yourapp)

# Filter by tag
adb logcat -s YourTag

# Filter by priority level (I and above)
adb logcat *:I

# Save to file
adb logcat -d > logcat.txt

# Clear logs
adb logcat -c
```

### Android Studio Logcat Filters
- Open Logcat window in Android Studio
- Use dropdown to select your app process
- Create custom filters:
  - Filter Name: "My App Errors"
  - Package Name: `com.yourcompany.yourapp`
  - Log Level: Error

### Production Monitoring
- Use Firebase Crashlytics for crash reports and non-fatal exceptions
- Consider Sentry, Datadog, or New Relic for comprehensive monitoring
- Implement custom log trees that send important logs to remote services
- Never log sensitive data at any level, even in production monitoring

## Testing Standards

### Unit Tests
- Write unit tests for ViewModels, repositories, and business logic
- Use JUnit 5 (Jupiter) for test framework
- Mock dependencies with MockK (Kotlin-friendly mocking)
- Test coroutines using `kotlinx-coroutines-test` and `runTest`
- Achieve minimum 80% code coverage for business logic
- Structure tests using Given-When-Then pattern
- Use `@Test` annotation and descriptive test names (backticks allowed)
- Test both success and error scenarios
- Verify StateFlow/LiveData emissions using `turbine` or similar

### Instrumented Tests (Android Tests)
- Write instrumented tests for database operations (Room DAOs)
- Test UI components using Jetpack Compose Testing or Espresso
- Use `@RunWith(AndroidJUnit4::class)` for Android context
- Leverage `Hilt` test utilities for dependency injection in tests
- Test navigation flows between screens
- Verify UI state changes and user interactions

### Test Organization
- Place unit tests in `src/test/kotlin`
- Place instrumented tests in `src/androidTest/kotlin`
- Mirror production package structure in test directories
- Use test fixtures and builders for complex test data
- Share common test utilities in dedicated test packages
- Use parameterized tests for testing multiple scenarios

### Test Naming Conventions
- Format: `methodName_scenario_expectedBehavior`
- Example: `fetchUser_whenNetworkError_emitsErrorState`
- Use backticks for natural language: `` `fetch user emits loading then success` ``

### Continuous Testing
- Run unit tests before committing: `./gradlew test`
- Get detailed test output with stack traces: `./gradlew test -s`
- Get full stack traces for debugging: `./gradlew test -S` or `./gradlew test --full-stacktrace`
- Run tests with detailed logging: `./gradlew test --info`
- Run specific test class: `./gradlew test --tests "com.company.app.ViewModelTest"`
- Run specific test method: `./gradlew test --tests "com.company.app.ViewModelTest.fetchUser_whenNetworkError_emitsErrorState"`
- Run instrumented tests regularly: `./gradlew connectedAndroidTest`
- Continue running tests after failures: `./gradlew test --continue`
- Integrate tests in CI/CD pipeline
- Monitor test execution time and optimize slow tests
- Use test coverage reports: `./gradlew jacocoTestReport`
- View test reports at: `app/build/reports/tests/testDebugUnitTest/index.html`

## File Management
- Follow standard Android project structure:
  - `app/src/main/kotlin` - Production code
  - `app/src/test/kotlin` - Unit tests
  - `app/src/androidTest/kotlin` - Instrumented tests
  - `app/src/main/res` - Resources (layouts, strings, drawables)
- Organize code by feature modules for large apps
- Use consistent package naming: `com.company.app.feature.presentation/domain/data`
- Keep resource files organized by type and purpose
- Use vector drawables (XML) over raster images when possible
- Store dimension values in `dimens.xml`
- Externalize strings to `strings.xml` for localization
- Avoid temporary or backup files in version control

## Build Configuration
- Use Gradle Kotlin DSL (`build.gradle.kts`) over Groovy
- Define build variants for different environments (debug, staging, production)
- Use `buildConfigField` for environment-specific values
- Implement ProGuard/R8 rules for release builds
- Configure signing configs securely (use Gradle properties, not hardcoded)
- Enable strict mode in debug builds for performance monitoring
- Use build features flags to disable unused features (viewBinding, dataBinding)

## Documentation Approach
- Maintain comprehensive README covering:
  - Project overview and architecture
  - Setup instructions and prerequisites
  - Build and run instructions
  - Testing procedures
  - Deployment process
- Document architecture decisions in ADR (Architecture Decision Records)
- Use KDoc for public APIs and complex functions
- Include inline comments for non-obvious business logic
- Document API endpoints and data models
- Keep dependency rationale documented in version catalog comments
- Update documentation when upgrading major dependencies

## Version Control Integration
- Commit frequently with meaningful conventional commit messages
  - `feat:` for new features
  - `fix:` for bug fixes
  - `test:` for test additions/changes
  - `refactor:` for code refactoring
- Use feature branches for development (`feature/user-authentication`)
- Keep `main` or `develop` branch deployable at all times
- Tag releases following semantic versioning (v1.2.3)
- Use comprehensive `.gitignore` for Android projects:
  - Build outputs (`/build`, `*.apk`, `*.aab`)
  - IDE files (`.idea/`, `*.iml`)
  - Local configuration (`local.properties`)
  - Generated files (`/captures`, `/generated`)
- Never commit secrets, API keys, or signing keystores

## Quality Assurance
- Run lint checks: `./gradlew lint`
- Use detekt for static code analysis
- Configure ktlint for code formatting
- Perform code reviews for all pull requests
- Monitor code coverage and maintain minimum 80% for business logic
- Use Android Studio's built-in inspections
- Profile app performance regularly with Android Profiler
- Test on multiple device configurations and API levels
- Monitor memory leaks with LeakCanary in debug builds
- Implement crash reporting (Firebase Crashlytics)

## Performance Best Practices
- Use `LaunchedEffect` and `rememberCoroutineScope` properly in Compose
- Implement proper image loading with Coil or Glide
- Use pagination for large data sets (Paging 3 library)
- Optimize RecyclerView with DiffUtil for list updates
- Avoid memory leaks by clearing references in lifecycle methods
- Use WorkManager for background tasks
- Implement proper caching strategies
- Monitor network usage and implement offline-first architecture

## Security Standards
- Never hardcode API keys or secrets
- Use encrypted SharedPreferences for sensitive data
- Implement certificate pinning for API calls
- Use AndroidX Security library for encrypted files
- Validate all user inputs
- Use SafeArgs for type-safe navigation arguments
- Keep ProGuard/R8 configuration up-to-date
- Implement proper authentication and authorization
- Follow OWASP Mobile Security guidelines