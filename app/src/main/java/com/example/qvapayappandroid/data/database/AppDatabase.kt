package com.example.qvapayappandroid.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.qvapayappandroid.data.database.dao.SessionDao
import com.example.qvapayappandroid.data.database.dao.SettingsDao
import com.example.qvapayappandroid.data.database.dao.UserDao
import com.example.qvapayappandroid.data.database.entities.SessionEntity
import com.example.qvapayappandroid.data.database.entities.SettingsEntity
import com.example.qvapayappandroid.data.database.entities.UserEntity

@Database(
    entities = [UserEntity::class, SessionEntity::class, SettingsEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun settingsDao(): SettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `settings` (
                        `id` INTEGER NOT NULL,
                        `theme` TEXT NOT NULL,
                        `language` TEXT NOT NULL,
                        `notificationsEnabled` INTEGER NOT NULL,
                        `biometricEnabled` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qvapay_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}