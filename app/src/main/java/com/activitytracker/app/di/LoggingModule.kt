package com.activitytracker.app.di

import com.activitytracker.app.util.Logger
import com.activitytracker.app.util.TimberLogger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing logging dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {
    
    @Binds
    @Singleton
    abstract fun bindLogger(timberLogger: TimberLogger): Logger
}
