package com.activitytracker.app.di

import android.content.Context
import androidx.room.Room
import com.activitytracker.app.data.local.AppDatabase
import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.local.dao.LocationPointDao
import com.activitytracker.app.data.local.dao.SessionLocationPointDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Always recreate database from scratch on schema changes
            .build()
    }
    
    @Provides
    @Singleton
    fun provideActivitySessionDao(database: AppDatabase): ActivitySessionDao {
        return database.activitySessionDao()
    }
    
    @Provides
    @Singleton
    fun provideLocationPointDao(database: AppDatabase): LocationPointDao {
        return database.locationPointDao()
    }
    
    @Provides
    @Singleton
    fun provideSessionLocationPointDao(database: AppDatabase): SessionLocationPointDao {
        return database.sessionLocationPointDao()
    }
}
