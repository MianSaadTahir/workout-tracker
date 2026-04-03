package com.example.workouttracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.WorkoutRepository
import com.example.workouttracker.databinding.ActivitySummaryBinding

class SummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displaySummary()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun displaySummary() {
        binding.tvTotalWorkouts.text = "Total Workouts: ${WorkoutRepository.getTotalWorkouts()}"
        binding.tvTotalSets.text = "Total Sets: ${WorkoutRepository.getTotalSets()}"
        binding.tvTotalReps.text = "Total Reps: ${WorkoutRepository.getTotalReps()}"
        
        val calories = String.format("%.2f", WorkoutRepository.getEstimatedCaloriesBurned())
        binding.tvTotalCalories.text = "Estimated Calories: $calories kcal"
    }
}
