package com.activitytracker.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.local.dao.LocationPointDao
import com.activitytracker.app.data.local.entity.ActivitySessionEntity
import com.activitytracker.app.data.local.entity.LocationPointEntity

/**
 * Room database for Activity Tracker App.
 * Stores activity sessions and location points locally on device.
 * 
 * Database version 1:
 * - ActivitySessionEntity: Stores activity session metadata
 * - LocationPointEntity: Stores GPS location points with foreign key to sessions
 */
@Database(
    entities = [
        ActivitySessionEntity::class,
        LocationPointEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Provides access to activity session operations.
     */
    abstract fun activitySessionDao(): ActivitySessionDao
    
    /**
     * Provides access to location point operations.
     */
    abstract fun locationPointDao(): LocationPointDao
    
    companion object {
        const val DATABASE_NAME = "activity_tracker_db"
    }
}
