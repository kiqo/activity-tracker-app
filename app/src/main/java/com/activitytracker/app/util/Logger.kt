package com.activitytracker.app.util

/**
 * Logging abstraction interface.
 * Provides a clean API for logging throughout the application.
 * Implementation can be swapped without changing application code.
 */
interface Logger {
    /**
     * Log a debug message.
     */
    fun d(message: String)
    
    /**
     * Log a debug message with throwable.
     */
    fun d(throwable: Throwable, message: String)
    
    /**
     * Log an info message.
     */
    fun i(message: String)
    
    /**
     * Log an info message with throwable.
     */
    fun i(throwable: Throwable, message: String)
    
    /**
     * Log a warning message.
     */
    fun w(message: String)
    
    /**
     * Log a warning message with throwable.
     */
    fun w(throwable: Throwable, message: String)
    
    /**
     * Log an error message.
     */
    fun e(message: String)
    
    /**
     * Log an error message with throwable.
     */
    fun e(throwable: Throwable, message: String)
    
    /**
     * Log a verbose message.
     */
    fun v(message: String)
    
    /**
     * Log a verbose message with throwable.
     */
    fun v(throwable: Throwable, message: String)
}
