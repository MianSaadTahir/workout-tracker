package com.example.workouttracker.model

data class Workout(
    var id: Int,
    val name: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val category: String
)
