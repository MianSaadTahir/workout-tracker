package com.example.workouttracker.model

import com.google.firebase.database.PropertyName

data class User(
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: String = "",
    val profilePicUri: String? = null,
    val password: String = "",
    @get:PropertyName("isPublic") @field:PropertyName("isPublic") val isPublic: Boolean = false
)
