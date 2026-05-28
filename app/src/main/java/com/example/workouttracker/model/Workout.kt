package com.example.workouttracker.model

data class Workout(
    val id: String = "",
    val name: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Double = 0.0,
    val category: String = "",
    val timestamp: Long = 0L
)
