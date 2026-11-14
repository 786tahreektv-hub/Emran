package com.example.ui.viewmodel

import com.example.core.DailyPrayerSchedule
import com.example.core.NextPrayerCountdown
import com.example.core.TripleDateInfo
import com.example.data.local.*
import com.example.data.prefs.UserPreferences

data class ResolvedTaskItem(
    val task: RoutineTaskEntity,
    val isCompleted: Boolean,
    val isCarriedExtra: Boolean,
    val isSkippedToday: Boolean
)

data class DayProgressSummary(
    val date: String,
    val tasksTotal: Int,
    val tasksCompleted: Int,
    val amalsTotal: Int,
    val amalsCompleted: Int,
    val prayersCompleted: Int,
    val quranPages: Int,
    val waterGlasses: Int,
    val overallPercent: Int
)

data class DecryptedDiaryNote(
    val entity: DiaryNoteEntity,
    val plainBody: String
)

data class CategoryBreakdown(
    val category: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val totalMinutes: Int
)

data class MainUiState(
    val isLoading: Boolean = true,
    val prefs: UserPreferences = UserPreferences(),
    val isUnlocked: Boolean = true,
    val showSplash: Boolean = true,
    val todayIso: String = "",
    val selectedDate: String = "",
    val activeMonthYear: Pair<Int, Int> = 2026 to 11, // year to month (1..12)
    val activeYear: Int = 2026,
    val currentTimeDisplay: String = "",
    val selectedDateTriple: TripleDateInfo? = null,
    val todayTriple: TripleDateInfo? = null,
    val todayPrayerSchedule: DailyPrayerSchedule? = null,
    val nextPrayerCountdown: NextPrayerCountdown? = null,

    val resolvedTasksForSelectedDate: List<ResolvedTaskItem> = emptyList(),
    val skippedTasksForSelectedDate: List<RoutineTaskEntity> = emptyList(),
    val specialDayForSelectedDate: SpecialDayEntity? = null,
    val tomorrowImportantTasks: List<RoutineTaskEntity> = emptyList(),

    val todaySummary: DayProgressSummary = DayProgressSummary("", 0, 0, 0, 0, 0, 0, 0, 0),
    val selectedSummary: DayProgressSummary = DayProgressSummary("", 0, 0, 0, 0, 0, 0, 0, 0),
    val last7DaysSummaries: List<DayProgressSummary> = emptyList(),
    val monthSummariesMap: Map<String, DayProgressSummary> = emptyMap(),
    val yearSummariesMap: Map<String, DayProgressSummary> = emptyMap(),
    val monthlyAvgPercent: Int = 0,
    val yearlyAvgPercent: Int = 0,
    val amalStreakDays: Int = 0,
    val categoryBreakdownToday: List<CategoryBreakdown> = emptyList(),

    val amals: List<AmalDefinitionEntity> = emptyList(),
    val amalCountsSelectedDate: Map<String, AmalLogEntity> = emptyMap(),
    val prayerLogSelectedDate: DailyPrayerLogEntity = DailyPrayerLogEntity(""),
    val quranSelectedDate: QuranProgressEntity = QuranProgressEntity(""),
    val healthSelectedDate: HealthDailyEntity = HealthDailyEntity(""),
    val healthToday: HealthDailyEntity = HealthDailyEntity(""),

    val financeList: List<FinanceTransactionEntity> = emptyList(),
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthSadaqah: Double = 0.0,
    val monthSavings: Double = 0.0,
    val totalReceivableDue: Double = 0.0,
    val totalPayableDue: Double = 0.0,

    val diaryNotes: List<DecryptedDiaryNote> = emptyList(),
    val goals: List<YearlyGoalEntity> = emptyList(),
    val madrasaWorkItems: List<MadrasaProfessionalItemEntity> = emptyList(),
    val reminders: List<CustomReminderEntity> = emptyList(),
    val allCompletions: List<TaskCompletionEntity> = emptyList(),
    val allPrayerLogs: List<DailyPrayerLogEntity> = emptyList(),
    val allQuranProgress: List<QuranProgressEntity> = emptyList(),
    val allSpecialDays: Map<String, SpecialDayEntity> = emptyMap(),

    val toastMessage: String? = null
)
