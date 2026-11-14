package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_PRAYER = "amar_jibon_prayer"
    const val CHANNEL_ROUTINE = "amar_jibon_routine"
    const val CHANNEL_HEALTH = "amar_jibon_health"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_PRAYER,
                    "নামাজ ও আমল রিমাইন্ডার",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "নামাজের ওয়াক্ত ও দৈনিক আমলের নোটিফিকেশন" },
                NotificationChannel(
                    CHANNEL_ROUTINE,
                    "দৈনিক কাজ ও গুরুত্বপূর্ণ তারিখ",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "২৪ ঘণ্টার রুটিন ও নির্ধারিত কাজের রিমাইন্ডার" },
                NotificationChannel(
                    CHANNEL_HEALTH,
                    "পানি, ওষুধ ও ঘুমের রিমাইন্ডার",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "স্বাস্থ্য ও জীবনযাত্রার নিয়মিত রিমাইন্ডার" }
            )
            channels.forEach { nm.createNotificationChannel(it) }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        body: String
    ) {
        if (!hasNotificationPermission(context)) return
        createChannels(context)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                if (channelId == CHANNEL_PRAYER) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }
}
