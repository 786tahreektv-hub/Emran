package com.example.core

import android.icu.util.IslamicCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class TripleDateInfo(
    val isoDate: String,
    val weekdayIndex: Int, // 0 = Sun .. 6 = Sat
    val weekdayBn: String,
    val weekdayEn: String,
    val gregorianDisplay: String,
    val banglaDisplay: String,
    val hijriDisplay: String,
    val hijriMonth: Int,
    val hijriDay: Int,
    val isLeapYear: Boolean,
    val specialOccasion: String?
)

object CalendarEngine {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val weekdaysBn = arrayOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")
    private val weekdaysEn = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    private val gregMonthsBn = arrayOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন",
        "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )
    private val gregMonthsEn = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val banglaMonthsBn = arrayOf(
        "বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন",
        "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র"
    )
    private val banglaMonthsEn = arrayOf(
        "Boishakh", "Joishtho", "Asharh", "Srabon", "Bhadro", "Ashwin",
        "Kartik", "Agrahayon", "Poush", "Magh", "Falgun", "Chaitra"
    )

    private val hijriMonthsBn = arrayOf(
        "মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জুমাদাল উলা", "জুমাদাস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )
    private val hijriMonthsEn = arrayOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Ula", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    fun todayIso(tzId: String = "Asia/Dhaka"): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone(tzId)
        }
        return fmt.format(Date())
    }

    fun currentTimeHm(tzId: String = "Asia/Dhaka"): String {
        val fmt = SimpleDateFormat("HH:mm", Locale.US).apply {
            timeZone = TimeZone.getTimeZone(tzId)
        }
        return fmt.format(Date())
    }

    fun currentTimeHms(tzId: String = "Asia/Dhaka", lang: String = "bn"): String {
        val fmt = SimpleDateFormat("hh:mm:ss a", Locale.US).apply {
            timeZone = TimeZone.getTimeZone(tzId)
        }
        val raw = fmt.format(Date())
        return if (lang == "en") raw else AppStrings.digits(
            raw.replace("AM", "পূর্বাহ্ণ").replace("PM", "অপরাহ্ণ"),
            lang
        )
    }

    fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    fun daysInYear(year: Int): Int = if (isLeapYear(year)) 366 else 365

    fun daysInMonth(year: Int, month1to12: Int): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(year, month1to12 - 1, 1, 0, 0, 0)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun addDays(isoDate: String, deltaDays: Int): String {
        val cal = parseUtcCalendar(isoDate)
        cal.add(Calendar.DAY_OF_MONTH, deltaDays)
        return formatUtcCalendar(cal)
    }

    fun dayOfWeek(isoDate: String): Int {
        val cal = parseUtcCalendar(isoDate)
        return cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sun .. 6 = Sat
    }

    fun getMonthDates(year: Int, month1to12: Int): List<String> {
        val count = daysInMonth(year, month1to12)
        return (1..count).map { d ->
            String.format(Locale.US, "%04d-%02d-%02d", year, month1to12, d)
        }
    }

    fun getYearDates(year: Int): List<String> {
        val list = ArrayList<String>(daysInYear(year))
        for (m in 1..12) {
            list.addAll(getMonthDates(year, m))
        }
        return list
    }

    fun gregMonthName(month1to12: Int, lang: String): String =
        if (lang == "en") gregMonthsEn[(month1to12 - 1).coerceIn(0, 11)]
        else gregMonthsBn[(month1to12 - 1).coerceIn(0, 11)]

    fun weekdayShort(index0to6: Int, lang: String): String {
        val bn = arrayOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র", "শনি")
        val en = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val idx = index0to6.coerceIn(0, 6)
        return if (lang == "en") en[idx] else bn[idx]
    }

    fun getTripleDate(isoDate: String, hijriOffsetDays: Int = 0, lang: String = "bn"): TripleDateInfo {
        val parts = isoDate.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
        val wd = dayOfWeek(isoDate)

        // Gregorian string
        val gregDisplay = if (lang == "en") {
            "$d ${gregMonthsEn[m - 1]} $y"
        } else {
            "${AppStrings.digits(d, lang)} ${gregMonthsBn[m - 1]} ${AppStrings.digits(y, lang)}"
        }

        // Bangla Calendar (Revised Bangladesh Standard: April 14 = 1 Boishakh)
        val banglaStr = computeBanglaDate(y, m, d, lang)

        // Hijri Calendar via Android ICU IslamicCalendar
        val hijriCal = IslamicCalendar()
        val gCal = parseUtcCalendar(isoDate)
        gCal.add(Calendar.DAY_OF_MONTH, hijriOffsetDays)
        hijriCal.timeInMillis = gCal.timeInMillis
        val hYear = hijriCal.get(IslamicCalendar.YEAR)
        val hMonth = hijriCal.get(IslamicCalendar.MONTH) // 0..11
        val hDay = hijriCal.get(IslamicCalendar.DAY_OF_MONTH)
        val hijriStr = if (lang == "en") {
            "$hDay ${hijriMonthsEn[hMonth.coerceIn(0, 11)]} $hYear AH"
        } else {
            "${AppStrings.digits(hDay, lang)} ${hijriMonthsBn[hMonth.coerceIn(0, 11)]} ${AppStrings.digits(hYear, lang)} হি."
        }

        val occasion = detectSpecialOccasion(m, d, wd, hMonth + 1, hDay, lang)

        return TripleDateInfo(
            isoDate = isoDate,
            weekdayIndex = wd,
            weekdayBn = weekdaysBn[wd],
            weekdayEn = weekdaysEn[wd],
            gregorianDisplay = gregDisplay,
            banglaDisplay = banglaStr,
            hijriDisplay = hijriStr,
            hijriMonth = hMonth + 1,
            hijriDay = hDay,
            isLeapYear = isLeapYear(y),
            specialOccasion = occasion
        )
    }

    private fun computeBanglaDate(y: Int, m: Int, d: Int, lang: String): String {
        val currentUtc = utcMillis(y, m, d)
        val boishakhStartYear = if (currentUtc >= utcMillis(y, 4, 14)) y else y - 1
        var diffDays = ((currentUtc - utcMillis(boishakhStartYear, 4, 14)) / 86_400_000L).toInt()
        val leapBangla = isLeapYear(boishakhStartYear + 1)
        // Revised Bangla Calendar (Bangladesh Academy 2019):
        // Boishakh-Bhadro (5) = 31, Ashwin-Poush? Actually Boishakh-Ashwin (6 months)=31, Kartik-Chaitra=30, Falgun=29 (30 in leap year)
        val monthLengths = intArrayOf(
            31, 31, 31, 31, 31, 31,
            30, 30, 30, 30,
            if (leapBangla) 30 else 29,
            30
        )
        var idx = 0
        while (idx < 11 && diffDays >= monthLengths[idx]) {
            diffDays -= monthLengths[idx]
            idx++
        }
        val bnDay = diffDays + 1
        val bnYear = boishakhStartYear - 593
        return if (lang == "en") {
            "$bnDay ${banglaMonthsEn[idx]} $bnYear"
        } else {
            "${AppStrings.digits(bnDay, lang)} ${banglaMonthsBn[idx]} ${AppStrings.digits(bnYear, lang)}"
        }
    }

    private fun detectSpecialOccasion(
        gMonth: Int,
        gDay: Int,
        weekdayIndex: Int,
        hMonth: Int,
        hDay: Int,
        lang: String
    ): String? {
        val tags = mutableListOf<String>()
        if (weekdayIndex == 5) {
            tags.add(AppStrings.t(lang, "জুমার দিন", "Jumu'ah"))
        }
        if (hMonth == 9 && hDay == 1) tags.add(AppStrings.t(lang, "রমজান শুরু", "1st Ramadan"))
        if (hMonth == 9 && hDay in 21..29 && hDay % 2 == 1) tags.add(AppStrings.t(lang, "শবে কদরের সম্ভাব্য রাত", "Odd Night of Laylatul Qadr"))
        if (hMonth == 10 && hDay == 1) tags.add(AppStrings.t(lang, "ঈদুল ফিতর", "Eid al-Fitr"))
        if (hMonth == 12 && hDay == 9) tags.add(AppStrings.t(lang, "আরাফাহর দিন (নফল রোজা)", "Day of Arafah"))
        if (hMonth == 12 && hDay == 10) tags.add(AppStrings.t(lang, "ঈদুল আযহা", "Eid al-Adha"))
        if (hMonth == 1 && hDay == 10) tags.add(AppStrings.t(lang, "পবিত্র আশুরা", "Day of Ashura"))
        if (hMonth == 8 && hDay == 15) tags.add(AppStrings.t(lang, "শবে বরাত (নিসফে শাবান)", "Mid-Sha'ban"))
        if (hDay == 13 || hDay == 14 || hDay == 15) tags.add(AppStrings.t(lang, "আইয়ামে বীয (সুন্নাহ রোজা)", "Ayyam al-Beed"))
        if (gMonth == 2 && gDay == 21) tags.add(AppStrings.t(lang, "আন্তর্জাতিক মাতৃভাষা দিবস", "Language Movement Day"))
        if (gMonth == 3 && gDay == 26) tags.add(AppStrings.t(lang, "স্বাধীনতা দিবস", "Independence Day"))
        if (gMonth == 4 && gDay == 14) tags.add(AppStrings.t(lang, "পহেলা বৈশাখ", "Pohela Boishakh"))
        if (gMonth == 12 && gDay == 16) tags.add(AppStrings.t(lang, "বিজয় দিবস", "Victory Day"))
        return if (tags.isEmpty()) null else tags.joinToString(" • ")
    }

    private fun utcMillis(y: Int, m: Int, d: Int): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.clear()
        cal.set(y, m - 1, d, 0, 0, 0)
        return cal.timeInMillis
    }

    private fun parseUtcCalendar(isoDate: String): Calendar {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val parts = isoDate.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
        cal.clear()
        cal.set(y, m - 1, d, 12, 0, 0)
        return cal
    }

    private fun formatUtcCalendar(cal: Calendar): String {
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
