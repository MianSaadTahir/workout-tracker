package com.example.workouttracker.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.workouttracker.model.UserProfileEntity

@Database(entities = [UserProfileEntity::class], version = 1, exportSchema = false)
abstract class UserProfileDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: UserProfileDatabase? = null

        fun getDatabase(context: Context): UserProfileDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserProfileDatabase::class.java,
                    "user_profile_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
