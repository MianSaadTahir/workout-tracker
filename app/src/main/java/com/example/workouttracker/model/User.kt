package com.example.workouttracker.model

data class User(
    val name: String,
    val email: String,
    val password: String,
    var age: Int,
    var gender: String,
    var profilePicUri: String? = null
)
