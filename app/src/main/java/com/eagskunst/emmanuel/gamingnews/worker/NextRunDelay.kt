package com.eagskunst.emmanuel.gamingnews.worker

import java.util.Calendar

/**
 * Milliseconds from now until the next occurrence of [hour] (local time, top of the hour).
 * Shared by the daily-reminder and game-release schedulers so both anchor to local midnight-
 * hour semantics identically.
 */
internal fun computeInitialDelayToHour(hour: Int): Long {
    val current = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(current)) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
    return target.timeInMillis - current.timeInMillis
}
