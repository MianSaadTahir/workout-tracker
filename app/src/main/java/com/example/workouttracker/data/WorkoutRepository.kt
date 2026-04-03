package com.example.workouttracker.data

import com.example.workouttracker.model.Workout

object WorkoutRepository {
    private val workouts = mutableListOf<Workout>()
    private var nextId = 1

    fun addWorkout(workout: Workout) {
        if (workout.id == -1) {
            workout.id = nextId++
            workouts.add(workout)
        } else {
            // Update existing workout
            val index = workouts.indexOfFirst { it.id == workout.id }
            if (index != -1) {
                workouts[index] = workout
            }
        }
    }

    fun getAllWorkouts(): List<Workout> = workouts

    fun deleteWorkout(id: Int) {
        workouts.removeAll { it.id == id }
    }

    fun getWorkoutById(id: Int): Workout? {
        return workouts.find { it.id == id }
    }

    fun getTotalWorkouts() = workouts.size
    fun getTotalSets() = workouts.sumOf { it.sets }
    fun getTotalReps() = workouts.sumOf { it.reps }
    fun getEstimatedCaloriesBurned(): Double {
        // Simple formula: total_volume * 0.1
        // Volume = sets * reps * weight
        val totalVolume = workouts.sumOf { it.sets * it.reps * it.weight }
        return totalVolume * 0.1
    }
}
