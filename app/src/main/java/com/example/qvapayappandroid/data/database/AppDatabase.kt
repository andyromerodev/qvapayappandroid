package com.example.qvapayappandroid.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.qvapayappandroid.data.database.dao.OfferAlertDao
import com.example.qvapayappandroid.data.database.dao.OfferTemplateDao
import com.example.qvapayappandroid.data.database.dao.SessionDao
import com.example.qvapayappandroid.data.database.dao.SettingsDao
import com.example.qvapayappandroid.data.database.dao.UserDao
import com.example.qvapayappandroid.data.database.entities.OfferAlertEntity
import com.example.qvapayappandroid.data.database.entities.OfferTemplateEntity
import com.example.qvapayappandroid.data.database.entities.SessionEntity
import com.example.qvapayappandroid.data.database.entities.SettingsEntity
import com.example.qvapayappandroid.data.database.entities.UserEntity

@Database(
    entities = [UserEntity::class, SessionEntity::class, SettingsEntity::class, OfferTemplateEntity::class, OfferAlertEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun settingsDao(): SettingsDao
    abstract fun offerTemplateDao(): OfferTemplateDao
    abstract fun offerAlertDao(): OfferAlertDao
    
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
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    ALTER TABLE users ADD COLUMN twoFactorSecret TEXT
                """)
                database.execSQL("""
                    ALTER TABLE users ADD COLUMN twoFactorResetCode TEXT
                """)
                database.execSQL("""
                    ALTER TABLE users ADD COLUMN phoneRequestId TEXT
                """)
                database.execSQL("""
                    ALTER TABLE users ADD COLUMN canWithdraw INTEGER NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE users ADD COLUMN canDeposit INTEGER NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE users ADD COLUMN canTransfer INTEGER NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE users ADD COLUMN canBuy INTEGER NOT NULL DEFAULT 0
                """)
                database.execSQL("""
                    ALTER TABLE users ADD COLUMN canSell INTEGER NOT NULL DEFAULT 0
                """)
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `offer_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `type` TEXT NOT NULL,
                        `coinId` TEXT NOT NULL,
                        `coinName` TEXT NOT NULL,
                        `coinTick` TEXT NOT NULL,
                        `amount` TEXT NOT NULL,
                        `receive` TEXT NOT NULL,
                        `detailsJson` TEXT NOT NULL,
                        `onlyKyc` INTEGER NOT NULL,
                        `private` INTEGER NOT NULL,
                        `promoteOffer` INTEGER NOT NULL,
                        `onlyVip` INTEGER NOT NULL,
                        `message` TEXT NOT NULL,
                        `webhook` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """)
            }
        }
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `offer_alerts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `coinType` TEXT NOT NULL,
                        `offerType` TEXT NOT NULL,
                        `minAmount` REAL,
                        `maxAmount` REAL,
                        `targetRate` REAL NOT NULL,
                        `rateComparison` TEXT NOT NULL,
                        `onlyKyc` INTEGER NOT NULL,
                        `onlyVip` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `checkIntervalMinutes` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `lastCheckedAt` INTEGER,
                        `lastTriggeredAt` INTEGER
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
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()
                INSTANCE = instance
                instance
            }
        }
    }
}