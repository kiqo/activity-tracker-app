package com.activitytracker.app.di

import com.activitytracker.app.util.Logger
import com.activitytracker.app.util.TimberLogger
import org.junit.After
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for LoggingModule.
 * Tests Logger provider and Timber initialization.
 */
class LoggingModuleTest {

    private lateinit var loggingModule: LoggingModule

    @Before
    fun setup() {
        loggingModule = LoggingModule
        // Clear any existing Timber trees before each test
        Timber.uprootAll()
    }

    @After
    fun tearDown() {
        // Clean up Timber trees after each test
        Timber.uprootAll()
    }

    @Test
    fun `provideLogger returns TimberLogger instance`() {
        // When
        val logger = loggingModule.provideLogger()

        // Then
        assertNotNull(logger)
        assertTrue(logger is TimberLogger)
    }

    @Test
    fun `provideLogger returns Logger interface`() {
        // When
        val logger = loggingModule.provideLogger()

        // Then
        assertNotNull(logger)
        assertTrue(logger is Logger)
    }

    @Test
    fun `provideLogger plants tree in debug mode`() {
        // Given
        Timber.uprootAll()
        
        // When
        val logger = loggingModule.provideLogger(isDebug = true)
        
        // Then
        assertNotNull(logger)
        assertTrue(logger is TimberLogger)
        assertEquals(1, Timber.treeCount, 
            "Debug builds should plant exactly one Timber tree")
    }
    
    @Test
    fun `provideLogger does not plant tree in release mode`() {
        // Given
        Timber.uprootAll()
        
        // When
        val logger = loggingModule.provideLogger(isDebug = false)
        
        // Then
        assertNotNull(logger)
        assertTrue(logger is TimberLogger)
        assertEquals(0, Timber.treeCount, 
            "Release builds should not plant any Timber trees")
    }
}
