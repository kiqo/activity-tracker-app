package com.activitytracker.app.util

import timber.log.Timber

/**
 * Timber-based implementation of Logger interface.
 * Uses Timber library for actual logging.
 * 
 * Note: Timber initialization is handled by LoggingModule.
 * This class should not be instantiated directly - use dependency injection.
 */
class TimberLogger : Logger {
    
    override fun d(message: String) {
        Timber.d(message)
    }
    
    override fun d(throwable: Throwable, message: String) {
        Timber.d(throwable, message)
    }
    
    override fun i(message: String) {
        Timber.i(message)
    }
    
    override fun i(throwable: Throwable, message: String) {
        Timber.i(throwable, message)
    }
    
    override fun w(message: String) {
        Timber.w(message)
    }
    
    override fun w(throwable: Throwable, message: String) {
        Timber.w(throwable, message)
    }
    
    override fun e(message: String) {
        Timber.e(message)
    }
    
    override fun e(throwable: Throwable, message: String) {
        Timber.e(throwable, message)
    }
    
    override fun v(message: String) {
        Timber.v(message)
    }
    
    override fun v(throwable: Throwable, message: String) {
        Timber.v(throwable, message)
    }
}
