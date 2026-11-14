package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine_tasks WHERE isActive = 1 ORDER BY startTime ASC, createdAt ASC")
    fun observeAllTasks(): Flow<List<RoutineTaskEntity>>

    @Query("SELECT * FROM routine_tasks")
    suspend fun getAllTasksOnce(): List<RoutineTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: RoutineTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(tasks: List<RoutineTaskEntity>)

    @Query("DELETE FROM routine_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("SELECT * FROM task_completions")
    fun observeAllCompletions(): Flow<List<TaskCompletionEntity>>

    @Query("SELECT * FROM task_completions")
    suspend fun getAllCompletionsOnce(): List<TaskCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(completion: TaskCompletionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletions(list: List<TaskCompletionEntity>)

    @Query("SELECT * FROM task_date_exceptions")
    fun observeAllExceptions(): Flow<List<TaskDateExceptionEntity>>

    @Query("SELECT * FROM task_date_exceptions")
    suspend fun getAllExceptionsOnce(): List<TaskDateExceptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertException(ex: TaskDateExceptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExceptions(list: List<TaskDateExceptionEntity>)

    @Query("DELETE FROM task_date_exceptions WHERE taskId = :taskId AND date = :date")
    suspend fun deleteException(taskId: String, date: String)

    @Query("SELECT * FROM special_days")
    fun observeAllSpecialDays(): Flow<List<SpecialDayEntity>>

    @Query("SELECT * FROM special_days")
    suspend fun getAllSpecialDaysOnce(): List<SpecialDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpecialDay(day: SpecialDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpecialDays(list: List<SpecialDayEntity>)

    @Query("DELETE FROM special_days WHERE date = :date")
    suspend fun deleteSpecialDay(date: String)

    @Query("DELETE FROM routine_tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM task_completions")
    suspend fun clearCompletions()

    @Query("DELETE FROM task_date_exceptions")
    suspend fun clearExceptions()

    @Query("DELETE FROM special_days")
    suspend fun clearSpecialDays()
}

@Dao
interface AmalDao {
    @Query("SELECT * FROM amal_definitions WHERE isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun observeAllAmals(): Flow<List<AmalDefinitionEntity>>

    @Query("SELECT * FROM amal_definitions")
    suspend fun getAllAmalsOnce(): List<AmalDefinitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAmal(amal: AmalDefinitionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAmals(list: List<AmalDefinitionEntity>)

    @Query("DELETE FROM amal_definitions WHERE id = :id")
    suspend fun deleteAmal(id: String)

    @Query("SELECT * FROM amal_logs")
    fun observeAllAmalLogs(): Flow<List<AmalLogEntity>>

    @Query("SELECT * FROM amal_logs")
    suspend fun getAllAmalLogsOnce(): List<AmalLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAmalLog(log: AmalLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAmalLogs(list: List<AmalLogEntity>)

    @Query("SELECT * FROM daily_prayer_logs")
    fun observeAllPrayerLogs(): Flow<List<DailyPrayerLogEntity>>

    @Query("SELECT * FROM daily_prayer_logs")
    suspend fun getAllPrayerLogsOnce(): List<DailyPrayerLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrayerLog(log: DailyPrayerLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrayerLogs(list: List<DailyPrayerLogEntity>)

    @Query("SELECT * FROM quran_progress")
    fun observeAllQuranProgress(): Flow<List<QuranProgressEntity>>

    @Query("SELECT * FROM quran_progress")
    suspend fun getAllQuranProgressOnce(): List<QuranProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuranProgress(q: QuranProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuranProgressList(list: List<QuranProgressEntity>)

    @Query("DELETE FROM amal_definitions")
    suspend fun clearAmals()

    @Query("DELETE FROM amal_logs")
    suspend fun clearAmalLogs()

    @Query("DELETE FROM daily_prayer_logs")
    suspend fun clearPrayerLogs()

    @Query("DELETE FROM quran_progress")
    suspend fun clearQuranProgress()
}

@Dao
interface LifeModulesDao {
    @Query("SELECT * FROM health_daily")
    fun observeAllHealth(): Flow<List<HealthDailyEntity>>

    @Query("SELECT * FROM health_daily")
    suspend fun getAllHealthOnce(): List<HealthDailyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHealth(h: HealthDailyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHealthList(list: List<HealthDailyEntity>)

    @Query("SELECT * FROM finance_transactions ORDER BY date DESC, createdAt DESC")
    fun observeAllFinance(): Flow<List<FinanceTransactionEntity>>

    @Query("SELECT * FROM finance_transactions")
    suspend fun getAllFinanceOnce(): List<FinanceTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFinance(item: FinanceTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFinanceList(list: List<FinanceTransactionEntity>)

    @Query("DELETE FROM finance_transactions WHERE id = :id")
    suspend fun deleteFinance(id: String)

    @Query("SELECT * FROM diary_notes ORDER BY date DESC, updatedAt DESC")
    fun observeAllDiary(): Flow<List<DiaryNoteEntity>>

    @Query("SELECT * FROM diary_notes")
    suspend fun getAllDiaryOnce(): List<DiaryNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiary(note: DiaryNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiaryList(list: List<DiaryNoteEntity>)

    @Query("DELETE FROM diary_notes WHERE id = :id")
    suspend fun deleteDiary(id: String)

    @Query("SELECT * FROM yearly_goals ORDER BY year DESC, month ASC")
    fun observeAllGoals(): Flow<List<YearlyGoalEntity>>

    @Query("SELECT * FROM yearly_goals")
    suspend fun getAllGoalsOnce(): List<YearlyGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: YearlyGoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoals(list: List<YearlyGoalEntity>)

    @Query("DELETE FROM yearly_goals WHERE id = :id")
    suspend fun deleteGoal(id: String)

    @Query("SELECT * FROM madrasa_professional_items ORDER BY createdAt DESC")
    fun observeAllMadrasaWork(): Flow<List<MadrasaProfessionalItemEntity>>

    @Query("SELECT * FROM madrasa_professional_items")
    suspend fun getAllMadrasaWorkOnce(): List<MadrasaProfessionalItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMadrasaWork(item: MadrasaProfessionalItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMadrasaWorkList(list: List<MadrasaProfessionalItemEntity>)

    @Query("DELETE FROM madrasa_professional_items WHERE id = :id")
    suspend fun deleteMadrasaWork(id: String)

    @Query("SELECT * FROM custom_reminders ORDER BY time ASC")
    fun observeAllReminders(): Flow<List<CustomReminderEntity>>

    @Query("SELECT * FROM custom_reminders")
    suspend fun getAllRemindersOnce(): List<CustomReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminder(reminder: CustomReminderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminders(list: List<CustomReminderEntity>)

    @Query("DELETE FROM custom_reminders WHERE id = :id")
    suspend fun deleteReminder(id: String)

    @Query("DELETE FROM health_daily")
    suspend fun clearHealth()

    @Query("DELETE FROM finance_transactions")
    suspend fun clearFinance()

    @Query("DELETE FROM diary_notes")
    suspend fun clearDiary()

    @Query("DELETE FROM yearly_goals")
    suspend fun clearGoals()

    @Query("DELETE FROM madrasa_professional_items")
    suspend fun clearMadrasaWork()

    @Query("DELETE FROM custom_reminders")
    suspend fun clearReminders()
}
