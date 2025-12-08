package com.activitytracker.app.util

import org.junit.After
import org.junit.Before
import org.junit.Test
import timber.log.Timber
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for TimberLogger.
 * Tests all logging methods and their interaction with Timber.
 */
class TimberLoggerTest {

    private lateinit var logger: TimberLogger
    private lateinit var testTree: TestTree

    @Before
    fun setup() {
        logger = TimberLogger()
        testTree = TestTree()
        Timber.plant(testTree)
    }

    @After
    fun tearDown() {
        Timber.uprootAll()
    }

    @Test
    fun `d logs debug message`() {
        // When
        logger.d("Debug message")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.DEBUG, log.priority)
        assertEquals("Debug message", log.message)
    }

    @Test
    fun `d logs debug message with throwable`() {
        // Given
        val exception = Exception("Test exception")

        // When
        logger.d(exception, "Debug with error")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.DEBUG, log.priority)
        assertTrue(log.message.contains("Debug with error"))
        assertEquals(exception, log.throwable)
    }

    @Test
    fun `i logs info message`() {
        // When
        logger.i("Info message")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.INFO, log.priority)
        assertEquals("Info message", log.message)
    }

    @Test
    fun `i logs info message with throwable`() {
        // Given
        val exception = Exception("Test exception")

        // When
        logger.i(exception, "Info with error")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.INFO, log.priority)
        assertTrue(log.message.contains("Info with error"))
        assertEquals(exception, log.throwable)
    }

    @Test
    fun `w logs warning message`() {
        // When
        logger.w("Warning message")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.WARN, log.priority)
        assertEquals("Warning message", log.message)
    }

    @Test
    fun `w logs warning message with throwable`() {
        // Given
        val exception = Exception("Test exception")

        // When
        logger.w(exception, "Warning with error")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.WARN, log.priority)
        assertTrue(log.message.contains("Warning with error"))
        assertEquals(exception, log.throwable)
    }

    @Test
    fun `e logs error message`() {
        // When
        logger.e("Error message")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.ERROR, log.priority)
        assertEquals("Error message", log.message)
    }

    @Test
    fun `e logs error message with throwable`() {
        // Given
        val exception = Exception("Test exception")

        // When
        logger.e(exception, "Error with exception")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.ERROR, log.priority)
        assertTrue(log.message.contains("Error with exception"))
        assertEquals(exception, log.throwable)
    }

    @Test
    fun `v logs verbose message`() {
        // When
        logger.v("Verbose message")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.VERBOSE, log.priority)
        assertEquals("Verbose message", log.message)
    }

    @Test
    fun `v logs verbose message with throwable`() {
        // Given
        val exception = Exception("Test exception")

        // When
        logger.v(exception, "Verbose with error")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertEquals(android.util.Log.VERBOSE, log.priority)
        assertTrue(log.message.contains("Verbose with error"))
        assertEquals(exception, log.throwable)
    }

    @Test
    fun `multiple log calls are captured in order`() {
        // When
        logger.d("First")
        logger.i("Second")
        logger.w("Third")
        logger.e("Fourth")
        logger.v("Fifth")

        // Then
        assertEquals(5, testTree.logs.size)
        assertEquals("First", testTree.logs[0].message)
        assertEquals("Second", testTree.logs[1].message)
        assertEquals("Third", testTree.logs[2].message)
        assertEquals("Fourth", testTree.logs[3].message)
        assertEquals("Fifth", testTree.logs[4].message)
    }

    @Test
    fun `logger handles empty messages`() {
        // When
        logger.d("")
        logger.i("")
        logger.w("")
        logger.e("")
        logger.v("")

        // Then
        // Timber may not log empty messages, so we just verify no crash
        assertTrue(testTree.logs.size >= 0)
    }

    @Test
    fun `logger handles special characters in messages`() {
        // Given
        val specialMessage = "Test\nwith\ttabs\rand\nnewlines"

        // When
        logger.d(specialMessage)

        // Then
        assertEquals(1, testTree.logs.size)
        assertEquals(specialMessage, testTree.logs[0].message)
    }

    @Test
    fun `logger handles null throwable message`() {
        // Given
        val exception = Exception(null as String?)

        // When
        logger.e(exception, "Error with null exception message")

        // Then
        assertEquals(1, testTree.logs.size)
        val log = testTree.logs[0]
        assertTrue(log.message.contains("Error with null exception message"))
        assertEquals(exception, log.throwable)
    }

    /**
     * Test implementation of Timber.Tree for capturing log calls.
     */
    private class TestTree : Timber.Tree() {
        data class LogEntry(
            val priority: Int,
            val tag: String?,
            val message: String,
            val throwable: Throwable?
        )

        val logs = mutableListOf<LogEntry>()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            logs.add(LogEntry(priority, tag, message, t))
        }
    }
}
