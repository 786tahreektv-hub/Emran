package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.core.AppStrings
import com.example.core.CalendarEngine
import com.example.core.PrayerTimeCalculator
import com.example.data.local.CustomReminderEntity
import com.example.data.prefs.UserPreferences
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val WORK_NAME = "amar_jibon_periodic_reminder_worker"

    fun enqueueBackgroundWorker(context: Context) {
        runCatching {
            val request = PeriodicWorkRequestBuilder<DailyRoutineWorker>(1, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    fun scheduleAllForToday(
        context: Context,
        prefs: UserPreferences,
        customReminders: List<CustomReminderEntity>
    ) {
        val tz = TimeZone.getTimeZone(prefs.timezoneId)
        val today = CalendarEngine.todayIso(prefs.timezoneId)
        val schedule = PrayerTimeCalculator.calculate(
            isoDate = today,
            lat = prefs.latitude,
            lon = prefs.longitude,
            tzId = prefs.timezoneId,
            method = prefs.prayerCalcMethod,
            asrJuristic = prefs.asrJuristic,
            customFajr = prefs.customFajr,
            customZuhr = prefs.customZuhr,
            customAsr = prefs.customAsr,
            customMaghrib = prefs.customMaghrib,
            customIsha = prefs.customIsha
        )

        if (prefs.prayerRemindersEnabled) {
            val prayers = listOf(
                101 to ("fajr" to schedule.fajr),
                102 to ("zuhr" to schedule.zuhr),
                103 to ("asr" to schedule.asr),
                104 to ("maghrib" to schedule.maghrib),
                105 to ("isha" to schedule.isha)
            )
            prayers.forEach { (id, pair) ->
                val name = AppStrings.prayerName(pair.first, prefs.language)
                scheduleDailyHm(
                    context = context,
                    requestCode = id,
                    hm = pair.second,
                    tz = tz,
                    channel = NotificationHelper.CHANNEL_PRAYER,
                    title = AppStrings.t(prefs.language, "$name-এর ওয়াক্ত হয়েছে", "Time for $name Prayer"),
                    body = AppStrings.t(
                        prefs.language,
                        "অজু ও মিসওয়াক করে জামাতের সাথে $name আদায়ের প্রস্তুতি নিন।",
                        "Prepare for $name prayer with wudu and miswak."
                    )
                )
            }
        }

        // Wake & Sleep reminders
        if (prefs.wakeReminderTime.isNotBlank()) {
            scheduleDailyHm(
                context, 201, prefs.wakeReminderTime, tz,
                NotificationHelper.CHANNEL_HEALTH,
                AppStrings.t(prefs.language, "ঘুম থেকে ওঠার সময়", "Time to Wake Up"),
                AppStrings.t(prefs.language, "আলহামদুলিল্লাহ! তাহাজ্জুদ ও ফজরের জন্য প্রস্তুতি নিন।", "Alhamdulillah! Prepare for Tahajjud and Fajr.")
            )
        }
        if (prefs.sleepReminderTime.isNotBlank()) {
            scheduleDailyHm(
                context, 202, prefs.sleepReminderTime, tz,
                NotificationHelper.CHANNEL_HEALTH,
                AppStrings.t(prefs.language, "ঘুমানোর সময়", "Time to Sleep"),
                AppStrings.t(prefs.language, "ঘুমের দোয়া পড়ে সুন্নাহ মোতাবেক বিশ্রাম নিন।", "Recite sleep dua and rest on time.")
            )
        }

        // Custom reminders (Medicine, Important dates, Tasks, Amal)
        customReminders.filter { it.isEnabled }.forEachIndexed { index, r ->
            if (r.date.isBlank() || r.date == today) {
                scheduleDailyHm(
                    context,
                    300 + index,
                    r.time,
                    tz,
                    NotificationHelper.CHANNEL_ROUTINE,
                    r.title,
                    AppStrings.t(prefs.language, "নির্ধারিত রিমাইন্ডার: ${r.title}", "Scheduled reminder: ${r.title}")
                )
            }
        }
    }

    private fun scheduleDailyHm(
        context: Context,
        requestCode: Int,
        hm: String,
        tz: TimeZone,
        channel: String,
        title: String,
        body: String
    ) {
        val parts = hm.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return
        val m = parts.getOrNull(1)?.toIntOrNull() ?: return

        val now = Calendar.getInstance(tz)
        val target = Calendar.getInstance(tz).apply {
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis) return

        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_ID, requestCode)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_BODY, body)
            putExtra(ReminderReceiver.EXTRA_CHANNEL, channel)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pi)
        } catch (_: SecurityException) {
        }
    }
}
