package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.AmarJibonApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val app = context.applicationContext as? AmarJibonApp
                    if (app != null) {
                        val prefs = app.prefsRepository.getSnapshot()
                        val reminders = app.database.lifeModulesDao().getAllRemindersOnce()
                        ReminderScheduler.scheduleAllForToday(context, prefs, reminders)
                        ReminderScheduler.enqueueBackgroundWorker(context)
                    }
                } finally {
                    pending.finish()
                }
            }
        }
    }
}
