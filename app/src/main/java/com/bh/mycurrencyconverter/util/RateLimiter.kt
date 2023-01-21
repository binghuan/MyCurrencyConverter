package com.bh.mycurrencyconverter.util

import java.util.concurrent.TimeUnit

/**
 * Utility class that decides whether we should fetch some data or not.
 */
class RateLimiter(timeout: Int, timeUnit: TimeUnit) {

    private val timeout = timeUnit.toMillis(timeout.toLong())

    @Synchronized
    fun shouldFetch(lastFetched: Long?): Boolean {
        val now = now()
        if (lastFetched == null) {
            return true
        }
        val diff = now - lastFetched
        if (diff > timeout) {
            return true
        }
        return false
    }

    private fun now() = System.currentTimeMillis()

}
