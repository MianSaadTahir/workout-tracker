package com.example.workouttracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.workouttracker.model.UserProfileEntity

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profile WHERE uid = :uid")
    suspend fun getProfile(uid: String): UserProfileEntity?
}
