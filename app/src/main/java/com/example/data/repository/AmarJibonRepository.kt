package com.example.data.repository

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.room.withTransaction
import com.example.core.AppStrings
import com.example.core.CalendarEngine
import com.example.core.CryptoManager
import com.example.data.local.*
import com.example.data.prefs.UserPreferences
import com.example.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.util.UUID

data class StarterTaskTemplate(
    val key: String,
    val titleBn: String,
    val titleEn: String,
    val startTime: String,
    val endTime: String,
    val category: String,
    val subTag: String,
    val estimatedMinutes: Int
)

data class StarterAmalTemplate(
    val key: String,
    val nameBn: String,
    val nameEn: String,
    val category: String,
    val dailyTarget: Int,
    val unitBn: String
)

object StarterCatalog {
    val defaultTasks = listOf(
        StarterTaskTemplate("t1", "তাহাজ্জুদ ও দোয়া", "Tahajjud & Dua", "04:15", "04:40", "IBADAT", "ইবাদত", 25),
        StarterTaskTemplate("t2", "মিসওয়াক, অজু ও পরিচ্ছন্নতা", "Miswak, Wudu & Hygiene", "04:40", "04:55", "HEALTH", "পরিচ্ছন্নতা", 15),
        StarterTaskTemplate("t3", "সকালের যিকির ও মাসনুন দোয়া", "Morning Zikr & Dua", "05:30", "05:50", "IBADAT", "যিকির", 20),
        StarterTaskTemplate("t4", "কুরআন তিলাওয়াত", "Quran Recitation", "05:50", "06:25", "IBADAT", "কুরআন", 35),
        StarterTaskTemplate("t5", "হাঁটাহাঁটি ও হালকা ব্যায়াম", "Walk & Exercise", "06:30", "07:00", "HEALTH", "ব্যায়াম", 30),
        StarterTaskTemplate("t6", "সকালের নাশতা ও প্রস্তুতি", "Breakfast & Preparation", "07:30", "08:00", "PERSONAL", "খাবার", 30),
        StarterTaskTemplate("t7", "দিনের কাজের পরিকল্পনা", "Daily Task Planning", "08:00", "08:15", "PERSONAL", "পরিকল্পনা", 15),
        StarterTaskTemplate("t8", "কর্মস্থল / মাদরাসায় উপস্থিতি", "Work / Madrasa Arrival", "08:30", "09:00", "WORK_MADRASA", "মাদরাসা", 30),
        StarterTaskTemplate("t9", "ক্লাস নেওয়া ও অফিসের কাজ", "Classes & Office Duties", "09:00", "12:00", "WORK_MADRASA", "শিক্ষকতা", 180),
        StarterTaskTemplate("t10", "ছাত্রদের পড়া পর্যবেক্ষণ ও যোগাযোগ", "Student Monitoring & Calls", "14:30", "16:00", "WORK_MADRASA", "ছাত্র তদারকি", 90),
        StarterTaskTemplate("t11", "পরিবার, স্ত্রী ও সন্তানের সময়", "Family, Spouse & Children", "19:00", "20:00", "FAMILY", "পরিবার", 60),
        StarterTaskTemplate("t12", "বাবা-মা ও আত্মীয়ের খোঁজ", "Parents & Relatives Check-in", "20:00", "20:20", "FAMILY", "বাবা-মা", 20),
        StarterTaskTemplate("t13", "দ্বীনি ইলম ও কিতাব অধ্যয়ন", "Deeni Ilm & Book Study", "21:00", "21:45", "EDUCATION", "অধ্যয়ন", 45),
        StarterTaskTemplate("t14", "আগামীকালের প্রস্তুতি ও মুহাসাবা", "Tomorrow Prep & Muhasabah", "22:00", "22:20", "PERSONAL", "আত্মসমালোচনা", 20),
        StarterTaskTemplate("t15", "ঘুমের দোয়া ও সুন্নাহ ঘুম", "Sleep Dua & Early Sleep", "22:30", "23:00", "HEALTH", "ঘুম", 30)
    )

    val defaultAmals = listOf(
        StarterAmalTemplate("a1", "জামাতে নামাজ", "Congregational Prayer", "NAMAZ", 5, "ওয়াক্ত"),
        StarterAmalTemplate("a2", "তাহাজ্জুদ", "Tahajjud", "NAMAZ", 1, "বার"),
        StarterAmalTemplate("a3", "ইশরাক", "Ishraq", "NAMAZ", 1, "বার"),
        StarterAmalTemplate("a4", "চাশত (দুহা)", "Chasht (Duha)", "NAMAZ", 1, "বার"),
        StarterAmalTemplate("a5", "কুরআন হিফজ", "Quran Hifz", "QURAN", 1, "বার"),
        StarterAmalTemplate("a6", "মুরাজাআ (দোহরানো)", "Muraja'a Revision", "QURAN", 1, "বার"),
        StarterAmalTemplate("a7", "সকাল-সন্ধ্যার যিকির", "Morning & Evening Zikr", "ZIKR", 2, "বেলা"),
        StarterAmalTemplate("a8", "ইস্তিগফার", "Istighfar", "ZIKR", 100, "বার"),
        StarterAmalTemplate("a9", "দরুদ শরিফ", "Durood Sharif", "ZIKR", 100, "বার"),
        StarterAmalTemplate("a10", "তাসবিহ-তাহলিল", "Tasbih & Tahlil", "ZIKR", 100, "বার"),
        StarterAmalTemplate("a11", "সদকা / দান", "Sadaqah", "CHARITY", 1, "বার"),
        StarterAmalTemplate("a12", "নফল রোজা", "Nafl Fasting", "SUNNAH", 1, "দিন"),
        StarterAmalTemplate("a13", "দ্বীনি ইলম অধ্যয়ন", "Deeni Ilm Study", "ILM", 1, "সেশন")
    )
}

class AmarJibonRepository(
    private val context: Context,
    private val db: AmarJibonDatabase,
    val prefsRepo: UserPreferencesRepository
) {
    private val routineDao = db.routineDao()
    private val amalDao = db.amalDao()
    private val lifeDao = db.lifeModulesDao()

    val preferencesFlow: Flow<UserPreferences> = prefsRepo.preferencesFlow
    val allTasksFlow: Flow<List<RoutineTaskEntity>> = routineDao.observeAllTasks()
    val allCompletionsFlow: Flow<List<TaskCompletionEntity>> = routineDao.observeAllCompletions()
    val allExceptionsFlow: Flow<List<TaskDateExceptionEntity>> = routineDao.observeAllExceptions()
    val allSpecialDaysFlow: Flow<List<SpecialDayEntity>> = routineDao.observeAllSpecialDays()

    val allAmalsFlow: Flow<List<AmalDefinitionEntity>> = amalDao.observeAllAmals()
    val allAmalLogsFlow: Flow<List<AmalLogEntity>> = amalDao.observeAllAmalLogs()
    val allPrayerLogsFlow: Flow<List<DailyPrayerLogEntity>> = amalDao.observeAllPrayerLogs()
    val allQuranProgressFlow: Flow<List<QuranProgressEntity>> = amalDao.observeAllQuranProgress()

    val allHealthFlow: Flow<List<HealthDailyEntity>> = lifeDao.observeAllHealth()
    val allFinanceFlow: Flow<List<FinanceTransactionEntity>> = lifeDao.observeAllFinance()
    val allDiaryFlow: Flow<List<DiaryNoteEntity>> = lifeDao.observeAllDiary()
    val allGoalsFlow: Flow<List<YearlyGoalEntity>> = lifeDao.observeAllGoals()
    val allMadrasaWorkFlow: Flow<List<MadrasaProfessionalItemEntity>> = lifeDao.observeAllMadrasaWork()
    val allRemindersFlow: Flow<List<CustomReminderEntity>> = lifeDao.observeAllReminders()

    suspend fun completeInitialSetup(
        userName: String,
        cityName: String,
        lat: Double,
        lon: Double,
        tzId: String,
        quranTarget: Int,
        waterTarget: Int,
        selectedTaskKeys: Set<String>,
        selectedAmalKeys: Set<String>,
        lang: String
    ) {
        val today = CalendarEngine.todayIso(tzId)
        db.withTransaction {
            val tasks = StarterCatalog.defaultTasks
                .filter { it.key in selectedTaskKeys }
                .map { tpl ->
                    RoutineTaskEntity(
                        id = UUID.randomUUID().toString(),
                        title = if (lang == "en") tpl.titleEn else tpl.titleBn,
                        startTime = tpl.startTime,
                        endTime = tpl.endTime,
                        category = tpl.category,
                        subTag = tpl.subTag,
                        priority = 1,
                        recurrence = "DAILY",
                        startDate = today,
                        estimatedMinutes = tpl.estimatedMinutes
                    )
                }
            if (tasks.isNotEmpty()) routineDao.upsertTasks(tasks)

            val amals = StarterCatalog.defaultAmals
                .filter { it.key in selectedAmalKeys }
                .mapIndexed { index, tpl ->
                    AmalDefinitionEntity(
                        id = UUID.randomUUID().toString(),
                        name = if (lang == "en") tpl.nameEn else tpl.nameBn,
                        category = tpl.category,
                        dailyTarget = tpl.dailyTarget,
                        unit = tpl.unitBn,
                        sortOrder = index
                    )
                }
            if (amals.isNotEmpty()) amalDao.upsertAmals(amals)
        }
        prefsRepo.update {
            it.copy(
                setupCompleted = true,
                userName = userName.trim(),
                cityName = cityName,
                latitude = lat,
                longitude = lon,
                timezoneId = tzId,
                dailyQuranTargetPages = quranTarget.coerceAtLeast(1),
                dailyWaterTargetGlasses = waterTarget.coerceAtLeast(1)
            )
        }
    }

    suspend fun upsertTask(task: RoutineTaskEntity) = routineDao.upsertTask(task)
    suspend fun deleteTask(id: String) = routineDao.deleteTask(id)

    suspend fun toggleTaskCompletion(taskId: String, date: String, currentlyCompleted: Boolean) {
        routineDao.upsertCompletion(
            TaskCompletionEntity(
                taskId = taskId,
                date = date,
                isCompleted = !currentlyCompleted,
                completedAt = if (!currentlyCompleted) System.currentTimeMillis() else 0L
            )
        )
    }

    suspend fun skipTaskForDate(taskId: String, date: String) {
        routineDao.upsertException(TaskDateExceptionEntity(taskId, date, "SKIP"))
    }

    suspend fun restoreTaskForDate(taskId: String, date: String) {
        routineDao.deleteException(taskId, date)
    }

    suspend fun carryTaskToTomorrow(task: RoutineTaskEntity, currentDate: String) {
        val tomorrow = CalendarEngine.addDays(currentDate, 1)
        if (task.recurrence == "ONCE") {
            routineDao.upsertTask(task.copy(startDate = tomorrow))
        } else {
            routineDao.upsertException(TaskDateExceptionEntity(task.id, tomorrow, "EXTRA"))
        }
    }

    suspend fun upsertSpecialDay(day: SpecialDayEntity) = routineDao.upsertSpecialDay(day)
    suspend fun deleteSpecialDay(date: String) = routineDao.deleteSpecialDay(date)

    suspend fun upsertAmal(amal: AmalDefinitionEntity) = amalDao.upsertAmal(amal)
    suspend fun deleteAmal(id: String) = amalDao.deleteAmal(id)

    suspend fun incrementAmal(amalId: String, date: String, delta: Int, currentCount: Int, note: String = "") {
        val next = (currentCount + delta).coerceAtLeast(0)
        amalDao.upsertAmalLog(
            AmalLogEntity(
                amalId = amalId,
                date = date,
                count = next,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateAmalNote(amalId: String, date: String, currentCount: Int, note: String) {
        amalDao.upsertAmalLog(
            AmalLogEntity(
                amalId = amalId,
                date = date,
                count = currentCount,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun upsertPrayerLog(log: DailyPrayerLogEntity) = amalDao.upsertPrayerLog(log)
    suspend fun upsertQuranProgress(q: QuranProgressEntity) = amalDao.upsertQuranProgress(q)
    suspend fun upsertHealthDaily(h: HealthDailyEntity) = lifeDao.upsertHealth(h)
    suspend fun upsertFinance(item: FinanceTransactionEntity) = lifeDao.upsertFinance(item)
    suspend fun deleteFinance(id: String) = lifeDao.deleteFinance(id)

    suspend fun saveDiaryNote(
        id: String?,
        date: String,
        type: String,
        title: String,
        plainBody: String,
        moodOrTag: String,
        isLocked: Boolean
    ) {
        val now = System.currentTimeMillis()
        val encrypted = CryptoManager.encrypt(plainBody)
        lifeDao.upsertDiary(
            DiaryNoteEntity(
                id = id ?: UUID.randomUUID().toString(),
                date = date,
                type = type,
                title = title,
                encryptedBody = encrypted,
                isLocked = isLocked,
                moodOrTag = moodOrTag,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun deleteDiaryNote(id: String) = lifeDao.deleteDiary(id)

    suspend fun upsertGoal(goal: YearlyGoalEntity) = lifeDao.upsertGoal(goal)
    suspend fun deleteGoal(id: String) = lifeDao.deleteGoal(id)

    suspend fun upsertMadrasaWork(item: MadrasaProfessionalItemEntity) = lifeDao.upsertMadrasaWork(item)
    suspend fun deleteMadrasaWork(id: String) = lifeDao.deleteMadrasaWork(id)

    suspend fun upsertReminder(r: CustomReminderEntity) = lifeDao.upsertReminder(r)
    suspend fun deleteReminder(id: String) = lifeDao.deleteReminder(id)

    suspend fun exportJsonBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val prefs = prefsRepo.getSnapshot()
            val root = JSONObject().apply {
                put("version", 1)
                put("exportedAt", System.currentTimeMillis())
                put("prefs", JSONObject().apply {
                    put("userName", prefs.userName)
                    put("language", prefs.language)
                    put("themeMode", prefs.themeMode)
                    put("dailyQuranTargetPages", prefs.dailyQuranTargetPages)
                    put("dailyWaterTargetGlasses", prefs.dailyWaterTargetGlasses)
                    put("monthlyBudget", prefs.monthlyBudget)
                    put("prayerCalcMethod", prefs.prayerCalcMethod)
                    put("asrJuristic", prefs.asrJuristic)
                    put("cityName", prefs.cityName)
                    put("latitude", prefs.latitude)
                    put("longitude", prefs.longitude)
                    put("timezoneId", prefs.timezoneId)
                    put("hijriOffset", prefs.hijriOffset)
                    put("qazaFajr", prefs.qazaFajr)
                    put("qazaZuhr", prefs.qazaZuhr)
                    put("qazaAsr", prefs.qazaAsr)
                    put("qazaMaghrib", prefs.qazaMaghrib)
                    put("qazaIsha", prefs.qazaIsha)
                })
                put("tasks", JSONArray().apply {
                    routineDao.getAllTasksOnce().forEach { t ->
                        put(JSONObject().apply {
                            put("id", t.id); put("title", t.title); put("description", t.description)
                            put("startTime", t.startTime); put("endTime", t.endTime)
                            put("category", t.category); put("subTag", t.subTag)
                            put("priority", t.priority); put("recurrence", t.recurrence)
                            put("startDate", t.startDate); put("reminderEnabled", t.reminderEnabled)
                            put("estimatedMinutes", t.estimatedMinutes); put("note", t.note)
                            put("isActive", t.isActive); put("createdAt", t.createdAt)
                        })
                    }
                })
                put("completions", JSONArray().apply {
                    routineDao.getAllCompletionsOnce().forEach { c ->
                        put(JSONObject().apply {
                            put("taskId", c.taskId); put("date", c.date)
                            put("isCompleted", c.isCompleted); put("completedAt", c.completedAt)
                            put("dailyNote", c.dailyNote)
                        })
                    }
                })
                put("exceptions", JSONArray().apply {
                    routineDao.getAllExceptionsOnce().forEach { e ->
                        put(JSONObject().apply {
                            put("taskId", e.taskId); put("date", e.date); put("type", e.type)
                        })
                    }
                })
                put("specialDays", JSONArray().apply {
                    routineDao.getAllSpecialDaysOnce().forEach { s ->
                        put(JSONObject().apply {
                            put("date", s.date); put("dayType", s.dayType)
                            put("title", s.title); put("note", s.note)
                        })
                    }
                })
                put("amals", JSONArray().apply {
                    amalDao.getAllAmalsOnce().forEach { a ->
                        put(JSONObject().apply {
                            put("id", a.id); put("name", a.name); put("category", a.category)
                            put("dailyTarget", a.dailyTarget); put("unit", a.unit)
                            put("note", a.note); put("sortOrder", a.sortOrder)
                        })
                    }
                })
                put("amalLogs", JSONArray().apply {
                    amalDao.getAllAmalLogsOnce().forEach { l ->
                        put(JSONObject().apply {
                            put("amalId", l.amalId); put("date", l.date)
                            put("count", l.count); put("note", l.note)
                        })
                    }
                })
                put("prayerLogs", JSONArray().apply {
                    amalDao.getAllPrayerLogsOnce().forEach { p ->
                        put(JSONObject().apply {
                            put("date", p.date); put("fajr", p.fajrStatus); put("zuhr", p.zuhrStatus)
                            put("asr", p.asrStatus); put("maghrib", p.maghribStatus); put("isha", p.ishaStatus)
                            put("tahajjud", p.tahajjudDone); put("ishraq", p.ishraqDone); put("chasht", p.chashtDone)
                            put("prepMiswak", p.prepMiswak); put("prepWudu", p.prepWudu)
                            put("prepCleanDress", p.prepCleanDress); put("prepMasjid", p.prepMasjid)
                        })
                    }
                })
                put("quran", JSONArray().apply {
                    amalDao.getAllQuranProgressOnce().forEach { q ->
                        put(JSONObject().apply {
                            put("date", q.date); put("pages", q.pagesRead); put("ayahs", q.ayahsRead)
                            put("hifz", q.hifzLines); put("muraja", q.murajaPages); put("note", q.surahNote)
                        })
                    }
                })
                put("health", JSONArray().apply {
                    lifeDao.getAllHealthOnce().forEach { h ->
                        put(JSONObject().apply {
                            put("date", h.date); put("water", h.waterGlasses); put("sleep", h.sleepHours.toDouble())
                            put("wakeTime", h.wakeUpTime); put("sleepTime", h.sleepTime)
                            put("exercise", h.exerciseMinutes); put("walk", h.walkMinutes)
                            put("bath", h.bathDone); put("brush", h.brushDone); put("clean", h.cleanClothesDone)
                            put("medicine", h.medicineNote)
                        })
                    }
                })
                put("finance", JSONArray().apply {
                    lifeDao.getAllFinanceOnce().forEach { f ->
                        put(JSONObject().apply {
                            put("id", f.id); put("date", f.date); put("type", f.type)
                            put("category", f.category); put("amount", f.amount); put("note", f.note)
                            put("settled", f.isSettled); put("createdAt", f.createdAt)
                        })
                    }
                })
                put("diary", JSONArray().apply {
                    lifeDao.getAllDiaryOnce().forEach { d ->
                        put(JSONObject().apply {
                            put("id", d.id); put("date", d.date); put("type", d.type)
                            put("title", d.title); put("body", CryptoManager.decrypt(d.encryptedBody))
                            put("mood", d.moodOrTag); put("locked", d.isLocked)
                            put("createdAt", d.createdAt); put("updatedAt", d.updatedAt)
                        })
                    }
                })
                put("goals", JSONArray().apply {
                    lifeDao.getAllGoalsOnce().forEach { g ->
                        put(JSONObject().apply {
                            put("id", g.id); put("year", g.year); put("month", g.month)
                            put("category", g.category); put("title", g.title)
                            put("target", g.targetValue); put("current", g.currentValue)
                            put("unit", g.unit); put("note", g.note)
                        })
                    }
                })
                put("madrasaWork", JSONArray().apply {
                    lifeDao.getAllMadrasaWorkOnce().forEach { m ->
                        put(JSONObject().apply {
                            put("id", m.id); put("section", m.section); put("title", m.title)
                            put("sub", m.subtitleOrPerson); put("sched", m.scheduleDayOrTime)
                            put("prog", m.statusOrProgress); put("target", m.targetProgress)
                            put("notes", m.notes); put("date", m.date); put("done", m.isCompleted)
                        })
                    }
                })
                put("reminders", JSONArray().apply {
                    lifeDao.getAllRemindersOnce().forEach { r ->
                        put(JSONObject().apply {
                            put("id", r.id); put("title", r.title); put("type", r.type)
                            put("time", r.time); put("date", r.date); put("interval", r.intervalHours)
                            put("enabled", r.isEnabled)
                        })
                    }
                })
            }
            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { writer ->
                    writer.write(root.toString(2))
                }
            } ?: error("Cannot open output stream")
        }
    }

    suspend fun importJsonBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                it.readText()
            } ?: error("Cannot open backup file")
            val root = JSONObject(raw)
            if (!root.has("tasks") || !root.has("amals")) error("Invalid backup structure")

            db.withTransaction {
                routineDao.clearTasks(); routineDao.clearCompletions(); routineDao.clearExceptions(); routineDao.clearSpecialDays()
                amalDao.clearAmals(); amalDao.clearAmalLogs(); amalDao.clearPrayerLogs(); amalDao.clearQuranProgress()
                lifeDao.clearHealth(); lifeDao.clearFinance(); lifeDao.clearDiary(); lifeDao.clearGoals(); lifeDao.clearMadrasaWork(); lifeDao.clearReminders()

                root.optJSONArray("tasks")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        RoutineTaskEntity(
                            id = o.getString("id"),
                            title = o.getString("title"),
                            description = o.optString("description"),
                            startTime = o.optString("startTime"),
                            endTime = o.optString("endTime"),
                            category = o.optString("category", "PERSONAL"),
                            subTag = o.optString("subTag"),
                            priority = o.optInt("priority", 1),
                            recurrence = o.optString("recurrence", "ONCE"),
                            startDate = o.optString("startDate"),
                            reminderEnabled = o.optBoolean("reminderEnabled"),
                            estimatedMinutes = o.optInt("estimatedMinutes"),
                            note = o.optString("note"),
                            isActive = o.optBoolean("isActive", true),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    }
                    routineDao.upsertTasks(list)
                }

                root.optJSONArray("completions")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        TaskCompletionEntity(
                            taskId = o.getString("taskId"),
                            date = o.getString("date"),
                            isCompleted = o.optBoolean("isCompleted"),
                            completedAt = o.optLong("completedAt"),
                            dailyNote = o.optString("dailyNote")
                        )
                    }
                    routineDao.upsertCompletions(list)
                }

                root.optJSONArray("exceptions")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        TaskDateExceptionEntity(o.getString("taskId"), o.getString("date"), o.getString("type"))
                    }
                    routineDao.upsertExceptions(list)
                }

                root.optJSONArray("specialDays")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        SpecialDayEntity(o.getString("date"), o.optString("dayType", "HOLIDAY"), o.optString("title"), o.optString("note"))
                    }
                    routineDao.upsertSpecialDays(list)
                }

                root.optJSONArray("amals")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        AmalDefinitionEntity(
                            id = o.getString("id"), name = o.getString("name"),
                            category = o.optString("category", "ZIKR"),
                            dailyTarget = o.optInt("dailyTarget", 1), unit = o.optString("unit"),
                            note = o.optString("note"), sortOrder = o.optInt("sortOrder")
                        )
                    }
                    amalDao.upsertAmals(list)
                }

                root.optJSONArray("amalLogs")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        AmalLogEntity(o.getString("amalId"), o.getString("date"), o.optInt("count"), o.optString("note"))
                    }
                    amalDao.upsertAmalLogs(list)
                }

                root.optJSONArray("prayerLogs")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        DailyPrayerLogEntity(
                            date = o.getString("date"),
                            fajrStatus = o.optInt("fajr"), zuhrStatus = o.optInt("zuhr"),
                            asrStatus = o.optInt("asr"), maghribStatus = o.optInt("maghrib"), ishaStatus = o.optInt("isha"),
                            tahajjudDone = o.optBoolean("tahajjud"), ishraqDone = o.optBoolean("ishraq"), chashtDone = o.optBoolean("chasht"),
                            prepMiswak = o.optBoolean("prepMiswak"), prepWudu = o.optBoolean("prepWudu"),
                            prepCleanDress = o.optBoolean("prepCleanDress"), prepMasjid = o.optBoolean("prepMasjid")
                        )
                    }
                    amalDao.upsertPrayerLogs(list)
                }

                root.optJSONArray("quran")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        QuranProgressEntity(o.getString("date"), o.optInt("pages"), o.optInt("ayahs"), o.optInt("hifz"), o.optInt("muraja"), o.optString("note"))
                    }
                    amalDao.upsertQuranProgressList(list)
                }

                root.optJSONArray("health")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        HealthDailyEntity(
                            date = o.getString("date"), waterGlasses = o.optInt("water"),
                            sleepHours = o.optDouble("sleep", 0.0).toFloat(),
                            wakeUpTime = o.optString("wakeTime"), sleepTime = o.optString("sleepTime"),
                            exerciseMinutes = o.optInt("exercise"), walkMinutes = o.optInt("walk"),
                            bathDone = o.optBoolean("bath"), brushDone = o.optBoolean("brush"),
                            cleanClothesDone = o.optBoolean("clean"), medicineNote = o.optString("medicine")
                        )
                    }
                    lifeDao.upsertHealthList(list)
                }

                root.optJSONArray("finance")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        FinanceTransactionEntity(
                            id = o.getString("id"), date = o.getString("date"),
                            type = o.getString("type"), category = o.getString("category"),
                            amount = o.getDouble("amount"), note = o.optString("note"),
                            isSettled = o.optBoolean("settled"), createdAt = o.optLong("createdAt")
                        )
                    }
                    lifeDao.upsertFinanceList(list)
                }

                root.optJSONArray("diary")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        DiaryNoteEntity(
                            id = o.getString("id"), date = o.getString("date"),
                            type = o.getString("type"), title = o.getString("title"),
                            encryptedBody = CryptoManager.encrypt(o.optString("body")),
                            isLocked = o.optBoolean("locked"), moodOrTag = o.optString("mood"),
                            createdAt = o.optLong("createdAt"), updatedAt = o.optLong("updatedAt")
                        )
                    }
                    lifeDao.upsertDiaryList(list)
                }

                root.optJSONArray("goals")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        YearlyGoalEntity(
                            id = o.getString("id"), year = o.getInt("year"), month = o.optInt("month"),
                            category = o.optString("category"), title = o.getString("title"),
                            targetValue = o.getInt("target"), currentValue = o.optInt("current"),
                            unit = o.optString("unit"), note = o.optString("note")
                        )
                    }
                    lifeDao.upsertGoals(list)
                }

                root.optJSONArray("madrasaWork")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        MadrasaProfessionalItemEntity(
                            id = o.getString("id"), section = o.getString("section"),
                            title = o.getString("title"), subtitleOrPerson = o.optString("sub"),
                            scheduleDayOrTime = o.optString("sched"), statusOrProgress = o.optInt("prog"),
                            targetProgress = o.optInt("target", 100), notes = o.optString("notes"),
                            date = o.optString("date"), isCompleted = o.optBoolean("done")
                        )
                    }
                    lifeDao.upsertMadrasaWorkList(list)
                }

                root.optJSONArray("reminders")?.let { arr ->
                    val list = (0 until arr.length()).map { i ->
                        val o = arr.getJSONObject(i)
                        CustomReminderEntity(
                            id = o.getString("id"), title = o.getString("title"),
                            type = o.getString("type"), time = o.getString("time"),
                            date = o.optString("date"), intervalHours = o.optInt("interval"),
                            isEnabled = o.optBoolean("enabled", true)
                        )
                    }
                    lifeDao.upsertReminders(list)
                }
            }

            root.optJSONObject("prefs")?.let { po ->
                prefsRepo.update { cur ->
                    cur.copy(
                        setupCompleted = true,
                        userName = po.optString("userName", cur.userName),
                        language = po.optString("language", cur.language),
                        themeMode = po.optString("themeMode", cur.themeMode),
                        dailyQuranTargetPages = po.optInt("dailyQuranTargetPages", cur.dailyQuranTargetPages),
                        dailyWaterTargetGlasses = po.optInt("dailyWaterTargetGlasses", cur.dailyWaterTargetGlasses),
                        monthlyBudget = po.optDouble("monthlyBudget", cur.monthlyBudget),
                        prayerCalcMethod = po.optString("prayerCalcMethod", cur.prayerCalcMethod),
                        asrJuristic = po.optString("asrJuristic", cur.asrJuristic),
                        cityName = po.optString("cityName", cur.cityName),
                        latitude = po.optDouble("latitude", cur.latitude),
                        longitude = po.optDouble("longitude", cur.longitude),
                        timezoneId = po.optString("timezoneId", cur.timezoneId),
                        hijriOffset = po.optInt("hijriOffset", cur.hijriOffset),
                        qazaFajr = po.optInt("qazaFajr", cur.qazaFajr),
                        qazaZuhr = po.optInt("qazaZuhr", cur.qazaZuhr),
                        qazaAsr = po.optInt("qazaAsr", cur.qazaAsr),
                        qazaMaghrib = po.optInt("qazaMaghrib", cur.qazaMaghrib),
                        qazaIsha = po.optInt("qazaIsha", cur.qazaIsha)
                    )
                }
            }
            Unit
        }
    }

    suspend fun exportCsvReport(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val tasks = routineDao.getAllTasksOnce()
            val prayers = amalDao.getAllPrayerLogsOnce()
            val quran = amalDao.getAllQuranProgressOnce()
            val finance = lifeDao.getAllFinanceOnce()

            val sb = StringBuilder()
            sb.append('\uFEFF') // UTF-8 BOM for Bengali Excel/Sheets compatibility
            sb.appendLine("Section,Date,Title/Category,Type/Time,Value/Status,Note")
            tasks.forEach { t ->
                sb.appendLine("Routine,${escCsv(t.startDate)},${escCsv(t.title)},${escCsv(t.startTime)},${escCsv(t.recurrence)},${escCsv(t.note)}")
            }
            prayers.forEach { p ->
                val count = listOf(p.fajrStatus, p.zuhrStatus, p.asrStatus, p.maghribStatus, p.ishaStatus).count { it > 0 }
                sb.appendLine("Prayer,${escCsv(p.date)},5-Wakt,${count}/5,Tahajjud=${p.tahajjudDone},Ishraq=${p.ishraqDone}")
            }
            quran.forEach { q ->
                sb.appendLine("Quran,${escCsv(q.date)},Tilawat,${q.pagesRead} pages,${q.ayahsRead} ayahs,${escCsv(q.surahNote)}")
            }
            finance.forEach { f ->
                sb.appendLine("Finance,${escCsv(f.date)},${escCsv(f.category)},${escCsv(f.type)},${f.amount},${escCsv(f.note)}")
            }

            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { it.write(sb.toString()) }
            } ?: error("Cannot write CSV")
        }
    }

    suspend fun exportPdfReport(uri: Uri, reportTitle: String, lines: List<String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val pdf = PdfDocument()
                val pageWidth = 595
                val pageHeight = 842
                val margin = 42f
                val contentWidth = (pageWidth - margin * 2).toInt()

                val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#0B4F33")
                    textSize = 18f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#1A2B22")
                    textSize = 11.5f
                }
                val accentPaint = Paint().apply {
                    color = Color.parseColor("#C9A227")
                    strokeWidth = 3f
                }

                var pageNumber = 1
                var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                var canvas: Canvas = page.canvas
                var y = margin

                fun drawTextBlock(text: String, paint: TextPaint) {
                    val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, contentWidth)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setLineSpacing(2f, 1.15f)
                        .build()
                    if (y + layout.height > pageHeight - margin) {
                        pdf.finishPage(page)
                        pageNumber++
                        page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                        canvas = page.canvas
                        y = margin
                    }
                    canvas.save()
                    canvas.translate(margin, y)
                    layout.draw(canvas)
                    canvas.restore()
                    y += layout.height + 8f
                }

                drawTextBlock("আমার জীবন (My Life) — $reportTitle", titlePaint)
                canvas.drawLine(margin, y, pageWidth - margin, y, accentPaint)
                y += 14f

                lines.forEach { line ->
                    drawTextBlock(line, bodyPaint)
                }

                pdf.finishPage(page)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    pdf.writeTo(out)
                } ?: error("Cannot write PDF")
                pdf.close()
            }
        }

    suspend fun clearAllUserData() {
        db.withTransaction {
            routineDao.clearTasks(); routineDao.clearCompletions(); routineDao.clearExceptions(); routineDao.clearSpecialDays()
            amalDao.clearAmals(); amalDao.clearAmalLogs(); amalDao.clearPrayerLogs(); amalDao.clearQuranProgress()
            lifeDao.clearHealth(); lifeDao.clearFinance(); lifeDao.clearDiary(); lifeDao.clearGoals(); lifeDao.clearMadrasaWork(); lifeDao.clearReminders()
        }
        prefsRepo.clearAll()
    }

    private fun escCsv(value: String): String {
        val cleaned = value.replace("\"", "\"\"")
        return "\"$cleaned\""
    }
}
