package com.example.core

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class CityPreset(
    val nameBn: String,
    val nameEn: String,
    val lat: Double,
    val lon: Double,
    val tzId: String
)

data class DailyPrayerSchedule(
    val date: String,
    val fajr: String,
    val sunrise: String,
    val zuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val tahajjud: String
)

data class NextPrayerCountdown(
    val prayerKey: String,
    val prayerTime: String,
    val remainingSeconds: Long,
    val formattedRemaining: String
)

object PrayerTimeCalculator {
    val cities = listOf(
        CityPreset("ঢাকা", "Dhaka", 23.8103, 90.4125, "Asia/Dhaka"),
        CityPreset("চট্টগ্রাম", "Chattogram", 22.3569, 91.7832, "Asia/Dhaka"),
        CityPreset("সিলেট", "Sylhet", 24.8949, 91.8687, "Asia/Dhaka"),
        CityPreset("রাজশাহী", "Rajshahi", 24.3745, 88.6042, "Asia/Dhaka"),
        CityPreset("খুলনা", "Khulna", 22.8456, 89.5403, "Asia/Dhaka"),
        CityPreset("বরিশাল", "Barishal", 22.7010, 90.3535, "Asia/Dhaka"),
        CityPreset("রংপুর", "Rangpur", 25.7439, 89.2752, "Asia/Dhaka"),
        CityPreset("কুমিল্লা", "Cumilla", 23.4607, 91.1809, "Asia/Dhaka"),
        CityPreset("মক্কা", "Makkah", 21.3891, 39.8579, "Asia/Riyadh"),
        CityPreset("মদিনা", "Madinah", 24.5247, 39.5692, "Asia/Riyadh"),
        CityPreset("লন্ডন", "London", 51.5074, -0.1278, "Europe/London"),
        CityPreset("নিউ ইয়র্ক", "New York", 40.7128, -74.0060, "America/New_York")
    )

    fun methodLabel(method: String, lang: String): String = when (method) {
        "KARACHI" -> AppStrings.t(lang, "করাচি (ইসলামিক ফাউন্ডেশন/হানাফি মান)", "Karachi (18° / 18°)")
        "MWL" -> AppStrings.t(lang, "মুসলিম ওয়ার্ল্ড লিগ (MWL)", "Muslim World League (18° / 17°)")
        "ISNA" -> AppStrings.t(lang, "ISNA (উত্তর আমেরিকা)", "ISNA (15° / 15°)")
        "UMM_AL_QURA" -> AppStrings.t(lang, "উম্মুল কুরা (মক্কা)", "Umm al-Qura (18.5° / 90m)")
        "CUSTOM" -> AppStrings.t(lang, "নিজস্ব নির্ধারিত সময়", "Custom Manual Times")
        else -> method
    }

    fun calculate(
        isoDate: String,
        lat: Double,
        lon: Double,
        tzId: String,
        method: String,
        asrJuristic: String,
        customFajr: String = "05:00",
        customZuhr: String = "12:15",
        customAsr: String = "15:45",
        customMaghrib: String = "17:20",
        customIsha: String = "18:45"
    ): DailyPrayerSchedule {
        if (method == "CUSTOM") {
            return DailyPrayerSchedule(
                date = isoDate,
                fajr = customFajr,
                sunrise = addMinutesToHm(customFajr, 75),
                zuhr = customZuhr,
                asr = customAsr,
                maghrib = customMaghrib,
                isha = customIsha,
                tahajjud = addMinutesToHm(customFajr, -60)
            )
        }

        val parts = isoDate.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val month = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val day = parts.getOrNull(2)?.toIntOrNull() ?: 1

        val tz = TimeZone.getTimeZone(tzId)
        val cal = Calendar.getInstance(tz).apply {
            set(year, month - 1, day, 12, 0, 0)
        }
        val tzHours = tz.getOffset(cal.timeInMillis) / 3_600_000.0

        val jd = julianDate(year, month, day) - lon / (15.0 * 24.0)
        val (decl, eqTime) = sunPosition(jd)

        val dhuhrHours = fixHour(12.0 + tzHours - lon / 15.0 - eqTime)

        val fajrAngle = when (method) {
            "ISNA" -> 15.0
            "UMM_AL_QURA" -> 18.5
            else -> 18.0 // KARACHI, MWL
        }
        val ishaAngle = when (method) {
            "ISNA" -> 15.0
            "MWL" -> 17.0
            else -> 18.0
        }

        val sunriseDiff = sunAngleTime(0.833, lat, decl)
        val sunriseHours = dhuhrHours - sunriseDiff
        val sunsetHours = dhuhrHours + sunriseDiff

        val fajrHours = dhuhrHours - sunAngleTime(fajrAngle, lat, decl)
        val asrFactor = if (asrJuristic == "SHAFI") 1.0 else 2.0
        val asrAngle = -Math.toDegrees(atan(1.0 / (asrFactor + tan(Math.toRadians(kotlin.math.abs(lat - decl))))))
        val asrHours = dhuhrHours + sunAngleTime(asrAngle, lat, decl)
        val maghribHours = sunsetHours + (2.0 / 60.0) // +2 min safety margin
        val ishaHours = if (method == "UMM_AL_QURA") {
            maghribHours + 1.5
        } else {
            dhuhrHours + sunAngleTime(ishaAngle, lat, decl)
        }

        // Last third of the night for Tahajjud
        val nightDuration = (fajrHours + 24.0) - sunsetHours
        val tahajjudHours = fixHour(fajrHours - (nightDuration / 3.0) + 24.0)

        return DailyPrayerSchedule(
            date = isoDate,
            fajr = hoursToHm(fajrHours),
            sunrise = hoursToHm(sunriseHours),
            zuhr = hoursToHm(dhuhrHours + (2.0 / 60.0)),
            asr = hoursToHm(asrHours),
            maghrib = hoursToHm(maghribHours),
            isha = hoursToHm(ishaHours),
            tahajjud = hoursToHm(tahajjudHours)
        )
    }

    fun getNextPrayerCountdown(
        schedule: DailyPrayerSchedule,
        tzId: String,
        lang: String
    ): NextPrayerCountdown {
        val tz = TimeZone.getTimeZone(tzId)
        val now = Calendar.getInstance(tz)
        val nowSeconds =
            now.get(Calendar.HOUR_OF_DAY) * 3600L +
                now.get(Calendar.MINUTE) * 60L +
                now.get(Calendar.SECOND)

        val entries = listOf(
            "fajr" to schedule.fajr,
            "zuhr" to schedule.zuhr,
            "asr" to schedule.asr,
            "maghrib" to schedule.maghrib,
            "isha" to schedule.isha
        )

        for ((key, hm) in entries) {
            val sec = hmToSeconds(hm)
            if (sec > nowSeconds) {
                val diff = sec - nowSeconds
                return NextPrayerCountdown(
                    prayerKey = key,
                    prayerTime = hm,
                    remainingSeconds = diff,
                    formattedRemaining = formatDuration(diff, lang)
                )
            }
        }
        // After Isha -> next is tomorrow's Fajr
        val fajrSec = hmToSeconds(schedule.fajr)
        val diff = (24 * 3600L - nowSeconds) + fajrSec
        return NextPrayerCountdown(
            prayerKey = "fajr",
            prayerTime = schedule.fajr,
            remainingSeconds = diff,
            formattedRemaining = formatDuration(diff, lang)
        )
    }

    private fun hmToSeconds(hm: String): Long {
        val p = hm.split(":")
        val h = p.getOrNull(0)?.toLongOrNull() ?: 0L
        val m = p.getOrNull(1)?.toLongOrNull() ?: 0L
        return h * 3600L + m * 60L
    }

    private fun formatDuration(totalSec: Long, lang: String): String {
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        val raw = String.format(java.util.Locale.US, "%02d:%02d:%02d", h, m, s)
        return AppStrings.digits(raw, lang)
    }

    private fun addMinutesToHm(hm: String, deltaMin: Int): String {
        val p = hm.split(":")
        val h = p.getOrNull(0)?.toIntOrNull() ?: 5
        val m = p.getOrNull(1)?.toIntOrNull() ?: 0
        var total = (h * 60 + m + deltaMin) % (24 * 60)
        if (total < 0) total += 24 * 60
        return String.format(java.util.Locale.US, "%02d:%02d", total / 60, total % 60)
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        val eqTime = q / 15.0 - fixHour(ra)
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        return decl to eqTime
    }

    private fun sunAngleTime(angle: Double, lat: Double, decl: Double): Double {
        val num = -sin(Math.toRadians(angle)) - sin(Math.toRadians(lat)) * sin(Math.toRadians(decl))
        val den = cos(Math.toRadians(lat)) * cos(Math.toRadians(decl))
        val cosVal = (num / den).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cosVal)) / 15.0
    }

    private fun fixAngle(a: Double): Double {
        val res = a - 360.0 * floor(a / 360.0)
        return if (res < 0) res + 360.0 else res
    }

    private fun fixHour(a: Double): Double {
        val res = a - 24.0 * floor(a / 24.0)
        return if (res < 0) res + 24.0 else res
    }

    private fun hoursToHm(time: Double): String {
        val t = fixHour(time + 0.5 / 60.0)
        val hours = floor(t).toInt()
        val minutes = floor((t - hours) * 60.0).toInt().coerceIn(0, 59)
        return String.format(java.util.Locale.US, "%02d:%02d", hours % 24, minutes)
    }
}
