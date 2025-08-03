package com.example.qvapayappandroid.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.qvapayappandroid.data.database.dao.SessionDao
import com.example.qvapayappandroid.data.database.dao.UserDao
import com.example.qvapayappandroid.data.database.entities.SessionEntity
import com.example.qvapayappandroid.data.database.entities.UserEntity

@Database(
    entities = [UserEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qvapay_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}