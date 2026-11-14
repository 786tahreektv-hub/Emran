package com.example

import android.app.Application
import androidx.work.Configuration
import com.example.data.local.AmarJibonDatabase
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.repository.AmarJibonRepository
import com.example.notifications.NotificationHelper
import com.example.notifications.ReminderScheduler

class AmarJibonApp : Application(), Configuration.Provider {
    val database: AmarJibonDatabase by lazy { AmarJibonDatabase.getInstance(this) }
    val prefsRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }
    val repository: AmarJibonRepository by lazy { AmarJibonRepository(this, database, prefsRepository) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        ReminderScheduler.enqueueBackgroundWorker(this)
    }
}
