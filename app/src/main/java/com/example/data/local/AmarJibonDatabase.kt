package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RoutineTaskEntity::class,
        TaskCompletionEntity::class,
        TaskDateExceptionEntity::class,
        SpecialDayEntity::class,
        AmalDefinitionEntity::class,
        AmalLogEntity::class,
        DailyPrayerLogEntity::class,
        QuranProgressEntity::class,
        HealthDailyEntity::class,
        FinanceTransactionEntity::class,
        DiaryNoteEntity::class,
        YearlyGoalEntity::class,
        MadrasaProfessionalItemEntity::class,
        CustomReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AmarJibonDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun amalDao(): AmalDao
    abstract fun lifeModulesDao(): LifeModulesDao

    companion object {
        @Volatile
        private var INSTANCE: AmarJibonDatabase? = null

        fun getInstance(context: Context): AmarJibonDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AmarJibonDatabase::class.java,
                    "amar_jibon_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
