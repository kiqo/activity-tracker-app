package com.activitytracker.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for Activity Tracker App.
 */
object DatabaseMigrations {
    
    /**
     * Migration from version 2 to version 3.
     * 
     * Changes:
     * 1. Create new location_points table without sessionId foreign key
     * 2. Create session_location_points junction table
     * 3. Migrate existing location point data to new schema
     * 4. Drop old location_points table
     * 5. Rename new table to location_points
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Step 1: Create new location_points table without sessionId
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS location_points_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    latitude REAL NOT NULL,
                    longitude REAL NOT NULL,
                    altitude REAL,
                    accuracy REAL NOT NULL,
                    timestamp INTEGER NOT NULL
                )
            """.trimIndent())
            
            // Step 2: Create junction table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS session_location_points (
                    sessionId INTEGER NOT NULL,
                    locationPointId INTEGER NOT NULL,
                    PRIMARY KEY(sessionId, locationPointId),
                    FOREIGN KEY(sessionId) REFERENCES activity_sessions(id) ON DELETE CASCADE,
                    FOREIGN KEY(locationPointId) REFERENCES location_points(id) ON DELETE CASCADE
                )
            """.trimIndent())
            
            // Step 3: Create indexes on junction table
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_session_location_points_sessionId 
                ON session_location_points(sessionId)
            """.trimIndent())
            
            database.execSQL("""
                CREATE INDEX IF NOT EXISTS index_session_location_points_locationPointId 
                ON session_location_points(locationPointId)
            """.trimIndent())
            
            // Step 4: Migrate existing data
            // Copy location points to new table (without sessionId)
            database.execSQL("""
                INSERT INTO location_points_new (id, latitude, longitude, altitude, accuracy, timestamp)
                SELECT id, latitude, longitude, altitude, accuracy, timestamp
                FROM location_points
            """.trimIndent())
            
            // Create junction table entries for existing location points
            database.execSQL("""
                INSERT INTO session_location_points (sessionId, locationPointId)
                SELECT sessionId, id
                FROM location_points
            """.trimIndent())
            
            // Step 5: Drop old table and rename new table
            database.execSQL("DROP TABLE location_points")
            database.execSQL("ALTER TABLE location_points_new RENAME TO location_points")
        }
    }
}
