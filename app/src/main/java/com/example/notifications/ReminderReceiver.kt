package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "আমার জীবন রিমাইন্ডার"
        val body = intent.getStringExtra(EXTRA_BODY) ?: "আপনার নির্ধারিত সময় হয়েছে।"
        val channel = intent.getStringExtra(EXTRA_CHANNEL) ?: NotificationHelper.CHANNEL_ROUTINE
        val id = intent.getIntExtra(EXTRA_ID, (System.currentTimeMillis() % 100000).toInt())

        NotificationHelper.showNotification(context, id, channel, title, body)
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_BODY = "extra_body"
        const val EXTRA_CHANNEL = "extra_channel"
        const val EXTRA_ID = "extra_id"
    }
}
