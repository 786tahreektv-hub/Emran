package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AmarJibonApp
import com.example.core.AppStrings
import com.example.core.CalendarEngine
import com.example.core.CryptoManager
import com.example.core.PrayerTimeCalculator
import com.example.data.local.*
import com.example.data.prefs.UserPreferences
import com.example.notifications.NotificationHelper
import com.example.notifications.ReminderScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

private data class CoreFlowBundle(
    val prefs: UserPreferences,
    val tasks: List<RoutineTaskEntity>,
    val completions: List<TaskCompletionEntity>,
    val exceptions: List<TaskDateExceptionEntity>,
    val specialDays: List<SpecialDayEntity>
)

private data class AmalFlowBundle(
    val amals: List<AmalDefinitionEntity>,
    val amalLogs: List<AmalLogEntity>,
    val prayerLogs: List<DailyPrayerLogEntity>,
    val quranLogs: List<QuranProgressEntity>
)

private data class LifeFlowBundle(
    val health: List<HealthDailyEntity>,
    val finance: List<FinanceTransactionEntity>,
    val diary: List<DiaryNoteEntity>,
    val goals: List<YearlyGoalEntity>,
    val madrasaWork: List<MadrasaProfessionalItemEntity>,
    val reminders: List<CustomReminderEntity>
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as AmarJibonApp
    private val repo = app.repository

    private val selectedDateFlow = MutableStateFlow(CalendarEngine.todayIso())
    private val activeMonthYearFlow = MutableStateFlow(parseYearMonth(CalendarEngine.todayIso()))
    private val activeYearFlow = MutableStateFlow(CalendarEngine.todayIso().substring(0, 4).toIntOrNull() ?: 2026)
    private val isUnlockedFlow = MutableStateFlow(false)
    private val pinCheckInitialized = MutableStateFlow(false)
    private val showSplashFlow = MutableStateFlow(true)
    private val tickFlow = MutableStateFlow(System.currentTimeMillis())
    private val toastFlow = MutableStateFlow<String?>(null)

    private val coreBundleFlow = combine(
        repo.preferencesFlow,
        repo.allTasksFlow,
        repo.allCompletionsFlow,
        repo.allExceptionsFlow,
        repo.allSpecialDaysFlow
    ) { prefs, tasks, completions, exceptions, specialDays ->
        CoreFlowBundle(prefs, tasks, completions, exceptions, specialDays)
    }

    private val amalBundleFlow = combine(
        repo.allAmalsFlow,
        repo.allAmalLogsFlow,
        repo.allPrayerLogsFlow,
        repo.allQuranProgressFlow
    ) { amals, amalLogs, prayerLogs, quranLogs ->
        AmalFlowBundle(amals, amalLogs, prayerLogs, quranLogs)
    }

    private val lifeBundleFlow = combine(
        repo.allHealthFlow,
        repo.allFinanceFlow,
        repo.allDiaryFlow,
        repo.allGoalsFlow,
        repo.allMadrasaWorkFlow
    ) { health, finance, diary, goals, madrasa ->
        LifeFlowBundle(health, finance, diary, goals, madrasa, emptyList())
    }.combine(repo.allRemindersFlow) { bundle, reminders ->
        bundle.copy(reminders = reminders)
    }

    private val navigationParamsFlow = combine(
        selectedDateFlow,
        activeMonthYearFlow,
        activeYearFlow,
        isUnlockedFlow,
        tickFlow
    ) { selDate, monthYear, year, unlocked, tick ->
        NavSnapshot(selDate, monthYear, year, unlocked, tick)
    }

    private data class NavSnapshot(
        val selectedDate: String,
        val monthYear: Pair<Int, Int>,
        val year: Int,
        val unlocked: Boolean,
        val tick: Long
    )

    val uiState: StateFlow<MainUiState> = combine(
        coreBundleFlow,
        amalBundleFlow,
        lifeBundleFlow,
        navigationParamsFlow,
        combine(showSplashFlow, toastFlow) { splash, toast -> splash to toast }
    ) { core, amal, life, nav, (splash, toast) ->
        buildUiState(core, amal, life, nav, splash, toast)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    init {
        viewModelScope.launch {
            delay(1100)
            showSplashFlow.value = false
        }
        viewModelScope.launch {
            while (true) {
                delay(1000)
                tickFlow.value = System.currentTimeMillis()
            }
        }
        viewModelScope.launch {
            repo.preferencesFlow.take(1).collect { prefs ->
                val tzToday = CalendarEngine.todayIso(prefs.timezoneId)
                selectedDateFlow.value = tzToday
                activeMonthYearFlow.value = parseYearMonth(tzToday)
                activeYearFlow.value = tzToday.substring(0, 4).toIntOrNull() ?: 2026
                isUnlockedFlow.value = !prefs.pinEnabled || prefs.pinHash.isBlank()
                pinCheckInitialized.value = true
            }
        }
    }

    private fun buildUiState(
        core: CoreFlowBundle,
        amal: AmalFlowBundle,
        life: LifeFlowBundle,
        nav: NavSnapshot,
        splash: Boolean,
        toast: String?
    ): MainUiState {
        val prefs = core.prefs
        val tz = prefs.timezoneId
        val today = CalendarEngine.todayIso(tz)
        val selDate = nav.selectedDate.ifBlank { today }

        val unlocked = if (!prefs.pinEnabled || prefs.pinHash.isBlank()) true else nav.unlocked

        val todayTriple = CalendarEngine.getTripleDate(today, prefs.hijriOffset, prefs.language)
        val selTriple = CalendarEngine.getTripleDate(selDate, prefs.hijriOffset, prefs.language)

        val todayPrayerSchedule = PrayerTimeCalculator.calculate(
            isoDate = today,
            lat = prefs.latitude,
            lon = prefs.longitude,
            tzId = tz,
            method = prefs.prayerCalcMethod,
            asrJuristic = prefs.asrJuristic,
            customFajr = prefs.customFajr,
            customZuhr = prefs.customZuhr,
            customAsr = prefs.customAsr,
            customMaghrib = prefs.customMaghrib,
            customIsha = prefs.customIsha
        )
        val nextPrayer = PrayerTimeCalculator.getNextPrayerCountdown(todayPrayerSchedule, tz, prefs.language)

        val completionMap = core.completions.associateBy { it.taskId to it.date }
        val exceptionMap = core.exceptions.associateBy { it.taskId to it.date }
        val amalLogMap = amal.amalLogs.associateBy { it.amalId to it.date }
        val prayerMap = amal.prayerLogs.associateBy { it.date }
        val quranMap = amal.quranLogs.associateBy { it.date }
        val healthMap = life.health.associateBy { it.date }
        val specialMap = core.specialDays.associateBy { it.date }

        fun tasksForDate(date: String): Pair<List<ResolvedTaskItem>, List<RoutineTaskEntity>> {
            val active = mutableListOf<ResolvedTaskItem>()
            val skipped = mutableListOf<RoutineTaskEntity>()
            val targetWd = CalendarEngine.dayOfWeek(date)
            for (task in core.tasks) {
                val ex = exceptionMap[task.id to date]?.type
                val matchesBase = when (task.recurrence) {
                    "ONCE" -> task.startDate == date
                    "DAILY" -> date >= task.startDate
                    "WEEKLY" -> date >= task.startDate && CalendarEngine.dayOfWeek(task.startDate) == targetWd
                    "MONTHLY" -> date >= task.startDate && task.startDate.takeLast(2) == date.takeLast(2)
                    "YEARLY" -> date >= task.startDate && task.startDate.takeLast(5) == date.takeLast(5)
                    else -> task.startDate == date
                }
                val isExtra = ex == "EXTRA"
                val isSkip = ex == "SKIP"
                if (isSkip && matchesBase) {
                    skipped.add(task)
                } else if (matchesBase || isExtra) {
                    val comp = completionMap[task.id to date]?.isCompleted ?: false
                    active.add(
                        ResolvedTaskItem(
                            task = task,
                            isCompleted = comp,
                            isCarriedExtra = isExtra && !matchesBase,
                            isSkippedToday = false
                        )
                    )
                }
            }
            active.sortWith(compareBy<ResolvedTaskItem> { it.task.startTime.ifBlank { "99:99" } }.thenByDescending { it.task.priority })
            return active to skipped
        }

        fun summarizeDay(date: String): DayProgressSummary {
            val (dayTasks, _) = tasksForDate(date)
            val tasksTotal = dayTasks.size
            val tasksDone = dayTasks.count { it.isCompleted }
            val amalsTotal = amal.amals.size
            val amalsDone = amal.amals.count { a ->
                (amalLogMap[a.id to date]?.count ?: 0) >= a.dailyTarget
            }
            val pLog = prayerMap[date]
            val prayersDone = if (pLog == null) 0 else listOf(
                pLog.fajrStatus, pLog.zuhrStatus, pLog.asrStatus, pLog.maghribStatus, pLog.ishaStatus
            ).count { it > 0 }
            val qPages = quranMap[date]?.pagesRead ?: 0
            val water = healthMap[date]?.waterGlasses ?: 0

            val totalUnits = tasksTotal + amalsTotal + 5
            val doneUnits = tasksDone + amalsDone + prayersDone
            val pct = if (totalUnits == 0) 0 else ((doneUnits * 100f) / totalUnits).toInt().coerceIn(0, 100)
            return DayProgressSummary(
                date = date,
                tasksTotal = tasksTotal,
                tasksCompleted = tasksDone,
                amalsTotal = amalsTotal,
                amalsCompleted = amalsDone,
                prayersCompleted = prayersDone,
                quranPages = qPages,
                waterGlasses = water,
                overallPercent = pct
            )
        }

        val (selTasks, selSkipped) = tasksForDate(selDate)
        val tomorrow = CalendarEngine.addDays(today, 1)
        val (tomorrowTasks, _) = tasksForDate(tomorrow)
        val tomorrowImportant = tomorrowTasks.map { it.task }.filter { it.priority >= 2 }

        val todaySummary = summarizeDay(today)
        val selectedSummary = summarizeDay(selDate)

        val last7 = (-6..0).map { offset -> summarizeDay(CalendarEngine.addDays(today, offset)) }

        val (mYear, mMonth) = nav.monthYear
        val monthDates = CalendarEngine.getMonthDates(mYear, mMonth)
        val monthSummaries = monthDates.associateWith { summarizeDay(it) }
        val pastOrTodayMonth = monthDates.filter { it <= today }.mapNotNull { monthSummaries[it] }
        val monthlyAvg = if (pastOrTodayMonth.isEmpty()) 0 else pastOrTodayMonth.map { it.overallPercent }.average().toInt()

        val yearDates = CalendarEngine.getYearDates(nav.year)
        val yearSummaries = yearDates.associateWith { summarizeDay(it) }
        val pastOrTodayYear = yearDates.filter { it <= today }.mapNotNull { yearSummaries[it] }
        val yearlyAvg = if (pastOrTodayYear.isEmpty()) 0 else pastOrTodayYear.map { it.overallPercent }.average().toInt()

        // Streak: consecutive days ending today (or yesterday if today not started) with overallPercent >= 50
        var streak = 0
        var checkDate = today
        while (true) {
            val s = summarizeDay(checkDate)
            if (s.overallPercent >= 50 && (s.tasksTotal + s.amalsTotal) > 0) {
                streak++
                checkDate = CalendarEngine.addDays(checkDate, -1)
                if (streak >= 365) break
            } else if (checkDate == today) {
                checkDate = CalendarEngine.addDays(checkDate, -1)
            } else {
                break
            }
        }

        val catBreakdown = tasksForDate(today).first
            .groupBy { it.task.category }
            .map { (cat, list) ->
                CategoryBreakdown(
                    category = cat,
                    totalTasks = list.size,
                    completedTasks = list.count { it.isCompleted },
                    totalMinutes = list.sumOf { it.task.estimatedMinutes }
                )
            }

        val amalLogsSel = amal.amals.associate { a ->
            a.id to (amalLogMap[a.id to selDate] ?: AmalLogEntity(a.id, selDate, 0))
        }

        val currentMonthPrefix = today.substring(0, 7)
        val todayFinance = life.finance.filter { it.date == today }
        val monthFinance = life.finance.filter { it.date.startsWith(currentMonthPrefix) }
        val todayIncome = todayFinance.filter { it.type == "INCOME" }.sumOf { it.amount }
        val todayExpense = todayFinance.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val monthIncome = monthFinance.filter { it.type == "INCOME" }.sumOf { it.amount }
        val monthExpense = monthFinance.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val monthSadaqah = monthFinance.filter { it.type == "SADAQAH" }.sumOf { it.amount }
        val monthSavings = monthFinance.filter { it.type == "SAVINGS" }.sumOf { it.amount }
        val receivableDue = life.finance.filter { it.type == "DUE_RECEIVABLE" && !it.isSettled }.sumOf { it.amount }
        val payableDue = life.finance.filter { it.type == "DUE_PAYABLE" && !it.isSettled }.sumOf { it.amount }

        val decryptedDiary = life.diary.map { d ->
            DecryptedDiaryNote(d, CryptoManager.decrypt(d.encryptedBody))
        }

        return MainUiState(
            isLoading = false,
            prefs = prefs,
            isUnlocked = unlocked,
            showSplash = splash,
            todayIso = today,
            selectedDate = selDate,
            activeMonthYear = nav.monthYear,
            activeYear = nav.year,
            currentTimeDisplay = CalendarEngine.currentTimeHms(tz, prefs.language),
            selectedDateTriple = selTriple,
            todayTriple = todayTriple,
            todayPrayerSchedule = todayPrayerSchedule,
            nextPrayerCountdown = nextPrayer,
            resolvedTasksForSelectedDate = selTasks,
            skippedTasksForSelectedDate = selSkipped,
            specialDayForSelectedDate = specialMap[selDate],
            tomorrowImportantTasks = tomorrowImportant,
            todaySummary = todaySummary,
            selectedSummary = selectedSummary,
            last7DaysSummaries = last7,
            monthSummariesMap = monthSummaries,
            yearSummariesMap = yearSummaries,
            monthlyAvgPercent = monthlyAvg,
            yearlyAvgPercent = yearlyAvg,
            amalStreakDays = streak,
            categoryBreakdownToday = catBreakdown,
            amals = amal.amals,
            amalCountsSelectedDate = amalLogsSel,
            prayerLogSelectedDate = prayerMap[selDate] ?: DailyPrayerLogEntity(selDate),
            quranSelectedDate = quranMap[selDate] ?: QuranProgressEntity(selDate),
            healthSelectedDate = healthMap[selDate] ?: HealthDailyEntity(selDate),
            healthToday = healthMap[today] ?: HealthDailyEntity(today),
            financeList = life.finance,
            todayIncome = todayIncome,
            todayExpense = todayExpense,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            monthSadaqah = monthSadaqah,
            monthSavings = monthSavings,
            totalReceivableDue = receivableDue,
            totalPayableDue = payableDue,
            diaryNotes = decryptedDiary,
            goals = life.goals,
            madrasaWorkItems = life.madrasaWork,
            reminders = life.reminders,
            allCompletions = core.completions,
            allPrayerLogs = amal.prayerLogs,
            allQuranProgress = amal.quranLogs,
            allSpecialDays = specialMap,
            toastMessage = toast
        )
    }

    fun selectDate(isoDate: String) {
        selectedDateFlow.value = isoDate
    }

    fun stepSelectedDate(deltaDays: Int) {
        selectedDateFlow.value = CalendarEngine.addDays(selectedDateFlow.value, deltaDays)
    }

    fun setMonthYear(year: Int, month: Int) {
        var y = year
        var m = month
        if (m < 1) { m = 12; y-- }
        if (m > 12) { m = 1; y++ }
        activeMonthYearFlow.value = y to m
    }

    fun setActiveYear(year: Int) {
        activeYearFlow.value = year
    }

    fun clearToast() {
        toastFlow.value = null
    }

    fun showToast(msg: String) {
        toastFlow.value = msg
    }

    fun verifyPin(pin: String): Boolean {
        val ok = CryptoManager.hashPin(pin) == uiState.value.prefs.pinHash
        if (ok) isUnlockedFlow.value = true
        return ok
    }

    fun completeSetup(
        userName: String,
        cityName: String,
        lat: Double,
        lon: Double,
        tzId: String,
        quranTarget: Int,
        waterTarget: Int,
        selectedTasks: Set<String>,
        selectedAmals: Set<String>
    ) {
        viewModelScope.launch {
            repo.completeInitialSetup(
                userName = userName,
                cityName = cityName,
                lat = lat,
                lon = lon,
                tzId = tzId,
                quranTarget = quranTarget,
                waterTarget = waterTarget,
                selectedTaskKeys = selectedTasks,
                selectedAmalKeys = selectedAmals,
                lang = uiState.value.prefs.language
            )
            ReminderScheduler.scheduleAllForToday(app, repo.prefsRepo.getSnapshot(), uiState.value.reminders)
            isUnlockedFlow.value = true
        }
    }

    // Routine Actions
    fun toggleTask(item: ResolvedTaskItem) {
        viewModelScope.launch {
            repo.toggleTaskCompletion(item.task.id, uiState.value.selectedDate, item.isCompleted)
        }
    }

    fun saveTask(
        id: String?,
        title: String,
        description: String,
        startTime: String,
        endTime: String,
        category: String,
        subTag: String,
        priority: Int,
        recurrence: String,
        startDate: String,
        reminderEnabled: Boolean,
        estimatedMinutes: Int,
        note: String
    ) {
        viewModelScope.launch {
            repo.upsertTask(
                RoutineTaskEntity(
                    id = id ?: UUID.randomUUID().toString(),
                    title = title.trim(),
                    description = description.trim(),
                    startTime = startTime,
                    endTime = endTime,
                    category = category,
                    subTag = subTag.trim(),
                    priority = priority,
                    recurrence = recurrence,
                    startDate = startDate,
                    reminderEnabled = reminderEnabled,
                    estimatedMinutes = estimatedMinutes,
                    note = note.trim()
                )
            )
            showToast(AppStrings.t(uiState.value.prefs.language, "কাজ সংরক্ষিত হয়েছে", "Task saved"))
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch { repo.deleteTask(id) }
    }

    fun skipTaskForSelectedDate(id: String) {
        viewModelScope.launch { repo.skipTaskForDate(id, uiState.value.selectedDate) }
    }

    fun restoreTaskForSelectedDate(id: String) {
        viewModelScope.launch { repo.restoreTaskForDate(id, uiState.value.selectedDate) }
    }

    fun carryTaskToTomorrow(task: RoutineTaskEntity) {
        viewModelScope.launch {
            repo.carryTaskToTomorrow(task, uiState.value.selectedDate)
            showToast(AppStrings.t(uiState.value.prefs.language, "কাজটি আগামীকাল নেওয়া হয়েছে", "Moved task to tomorrow"))
        }
    }

    fun carryAllUnfinishedToTomorrow() {
        viewModelScope.launch {
            val date = uiState.value.selectedDate
            val unfinished = uiState.value.resolvedTasksForSelectedDate.filter { !it.isCompleted }
            unfinished.forEach { repo.carryTaskToTomorrow(it.task, date) }
            showToast(
                AppStrings.t(
                    uiState.value.prefs.language,
                    "${AppStrings.digits(unfinished.size, "bn")}টি অসমাপ্ত কাজ আগামীকাল নেওয়া হয়েছে",
                    "Moved ${unfinished.size} unfinished tasks to tomorrow"
                )
            )
        }
    }

    fun saveSpecialDay(date: String, dayType: String, title: String, note: String) {
        viewModelScope.launch {
            if (dayType == "NORMAL" && title.isBlank()) {
                repo.deleteSpecialDay(date)
            } else {
                repo.upsertSpecialDay(SpecialDayEntity(date, dayType, title.trim(), note.trim()))
            }
            showToast(AppStrings.t(uiState.value.prefs.language, "বিশেষ দিনের সেটিং সংরক্ষিত", "Special day saved"))
        }
    }

    // Amal & Prayer & Quran Actions
    fun incrementAmal(amalId: String, delta: Int) {
        viewModelScope.launch {
            val date = uiState.value.selectedDate
            val existing = uiState.value.amalCountsSelectedDate[amalId]
            repo.incrementAmal(amalId, date, delta, existing?.count ?: 0, existing?.note ?: "")
        }
    }

    fun updateAmalDailyNote(amalId: String, note: String) {
        viewModelScope.launch {
            val date = uiState.value.selectedDate
            val existing = uiState.value.amalCountsSelectedDate[amalId]
            repo.updateAmalNote(amalId, date, existing?.count ?: 0, note)
        }
    }

    fun saveAmalDefinition(id: String?, name: String, category: String, target: Int, unit: String, note: String) {
        viewModelScope.launch {
            repo.upsertAmal(
                AmalDefinitionEntity(
                    id = id ?: UUID.randomUUID().toString(),
                    name = name.trim(),
                    category = category,
                    dailyTarget = target.coerceAtLeast(1),
                    unit = unit.trim(),
                    note = note.trim(),
                    sortOrder = uiState.value.amals.size
                )
            )
        }
    }

    fun deleteAmalDefinition(id: String) {
        viewModelScope.launch { repo.deleteAmal(id) }
    }

    fun cyclePrayerStatus(key: String) {
        viewModelScope.launch {
            val cur = uiState.value.prayerLogSelectedDate
            val date = uiState.value.selectedDate
            val next = when (key) {
                "fajr" -> cur.copy(date = date, fajrStatus = (cur.fajrStatus + 1) % 3)
                "zuhr" -> cur.copy(date = date, zuhrStatus = (cur.zuhrStatus + 1) % 3)
                "asr" -> cur.copy(date = date, asrStatus = (cur.asrStatus + 1) % 3)
                "maghrib" -> cur.copy(date = date, maghribStatus = (cur.maghribStatus + 1) % 3)
                "isha" -> cur.copy(date = date, ishaStatus = (cur.ishaStatus + 1) % 3)
                else -> cur
            }
            repo.upsertPrayerLog(next)
        }
    }

    fun updatePrayerExtras(update: (DailyPrayerLogEntity) -> DailyPrayerLogEntity) {
        viewModelScope.launch {
            val date = uiState.value.selectedDate
            repo.upsertPrayerLog(update(uiState.value.prayerLogSelectedDate.copy(date = date)))
        }
    }

    fun adjustQaza(key: String, delta: Int) {
        viewModelScope.launch {
            repo.prefsRepo.update { p ->
                when (key) {
                    "fajr" -> p.copy(qazaFajr = (p.qazaFajr + delta).coerceAtLeast(0))
                    "zuhr" -> p.copy(qazaZuhr = (p.qazaZuhr + delta).coerceAtLeast(0))
                    "asr" -> p.copy(qazaAsr = (p.qazaAsr + delta).coerceAtLeast(0))
                    "maghrib" -> p.copy(qazaMaghrib = (p.qazaMaghrib + delta).coerceAtLeast(0))
                    "isha" -> p.copy(qazaIsha = (p.qazaIsha + delta).coerceAtLeast(0))
                    else -> p
                }
            }
        }
    }

    fun updateQuranProgress(update: (QuranProgressEntity) -> QuranProgressEntity) {
        viewModelScope.launch {
            val date = uiState.value.selectedDate
            repo.upsertQuranProgress(update(uiState.value.quranSelectedDate.copy(date = date)))
        }
    }

    // Health Actions
    fun adjustWaterToday(delta: Int) {
        viewModelScope.launch {
            val today = uiState.value.todayIso
            val cur = uiState.value.healthToday.copy(date = today)
            repo.upsertHealthDaily(cur.copy(waterGlasses = (cur.waterGlasses + delta).coerceAtLeast(0)))
        }
    }

    fun updateHealthSelectedDate(update: (HealthDailyEntity) -> HealthDailyEntity) {
        viewModelScope.launch {
            val date = uiState.value.selectedDate
            repo.upsertHealthDaily(update(uiState.value.healthSelectedDate.copy(date = date)))
        }
    }

    // Finance Actions
    fun addFinance(date: String, type: String, category: String, amount: Double, note: String) {
        viewModelScope.launch {
            repo.upsertFinance(
                FinanceTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    date = date,
                    type = type,
                    category = category,
                    amount = amount,
                    note = note.trim()
                )
            )
            showToast(AppStrings.t(uiState.value.prefs.language, "হিসাব যোগ হয়েছে", "Transaction added"))
        }
    }

    fun toggleFinanceSettled(item: FinanceTransactionEntity) {
        viewModelScope.launch { repo.upsertFinance(item.copy(isSettled = !item.isSettled)) }
    }

    fun deleteFinance(id: String) {
        viewModelScope.launch { repo.deleteFinance(id) }
    }

    // Diary Actions
    fun saveDiary(id: String?, date: String, type: String, title: String, body: String, tag: String, locked: Boolean) {
        viewModelScope.launch {
            repo.saveDiaryNote(id, date, type, title.trim(), body, tag.trim(), locked)
            showToast(AppStrings.t(uiState.value.prefs.language, "নোট নিরাপদে সংরক্ষিত হয়েছে", "Note saved securely"))
        }
    }

    fun deleteDiary(id: String) {
        viewModelScope.launch { repo.deleteDiaryNote(id) }
    }

    // Goals Actions
    fun saveGoal(id: String?, year: Int, month: Int, category: String, title: String, target: Int, current: Int, unit: String, note: String) {
        viewModelScope.launch {
            repo.upsertGoal(
                YearlyGoalEntity(
                    id = id ?: UUID.randomUUID().toString(),
                    year = year,
                    month = month,
                    category = category,
                    title = title.trim(),
                    targetValue = target.coerceAtLeast(1),
                    currentValue = current.coerceAtLeast(0),
                    unit = unit.trim(),
                    note = note.trim()
                )
            )
        }
    }

    fun adjustGoalProgress(goal: YearlyGoalEntity, delta: Int) {
        viewModelScope.launch {
            repo.upsertGoal(goal.copy(currentValue = (goal.currentValue + delta).coerceAtLeast(0)))
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch { repo.deleteGoal(id) }
    }

    // Madrasa / Work / Education Actions
    fun saveMadrasaWorkItem(
        id: String?,
        section: String,
        title: String,
        subtitleOrPerson: String,
        scheduleDayOrTime: String,
        progress: Int,
        target: Int,
        notes: String,
        date: String,
        isCompleted: Boolean
    ) {
        viewModelScope.launch {
            repo.upsertMadrasaWork(
                MadrasaProfessionalItemEntity(
                    id = id ?: UUID.randomUUID().toString(),
                    section = section,
                    title = title.trim(),
                    subtitleOrPerson = subtitleOrPerson.trim(),
                    scheduleDayOrTime = scheduleDayOrTime.trim(),
                    statusOrProgress = progress,
                    targetProgress = target.coerceAtLeast(1),
                    notes = notes.trim(),
                    date = date,
                    isCompleted = isCompleted
                )
            )
        }
    }

    fun toggleMadrasaWorkCompleted(item: MadrasaProfessionalItemEntity) {
        viewModelScope.launch {
            repo.upsertMadrasaWork(item.copy(isCompleted = !item.isCompleted))
        }
    }

    fun incrementMadrasaWorkProgress(item: MadrasaProfessionalItemEntity, delta: Int) {
        viewModelScope.launch {
            val next = (item.statusOrProgress + delta).coerceIn(0, item.targetProgress)
            repo.upsertMadrasaWork(item.copy(statusOrProgress = next, isCompleted = next >= item.targetProgress))
        }
    }

    fun deleteMadrasaWork(id: String) {
        viewModelScope.launch { repo.deleteMadrasaWork(id) }
    }

    // Reminders
    fun saveReminder(id: String?, title: String, type: String, time: String, date: String, intervalHours: Int, enabled: Boolean) {
        viewModelScope.launch {
            repo.upsertReminder(
                CustomReminderEntity(
                    id = id ?: UUID.randomUUID().toString(),
                    title = title.trim(),
                    type = type,
                    time = time,
                    date = date,
                    intervalHours = intervalHours,
                    isEnabled = enabled
                )
            )
            ReminderScheduler.scheduleAllForToday(app, uiState.value.prefs, uiState.value.reminders)
        }
    }

    fun deleteReminder(id: String) {
        viewModelScope.launch { repo.deleteReminder(id) }
    }

    fun sendTestNotification() {
        val lang = uiState.value.prefs.language
        NotificationHelper.showNotification(
            app,
            9999,
            NotificationHelper.CHANNEL_PRAYER,
            AppStrings.t(lang, "আমার জীবন — রিমাইন্ডার সক্রিয়", "My Life — Reminder Active"),
            AppStrings.t(lang, "নোটিফিকেশন সঠিকভাবে কাজ করছে আলহামদুলিল্লাহ!", "Local notifications are working properly!")
        )
    }

    // Settings
    fun updatePreferences(transform: (UserPreferences) -> UserPreferences) {
        viewModelScope.launch {
            repo.prefsRepo.update(transform)
            val updated = repo.prefsRepo.getSnapshot()
            ReminderScheduler.scheduleAllForToday(app, updated, uiState.value.reminders)
        }
    }

    fun setPin(pin: String?) {
        viewModelScope.launch {
            if (pin.isNullOrBlank()) {
                repo.prefsRepo.update { it.copy(pinEnabled = false, pinHash = "") }
                showToast(AppStrings.t(uiState.value.prefs.language, "অ্যাপ লক বন্ধ করা হয়েছে", "App Lock disabled"))
            } else {
                repo.prefsRepo.update { it.copy(pinEnabled = true, pinHash = CryptoManager.hashPin(pin)) }
                showToast(AppStrings.t(uiState.value.prefs.language, "PIN লক সক্রিয় করা হয়েছে", "PIN Lock enabled"))
            }
        }
    }

    // Export & Backup
    fun exportJson(uri: Uri) {
        viewModelScope.launch {
            val res = repo.exportJsonBackup(uri)
            val lang = uiState.value.prefs.language
            showToast(
                if (res.isSuccess) AppStrings.t(lang, "JSON ব্যাকআপ সংরক্ষিত হয়েছে", "JSON backup saved")
                else AppStrings.t(lang, "ব্যাকআপ ব্যর্থ হয়েছে", "Backup failed")
            )
        }
    }

    fun importJson(uri: Uri) {
        viewModelScope.launch {
            val res = repo.importJsonBackup(uri)
            val lang = uiState.value.prefs.language
            showToast(
                if (res.isSuccess) AppStrings.t(lang, "ডেটা রিস্টোর সম্পন্ন হয়েছে", "Data restored successfully")
                else AppStrings.t(lang, "ফাইলটি সঠিক ব্যাকআপ নয়", "Invalid backup file")
            )
        }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            val res = repo.exportCsvReport(uri)
            val lang = uiState.value.prefs.language
            showToast(
                if (res.isSuccess) AppStrings.t(lang, "CSV রিপোর্ট সংরক্ষিত হয়েছে", "CSV report saved")
                else AppStrings.t(lang, "CSV এক্সপোর্ট ব্যর্থ হয়েছে", "CSV export failed")
            )
        }
    }

    fun exportPdf(uri: Uri) {
        viewModelScope.launch {
            val s = uiState.value
            val lang = s.prefs.language
            val title = AppStrings.t(lang, "মাসিক ও দৈনিক রিপোর্ট (${s.todayIso})", "Life & Amal Report (${s.todayIso})")
            val lines = listOf(
                "ব্যবহারকারী: ${s.prefs.userName.ifBlank { "—" }} | শহর: ${s.prefs.cityName}",
                "আজকের তারিখ: ${s.todayTriple?.gregorianDisplay} | ${s.todayTriple?.banglaDisplay} | ${s.todayTriple?.hijriDisplay}",
                "আজকের সামগ্রিক অগ্রগতি: ${s.todaySummary.overallPercent}% (কাজ ${s.todaySummary.tasksCompleted}/${s.todaySummary.tasksTotal}, নামাজ ${s.todaySummary.prayersCompleted}/5, আমল ${s.todaySummary.amalsCompleted}/${s.todaySummary.amalsTotal})",
                "কুরআন তিলাওয়াত (আজ): ${s.quranSelectedDate.pagesRead}/${s.prefs.dailyQuranTargetPages} পৃষ্ঠা, ${s.quranSelectedDate.ayahsRead} আয়াত",
                "মাসিক গড় অগ্রগতি: ${s.monthlyAvgPercent}% | বার্ষিক গড় অগ্রগতি: ${s.yearlyAvgPercent}% | ধারাবাহিকতা: ${s.amalStreakDays} দিন",
                "আর্থিক হিসাব (চলতি মাস): আয় ৳${s.monthIncome.toInt()}, ব্যয় ৳${s.monthExpense.toInt()}, সদকা ৳${s.monthSadaqah.toInt()}, সঞ্চয় ৳${s.monthSavings.toInt()}",
                "বকেয়া হিসাব: পাওনা ৳${s.totalReceivableDue.toInt()} | দেনা ৳${s.totalPayableDue.toInt()}",
                "আজকের কাজসমূহ: " + s.resolvedTasksForSelectedDate.joinToString("; ") {
                    "${it.task.title} [${if (it.isCompleted) "সম্পন্ন" else "বাকি"}]"
                }.ifBlank { "কোনো কাজ নেই" },
                "বার্ষিক লক্ষ্যসমূহ: " + s.goals.joinToString("; ") {
                    "${it.title}: ${it.currentValue}/${it.targetValue} ${it.unit}"
                }.ifBlank { "কোনো লক্ষ্য যোগ করা হয়নি" }
            )
            val res = repo.exportPdfReport(uri, title, lines)
            showToast(
                if (res.isSuccess) AppStrings.t(lang, "PDF রিপোর্ট তৈরি হয়েছে", "PDF report exported")
                else AppStrings.t(lang, "PDF এক্সপোর্ট ব্যর্থ হয়েছে", "PDF export failed")
            )
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repo.clearAllUserData()
            isUnlockedFlow.value = true
            showToast(AppStrings.t(uiState.value.prefs.language, "সব ডেটা মুছে ফেলা হয়েছে", "All data cleared"))
        }
    }

    private fun parseYearMonth(isoDate: String): Pair<Int, Int> {
        val parts = isoDate.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 11
        return y to m
    }
}
