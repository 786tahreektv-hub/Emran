package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.AppStrings
import com.example.core.CalendarEngine
import com.example.core.PrayerTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Bengali title`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("আমার জীবন", appName)
    }

    @Test
    fun `calendar engine supports leap year and triple dates`() {
        assertTrue(CalendarEngine.isLeapYear(2024))
        assertEquals(366, CalendarEngine.daysInYear(2024))
        assertFalse(CalendarEngine.isLeapYear(2026))
        assertEquals(365, CalendarEngine.daysInYear(2026))

        val info = CalendarEngine.getTripleDate("2026-04-14", 0, "bn")
        assertTrue(info.banglaDisplay.contains("১ বৈশাখ"))
        assertTrue(info.hijriDisplay.isNotBlank())
    }

    @Test
    fun `prayer time calculator returns ordered daily prayer times`() {
        val schedule = PrayerTimeCalculator.calculate(
            isoDate = "2026-11-14",
            lat = 23.8103,
            lon = 90.4125,
            tzId = "Asia/Dhaka",
            method = "KARACHI",
            asrJuristic = "HANAFI"
        )
        assertTrue(schedule.fajr < schedule.sunrise)
        assertTrue(schedule.sunrise < schedule.zuhr)
        assertTrue(schedule.zuhr < schedule.asr)
        assertTrue(schedule.asr < schedule.maghrib)
        assertTrue(schedule.maghrib < schedule.isha)
    }

    @Test
    fun `bengali digits formatter converts digits accurately`() {
        assertEquals("১২৩৪৫", AppStrings.digits(12345, "bn"))
        assertEquals("12345", AppStrings.digits(12345, "en"))
    }
}
