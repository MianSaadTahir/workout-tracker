package com.example.workouttracker.data

import com.example.workouttracker.model.Workout

object WorkoutRepository {
    private fun getWorkouts() = AppRepository.getWorkoutsForCurrentUser()
    private var nextId = 1

    fun addWorkout(workout: Workout) {
        val workouts = getWorkouts()
        if (workout.id.isEmpty() || workout.id == "-1") {
            val newWorkout = workout.copy(id = (nextId++).toString())
            workouts.add(newWorkout)
        } else {
            // Update existing workout
            val index = workouts.indexOfFirst { it.id == workout.id }
            if (index != -1) {
                workouts[index] = workout
            }
        }
    }

    fun getAllWorkouts(): List<Workout> = getWorkouts()

    fun deleteWorkout(id: String) {
        getWorkouts().removeAll { it.id == id }
    }

    fun getWorkoutById(id: String): Workout? {
        return getWorkouts().find { it.id == id }
    }

    fun getTotalWorkouts() = getWorkouts().size
    fun getTotalSets() = getWorkouts().sumOf { it.sets }
    fun getTotalReps() = getWorkouts().sumOf { it.reps }
    fun getEstimatedCaloriesBurned(): Double {
        // Simple formula: total_volume * 0.1
        // Volume = sets * reps * weight
        val totalVolume = getWorkouts().sumOf { it.sets * it.reps * it.weight }
        return totalVolume * 0.1
    }
}
