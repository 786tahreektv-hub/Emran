package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "amar_jibon_prefs")

data class UserPreferences(
    val setupCompleted: Boolean = false,
    val userName: String = "",
    val profilePhotoUri: String = "",
    val language: String = "bn", // "bn" or "en"
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val dailyQuranTargetPages: Int = 5,
    val dailyWaterTargetGlasses: Int = 8,
    val monthlyBudget: Double = 0.0,
    val prayerCalcMethod: String = "KARACHI", // KARACHI, MWL, ISNA, UMM_AL_QURA, CUSTOM
    val asrJuristic: String = "HANAFI", // HANAFI, SHAFI
    val cityName: String = "ঢাকা",
    val latitude: Double = 23.8103,
    val longitude: Double = 90.4125,
    val timezoneId: String = "Asia/Dhaka",
    val hijriOffset: Int = 0,
    val customFajr: String = "05:00",
    val customZuhr: String = "12:15",
    val customAsr: String = "15:45",
    val customMaghrib: String = "17:20",
    val customIsha: String = "18:45",
    val prayerRemindersEnabled: Boolean = true,
    val waterRemindersEnabled: Boolean = false,
    val waterReminderIntervalHours: Int = 2,
    val wakeReminderTime: String = "04:30",
    val sleepReminderTime: String = "22:30",
    val pinEnabled: Boolean = false,
    val pinHash: String = "",
    val qazaFajr: Int = 0,
    val qazaZuhr: Int = 0,
    val qazaAsr: Int = 0,
    val qazaMaghrib: Int = 0,
    val qazaIsha: Int = 0
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val SETUP_COMPLETED = booleanPreferencesKey("setup_completed")
        val USER_NAME = stringPreferencesKey("user_name")
        val PROFILE_PHOTO_URI = stringPreferencesKey("profile_photo_uri")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val QURAN_TARGET = intPreferencesKey("quran_target")
        val WATER_TARGET = intPreferencesKey("water_target")
        val MONTHLY_BUDGET = doublePreferencesKey("monthly_budget")
        val PRAYER_CALC_METHOD = stringPreferencesKey("prayer_calc_method")
        val ASR_JURISTIC = stringPreferencesKey("asr_juristic")
        val CITY_NAME = stringPreferencesKey("city_name")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val TIMEZONE_ID = stringPreferencesKey("timezone_id")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset")
        val CUSTOM_FAJR = stringPreferencesKey("custom_fajr")
        val CUSTOM_ZUHR = stringPreferencesKey("custom_zuhr")
        val CUSTOM_ASR = stringPreferencesKey("custom_asr")
        val CUSTOM_MAGHRIB = stringPreferencesKey("custom_maghrib")
        val CUSTOM_ISHA = stringPreferencesKey("custom_isha")
        val PRAYER_REMINDERS = booleanPreferencesKey("prayer_reminders")
        val WATER_REMINDERS = booleanPreferencesKey("water_reminders")
        val WATER_INTERVAL = intPreferencesKey("water_interval")
        val WAKE_TIME = stringPreferencesKey("wake_time")
        val SLEEP_TIME = stringPreferencesKey("sleep_time")
        val PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val QAZA_FAJR = intPreferencesKey("qaza_fajr")
        val QAZA_ZUHR = intPreferencesKey("qaza_zuhr")
        val QAZA_ASR = intPreferencesKey("qaza_asr")
        val QAZA_MAGHRIB = intPreferencesKey("qaza_maghrib")
        val QAZA_ISHA = intPreferencesKey("qaza_isha")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { p ->
        UserPreferences(
            setupCompleted = p[Keys.SETUP_COMPLETED] ?: false,
            userName = p[Keys.USER_NAME] ?: "",
            profilePhotoUri = p[Keys.PROFILE_PHOTO_URI] ?: "",
            language = p[Keys.LANGUAGE] ?: "bn",
            themeMode = p[Keys.THEME_MODE] ?: "SYSTEM",
            dailyQuranTargetPages = p[Keys.QURAN_TARGET] ?: 5,
            dailyWaterTargetGlasses = p[Keys.WATER_TARGET] ?: 8,
            monthlyBudget = p[Keys.MONTHLY_BUDGET] ?: 0.0,
            prayerCalcMethod = p[Keys.PRAYER_CALC_METHOD] ?: "KARACHI",
            asrJuristic = p[Keys.ASR_JURISTIC] ?: "HANAFI",
            cityName = p[Keys.CITY_NAME] ?: "ঢাকা",
            latitude = p[Keys.LATITUDE] ?: 23.8103,
            longitude = p[Keys.LONGITUDE] ?: 90.4125,
            timezoneId = p[Keys.TIMEZONE_ID] ?: "Asia/Dhaka",
            hijriOffset = p[Keys.HIJRI_OFFSET] ?: 0,
            customFajr = p[Keys.CUSTOM_FAJR] ?: "05:00",
            customZuhr = p[Keys.CUSTOM_ZUHR] ?: "12:15",
            customAsr = p[Keys.CUSTOM_ASR] ?: "15:45",
            customMaghrib = p[Keys.CUSTOM_MAGHRIB] ?: "17:20",
            customIsha = p[Keys.CUSTOM_ISHA] ?: "18:45",
            prayerRemindersEnabled = p[Keys.PRAYER_REMINDERS] ?: true,
            waterRemindersEnabled = p[Keys.WATER_REMINDERS] ?: false,
            waterReminderIntervalHours = p[Keys.WATER_INTERVAL] ?: 2,
            wakeReminderTime = p[Keys.WAKE_TIME] ?: "04:30",
            sleepReminderTime = p[Keys.SLEEP_TIME] ?: "22:30",
            pinEnabled = p[Keys.PIN_ENABLED] ?: false,
            pinHash = p[Keys.PIN_HASH] ?: "",
            qazaFajr = p[Keys.QAZA_FAJR] ?: 0,
            qazaZuhr = p[Keys.QAZA_ZUHR] ?: 0,
            qazaAsr = p[Keys.QAZA_ASR] ?: 0,
            qazaMaghrib = p[Keys.QAZA_MAGHRIB] ?: 0,
            qazaIsha = p[Keys.QAZA_ISHA] ?: 0
        )
    }

    suspend fun getSnapshot(): UserPreferences = preferencesFlow.first()

    suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        context.dataStore.edit { prefs ->
            val current = UserPreferences(
                setupCompleted = prefs[Keys.SETUP_COMPLETED] ?: false,
                userName = prefs[Keys.USER_NAME] ?: "",
                profilePhotoUri = prefs[Keys.PROFILE_PHOTO_URI] ?: "",
                language = prefs[Keys.LANGUAGE] ?: "bn",
                themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
                dailyQuranTargetPages = prefs[Keys.QURAN_TARGET] ?: 5,
                dailyWaterTargetGlasses = prefs[Keys.WATER_TARGET] ?: 8,
                monthlyBudget = prefs[Keys.MONTHLY_BUDGET] ?: 0.0,
                prayerCalcMethod = prefs[Keys.PRAYER_CALC_METHOD] ?: "KARACHI",
                asrJuristic = prefs[Keys.ASR_JURISTIC] ?: "HANAFI",
                cityName = prefs[Keys.CITY_NAME] ?: "ঢাকা",
                latitude = prefs[Keys.LATITUDE] ?: 23.8103,
                longitude = prefs[Keys.LONGITUDE] ?: 90.4125,
                timezoneId = prefs[Keys.TIMEZONE_ID] ?: "Asia/Dhaka",
                hijriOffset = prefs[Keys.HIJRI_OFFSET] ?: 0,
                customFajr = prefs[Keys.CUSTOM_FAJR] ?: "05:00",
                customZuhr = prefs[Keys.CUSTOM_ZUHR] ?: "12:15",
                customAsr = prefs[Keys.CUSTOM_ASR] ?: "15:45",
                customMaghrib = prefs[Keys.CUSTOM_MAGHRIB] ?: "17:20",
                customIsha = prefs[Keys.CUSTOM_ISHA] ?: "18:45",
                prayerRemindersEnabled = prefs[Keys.PRAYER_REMINDERS] ?: true,
                waterRemindersEnabled = prefs[Keys.WATER_REMINDERS] ?: false,
                waterReminderIntervalHours = prefs[Keys.WATER_INTERVAL] ?: 2,
                wakeReminderTime = prefs[Keys.WAKE_TIME] ?: "04:30",
                sleepReminderTime = prefs[Keys.SLEEP_TIME] ?: "22:30",
                pinEnabled = prefs[Keys.PIN_ENABLED] ?: false,
                pinHash = prefs[Keys.PIN_HASH] ?: "",
                qazaFajr = prefs[Keys.QAZA_FAJR] ?: 0,
                qazaZuhr = prefs[Keys.QAZA_ZUHR] ?: 0,
                qazaAsr = prefs[Keys.QAZA_ASR] ?: 0,
                qazaMaghrib = prefs[Keys.QAZA_MAGHRIB] ?: 0,
                qazaIsha = prefs[Keys.QAZA_ISHA] ?: 0
            )
            val next = transform(current)
            prefs[Keys.SETUP_COMPLETED] = next.setupCompleted
            prefs[Keys.USER_NAME] = next.userName
            prefs[Keys.PROFILE_PHOTO_URI] = next.profilePhotoUri
            prefs[Keys.LANGUAGE] = next.language
            prefs[Keys.THEME_MODE] = next.themeMode
            prefs[Keys.QURAN_TARGET] = next.dailyQuranTargetPages
            prefs[Keys.WATER_TARGET] = next.dailyWaterTargetGlasses
            prefs[Keys.MONTHLY_BUDGET] = next.monthlyBudget
            prefs[Keys.PRAYER_CALC_METHOD] = next.prayerCalcMethod
            prefs[Keys.ASR_JURISTIC] = next.asrJuristic
            prefs[Keys.CITY_NAME] = next.cityName
            prefs[Keys.LATITUDE] = next.latitude
            prefs[Keys.LONGITUDE] = next.longitude
            prefs[Keys.TIMEZONE_ID] = next.timezoneId
            prefs[Keys.HIJRI_OFFSET] = next.hijriOffset
            prefs[Keys.CUSTOM_FAJR] = next.customFajr
            prefs[Keys.CUSTOM_ZUHR] = next.customZuhr
            prefs[Keys.CUSTOM_ASR] = next.customAsr
            prefs[Keys.CUSTOM_MAGHRIB] = next.customMaghrib
            prefs[Keys.CUSTOM_ISHA] = next.customIsha
            prefs[Keys.PRAYER_REMINDERS] = next.prayerRemindersEnabled
            prefs[Keys.WATER_REMINDERS] = next.waterRemindersEnabled
            prefs[Keys.WATER_INTERVAL] = next.waterReminderIntervalHours
            prefs[Keys.WAKE_TIME] = next.wakeReminderTime
            prefs[Keys.SLEEP_TIME] = next.sleepReminderTime
            prefs[Keys.PIN_ENABLED] = next.pinEnabled
            prefs[Keys.PIN_HASH] = next.pinHash
            prefs[Keys.QAZA_FAJR] = next.qazaFajr
            prefs[Keys.QAZA_ZUHR] = next.qazaZuhr
            prefs[Keys.QAZA_ASR] = next.qazaAsr
            prefs[Keys.QAZA_MAGHRIB] = next.qazaMaghrib
            prefs[Keys.QAZA_ISHA] = next.qazaIsha
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
