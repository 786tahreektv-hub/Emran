package com.example.core

object AppStrings {
    fun isBn(lang: String): Boolean = lang != "en"

    fun digits(value: Any, lang: String): String {
        val s = value.toString()
        if (lang == "en") return s
        val bn = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return buildString(s.length) {
            for (ch in s) {
                if (ch in '0'..'9') append(bn[ch - '0']) else append(ch)
            }
        }
    }

    fun t(lang: String, bn: String, en: String): String = if (isBn(lang)) bn else en

    fun navTitle(route: String, lang: String): String = when (route) {
        "home" -> t(lang, "হোম", "Home")
        "routine" -> t(lang, "রুটিন", "Routine")
        "amal" -> t(lang, "আমল", "Amal")
        "calendar" -> t(lang, "ক্যালেন্ডার", "Calendar")
        "profile" -> t(lang, "প্রোফাইল", "Profile")
        "daily_life" -> t(lang, "দৈনন্দিন জীবন", "Daily Life")
        "family" -> t(lang, "পরিবার ও সমাজ", "Family & Social")
        "madrasa_work" -> t(lang, "পেশা ও মাদরাসা", "Madrasa & Work")
        "education" -> t(lang, "শিক্ষা ও উন্নয়ন", "Education & Skills")
        "finance" -> t(lang, "আর্থিক হিসাব", "Finance")
        "diary" -> t(lang, "নোট ও ডায়েরি", "Diary & Notes")
        "reports" -> t(lang, "রিপোর্ট ও চার্ট", "Reports & Charts")
        "reminders" -> t(lang, "রিমাইন্ডার", "Reminders")
        else -> t(lang, "আমার জীবন", "My Life")
    }

    fun categoryLabel(cat: String, lang: String): String = when (cat) {
        "IBADAT" -> t(lang, "ইবাদত ও আমল", "Worship & Amal")
        "PERSONAL" -> t(lang, "ব্যক্তিগত জীবন", "Personal")
        "FAMILY" -> t(lang, "পরিবার ও সামাজিক", "Family & Social")
        "WORK_MADRASA" -> t(lang, "পেশা ও মাদরাসা", "Work & Madrasa")
        "EDUCATION" -> t(lang, "শিক্ষা ও উন্নয়ন", "Education & Skills")
        "HEALTH" -> t(lang, "স্বাস্থ্য ও বিশ্রাম", "Health & Rest")
        "FINANCE" -> t(lang, "আর্থিক কাজ", "Finance")
        else -> cat
    }

    fun priorityLabel(priority: Int, lang: String): String = when (priority) {
        3 -> t(lang, "জরুরি", "Urgent")
        2 -> t(lang, "গুরুত্বপূর্ণ", "Important")
        else -> t(lang, "সাধারণ", "Normal")
    }

    fun recurrenceLabel(rec: String, lang: String): String = when (rec) {
        "DAILY" -> t(lang, "দৈনিক", "Daily")
        "WEEKLY" -> t(lang, "সাপ্তাহিক", "Weekly")
        "MONTHLY" -> t(lang, "মাসিক", "Monthly")
        "YEARLY" -> t(lang, "বার্ষিক", "Yearly")
        else -> t(lang, "এককালীন", "Once")
    }

    fun prayerName(key: String, lang: String): String = when (key) {
        "fajr" -> t(lang, "ফজর", "Fajr")
        "sunrise" -> t(lang, "সূর্যোদয়", "Sunrise")
        "zuhr" -> t(lang, "যোহর", "Dhuhr")
        "asr" -> t(lang, "আসর", "Asr")
        "maghrib" -> t(lang, "মাগরিব", "Maghrib")
        "isha" -> t(lang, "এশা", "Isha")
        "tahajjud" -> t(lang, "তাহাজ্জুদ", "Tahajjud")
        else -> key
    }

    fun prayerStatusLabel(status: Int, lang: String): String = when (status) {
        2 -> t(lang, "জামাতে ✔", "Jamaat ✔")
        1 -> t(lang, "একা আদায়", "Alone ✔")
        else -> t(lang, "বাকি", "Pending")
    }

    fun financeTypeLabel(type: String, lang: String): String = when (type) {
        "INCOME" -> t(lang, "আয়", "Income")
        "EXPENSE" -> t(lang, "ব্যয়", "Expense")
        "SADAQAH" -> t(lang, "দান-সদকা", "Sadaqah")
        "SAVINGS" -> t(lang, "সঞ্চয়", "Savings")
        "DUE_RECEIVABLE" -> t(lang, "পাওনা বকেয়া", "Receivable")
        "DUE_PAYABLE" -> t(lang, "দেনা বকেয়া", "Payable")
        else -> type
    }
}
