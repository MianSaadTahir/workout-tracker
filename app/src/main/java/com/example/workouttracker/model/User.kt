package com.example.workouttracker.model

data class User(
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: String = "",
    val profilePicUri: String? = null
)
