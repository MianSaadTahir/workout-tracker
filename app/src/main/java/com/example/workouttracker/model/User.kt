package com.example.workouttracker.model

import com.google.firebase.database.PropertyName

data class User(
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: String = "",
    val profilePicUri: String? = null,
    val password: String = "",
    @get:PropertyName("isPublic") @field:PropertyName("isPublic") val isPublic: Boolean = false,
    val weight: Double = 0.0,
    val weightUnit: String = "kg",
    val height: Double = 0.0,
    val heightUnit: String = "meters",
    val bmi: Double = 0.0
)
