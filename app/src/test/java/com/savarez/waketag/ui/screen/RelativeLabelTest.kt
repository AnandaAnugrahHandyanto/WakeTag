package com.savarez.waketag.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeLabelTest {

    private val now = 1_000_000_000L

    @Test
    fun pastTime_showsSekarang() {
        assertEquals("Sekarang", relativeLabel(now - 1L, now))
    }

    @Test
    fun underOneMinute_showsSeconds() {
        assertEquals("30 detik lagi", relativeLabel(now + 30_000L, now))
    }

    @Test
    fun fiveMinutes_showsFiveMenit() {
        assertEquals("5 menit lagi", relativeLabel(now + 5 * 60_000L, now))
    }

    @Test
    fun minutesRoundUpSoSnoozeDoesNotShowFour() {
        assertEquals("5 menit lagi", relativeLabel(now + 4 * 60_000L + 59_000L, now))
    }

    @Test
    fun hoursAndMinutes_areBothShown() {
        assertEquals(
            "1 jam 5 menit lagi",
            relativeLabel(now + 65 * 60_000L, now)
        )
    }

    @Test
    fun wholeDay_showsOneDay() {
        assertEquals("1 hari lagi", relativeLabel(now + 24 * 60 * 60_000L, now))
    }

    @Test
    fun daysAndHours_areBothShown() {
        assertEquals(
            "2 hari 3 jam lagi",
            relativeLabel(now + (2 * 24 + 3) * 60 * 60_000L, now)
        )
    }
}
