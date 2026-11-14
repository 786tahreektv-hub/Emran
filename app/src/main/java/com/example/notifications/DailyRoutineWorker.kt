package com.example.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.AmarJibonApp
import com.example.core.AppStrings
import com.example.core.CalendarEngine
import com.example.core.PrayerTimeCalculator
import java.util.Calendar
import java.util.TimeZone

class DailyRoutineWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? AmarJibonApp ?: return Result.success()
        val prefs = app.prefsRepository.getSnapshot()
        if (!prefs.setupCompleted) return Result.success()

        // Reschedule today's alarms cleanly so prayer/custom reminders stay reliable
        ReminderScheduler.scheduleAllForToday(applicationContext, prefs, app.database.lifeModulesDao().getAllRemindersOnce())

        // Periodic hydration nudge if enabled during daytime (07:00 - 21:00)
        if (prefs.waterRemindersEnabled) {
            val tz = TimeZone.getTimeZone(prefs.timezoneId)
            val hour = Calendar.getInstance(tz).get(Calendar.HOUR_OF_DAY)
            if (hour in 7..21) {
                val interval = prefs.waterReminderIntervalHours.coerceIn(1, 6)
                if (hour % interval == 0) {
                    NotificationHelper.showNotification(
                        applicationContext,
                        4001,
                        NotificationHelper.CHANNEL_HEALTH,
                        AppStrings.t(prefs.language, "পানি পানের সময়", "Time to Drink Water"),
                        AppStrings.t(
                            prefs.language,
                            "সুস্থ থাকার জন্য ১ গ্লাস পানি পান করুন এবং অ্যাপে আপডেট দিন।",
                            "Drink a glass of water and log it in My Life."
                        )
                    )
                }
            }
        }

        return Result.success()
    }
}
