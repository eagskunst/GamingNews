package com.eagskunst.emmanuel.gamingnews.core.data.source.local

import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `given null timestamp when fromTimestamp then returns null`() {
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun `given timestamp when fromTimestamp then returns date with same time`() {
        val timestamp = 1_700_000_000_000L

        val date = converters.fromTimestamp(timestamp)

        assertEquals(Date(timestamp), date)
    }

    @Test
    fun `given null date when dateToTimestamp then returns null`() {
        assertNull(converters.dateToTimestamp(null))
    }

    @Test
    fun `given date when dateToTimestamp then returns its time`() {
        val date = Date(1_700_000_000_000L)

        val timestamp = converters.dateToTimestamp(date)

        assertEquals(date.time, timestamp)
    }

    @Test
    fun `given date when round tripped through timestamp then equal`() {
        val date = Date(1_700_000_000_000L)

        val roundTripped = converters.fromTimestamp(converters.dateToTimestamp(date))

        assertEquals(date, roundTripped)
    }
}
