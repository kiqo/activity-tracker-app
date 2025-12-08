package com.activitytracker.app.di

import com.activitytracker.app.BuildConfig
import com.activitytracker.app.util.Logger
import com.activitytracker.app.util.TimberLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

/**
 * Hilt module for providing logging dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {
    
    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return provideLogger(BuildConfig.DEBUG)
    }
    
    internal fun provideLogger(isDebug: Boolean): Logger {
        // Initialize Timber when Logger is first created
        if (isDebug) {
            Timber.plant(Timber.DebugTree())
        }
        return TimberLogger()
    }
}
