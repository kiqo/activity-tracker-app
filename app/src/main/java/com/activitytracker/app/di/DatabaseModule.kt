package com.activitytracker.app.di

import android.content.Context
import androidx.room.Room
import com.activitytracker.app.data.local.AppDatabase
import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.local.dao.LocationPointDao
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigration() // For development; use proper migrations in production
            .build()
    }
    
    /**
     * Migration from version 1 to 2: Add indexes for performance optimization.
     */
    private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
            // Add index on startTime for activity_sessions table
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_activity_sessions_startTime ON activity_sessions(startTime)"
            )
            // Add index on activityType for activity_sessions table
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_activity_sessions_activityType ON activity_sessions(activityType)"
            )
        }
    }
    
    /**
     * Migration from version 2 to 3: Add isManuallyStarted field.
     */
    private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
            // Add isManuallyStarted column with default value false (auto-detected)
            database.execSQL(
                "ALTER TABLE activity_sessions ADD COLUMN isManuallyStarted INTEGER NOT NULL DEFAULT 0"
            )
        }
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
}
