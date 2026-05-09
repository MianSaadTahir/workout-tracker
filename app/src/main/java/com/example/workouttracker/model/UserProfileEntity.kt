package com.example.workouttracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val uid: String,           // Firebase user UID as primary key
    val profilePicBase64: String  // image stored as Base64 string
)
