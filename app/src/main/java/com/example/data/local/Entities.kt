package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_tasks")
data class RoutineTaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val startTime: String = "", // "HH:mm" or empty
    val endTime: String = "",   // "HH:mm" or empty
    val category: String = "PERSONAL", // IBADAT, PERSONAL, FAMILY, WORK_MADRASA, EDUCATION, HEALTH, FINANCE
    val subTag: String = "",
    val priority: Int = 1, // 1=Normal, 2=Important, 3=Urgent
    val recurrence: String = "ONCE", // ONCE, DAILY, WEEKLY, MONTHLY, YEARLY
    val startDate: String, // "yyyy-MM-dd"
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 0,
    val estimatedMinutes: Int = 0,
    val note: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_completions", primaryKeys = ["taskId", "date"])
data class TaskCompletionEntity(
    val taskId: String,
    val date: String,
    val isCompleted: Boolean = false,
    val completedAt: Long = 0L,
    val dailyNote: String = ""
)

@Entity(tableName = "task_date_exceptions", primaryKeys = ["taskId", "date"])
data class TaskDateExceptionEntity(
    val taskId: String,
    val date: String,
    val type: String // "SKIP" (holiday/override) or "EXTRA" (carried forward)
)

@Entity(tableName = "special_days")
data class SpecialDayEntity(
    @PrimaryKey val date: String,
    val dayType: String = "HOLIDAY", // NORMAL, HOLIDAY, JUMUAH, RAMADAN, EID, SPECIAL
    val title: String = "",
    val note: String = ""
)

@Entity(tableName = "amal_definitions")
data class AmalDefinitionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String = "ZIKR", // NAMAZ, QURAN, ZIKR, SUNNAH, CHARITY, ILM
    val dailyTarget: Int = 1,
    val unit: String = "বার",
    val note: String = "",
    val reminderTime: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(tableName = "amal_logs", primaryKeys = ["amalId", "date"])
data class AmalLogEntity(
    val amalId: String,
    val date: String,
    val count: Int = 0,
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_prayer_logs")
data class DailyPrayerLogEntity(
    @PrimaryKey val date: String,
    val fajrStatus: Int = 0, // 0=Pending, 1=Alone, 2=Jamaat
    val zuhrStatus: Int = 0,
    val asrStatus: Int = 0,
    val maghribStatus: Int = 0,
    val ishaStatus: Int = 0,
    val tahajjudDone: Boolean = false,
    val ishraqDone: Boolean = false,
    val chashtDone: Boolean = false,
    val prepMiswak: Boolean = false,
    val prepWudu: Boolean = false,
    val prepCleanDress: Boolean = false,
    val prepMasjid: Boolean = false
)

@Entity(tableName = "quran_progress")
data class QuranProgressEntity(
    @PrimaryKey val date: String,
    val pagesRead: Int = 0,
    val ayahsRead: Int = 0,
    val hifzLines: Int = 0,
    val murajaPages: Int = 0,
    val surahNote: String = ""
)

@Entity(tableName = "health_daily")
data class HealthDailyEntity(
    @PrimaryKey val date: String,
    val waterGlasses: Int = 0,
    val sleepHours: Float = 0f,
    val wakeUpTime: String = "",
    val sleepTime: String = "",
    val exerciseMinutes: Int = 0,
    val walkMinutes: Int = 0,
    val bathDone: Boolean = false,
    val brushDone: Boolean = false,
    val cleanClothesDone: Boolean = false,
    val medicineNote: String = ""
)

@Entity(tableName = "finance_transactions")
data class FinanceTransactionEntity(
    @PrimaryKey val id: String,
    val date: String,
    val type: String, // INCOME, EXPENSE, SADAQAH, SAVINGS, DUE_RECEIVABLE, DUE_PAYABLE
    val category: String,
    val amount: Double,
    val note: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "diary_notes")
data class DiaryNoteEntity(
    @PrimaryKey val id: String,
    val date: String,
    val type: String, // DIARY, MUHASABAH, PLAN, THOUGHT, MEETING, DECISION, STUDY_NOTE
    val title: String,
    val encryptedBody: String,
    val isLocked: Boolean = false,
    val moodOrTag: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "yearly_goals")
data class YearlyGoalEntity(
    @PrimaryKey val id: String,
    val year: Int,
    val month: Int = 0, // 0 = yearly, 1..12 = monthly
    val category: String = "QURAN_KHATAM",
    val title: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val unit: String = "",
    val note: String = ""
)

@Entity(tableName = "madrasa_professional_items")
data class MadrasaProfessionalItemEntity(
    @PrimaryKey val id: String,
    val section: String, // MADRASA_CLASS, STUDENT_MONITOR, MEETING, OFFICE_DECISION, STUDY_SKILL
    val title: String,
    val subtitleOrPerson: String = "",
    val scheduleDayOrTime: String = "",
    val statusOrProgress: Int = 0,
    val targetProgress: Int = 100,
    val notes: String = "",
    val date: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_reminders")
data class CustomReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String, // WATER, MEDICINE, SLEEP, WAKE, IMPORTANT_DATE, CUSTOM
    val time: String, // HH:mm
    val date: String = "",
    val intervalHours: Int = 0,
    val isEnabled: Boolean = true
)
