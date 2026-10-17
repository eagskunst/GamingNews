package com.eagskunst.emmanuel.gamingnews.core.domain.model

/**
 * A reminder trigger relative to a followed game's release day. [bit] is the position used in
 * the compact bitmask persisted on each followed-games row; [daysBefore] is how many calendar
 * days before release the reminder fires (0 = release day itself).
 */
enum class ReminderOffset(val bit: Int, val daysBefore: Int) {
    RELEASE_DAY(bit = 1, daysBefore = 0),
    ONE_DAY_BEFORE(bit = 2, daysBefore = 1),
    ONE_WEEK_BEFORE(bit = 4, daysBefore = 7)
}

fun Set<ReminderOffset>.toMask(): Int = fold(0) { mask, offset -> mask or offset.bit }

fun Int.toOffsets(): Set<ReminderOffset> =
    ReminderOffset.entries.filterTo(mutableSetOf()) { this and it.bit != 0 }
