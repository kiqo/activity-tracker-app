package com.activitytracker.app.util

import timber.log.Timber

/**
 * Logging abstraction interface.
 * Provides a clean API for logging throughout the application.
 * Implementation can be swapped without changing application code.
 */
interface Logger {
    
    companion object {
        /**
         * Initialize the logging framework.
         * Should be called once in Application.onCreate().
         * 
         * @param isDebug Whether the app is running in debug mode
         */
        fun initializeLogger(isDebug: Boolean) {
            if (isDebug) {
                Timber.plant(Timber.DebugTree())
            }
        }
    }
    
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
