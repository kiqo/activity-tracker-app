package com.activitytracker.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.activitytracker.app.data.local.dao.ActivitySessionDao
import com.activitytracker.app.data.local.dao.LocationPointDao
import com.activitytracker.app.data.local.dao.SessionLocationPointDao
import com.activitytracker.app.data.local.entity.ActivitySessionEntity
import com.activitytracker.app.data.local.entity.LocationPointEntity
import com.activitytracker.app.data.local.entity.SessionLocationPointEntity

/**
 * Room database for Activity Tracker App.
 * Stores activity sessions and location points locally on device.
 * 
 * Database version 3:
 * - Removed sessionId foreign key from LocationPointEntity
 * - Added SessionLocationPointEntity junction table for many-to-many relationship
 * - Enables shared location tracking: one location point can be linked to multiple sessions
 * - Migration from v2 to v3 converts existing data to junction table format
 * 
 * Database version 2:
 * - Added indexes on startTime and activityType for performance
 * - Added isManuallyStarted field to ActivitySessionEntity for dual-track session management
 * - Added partial unique indexes to enforce at most 1 active manual and 1 active automatic session (Req 1.5, 1.7)
 * 
 * Database version 1:
 * - ActivitySessionEntity: Stores activity session metadata
 * - LocationPointEntity: Stores GPS location points with foreign key to sessions
 */
@Database(
    entities = [
        ActivitySessionEntity::class,
        LocationPointEntity::class,
        SessionLocationPointEntity::class
    ],
    version = 3,
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
    
    /**
     * Provides access to session-location point junction table operations.
     */
    abstract fun sessionLocationPointDao(): SessionLocationPointDao
    
    companion object {
        const val DATABASE_NAME = "activity_tracker_db"
    }
}
