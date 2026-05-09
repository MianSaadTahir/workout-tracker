package com.example.workouttracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.databinding.ActivitySummaryBinding
import com.example.workouttracker.model.Workout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

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
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        FirebaseRepository.database.child("workouts").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalWorkouts = 0
                    var totalSets = 0
                    var totalReps = 0
                    var totalVolume = 0.0

                    for (child in snapshot.children) {
                        val workout = child.getValue(Workout::class.java) ?: continue
                        totalWorkouts++
                        totalSets += workout.sets
                        totalReps += workout.reps
                        totalVolume += workout.sets * workout.reps * workout.weight
                    }

                    binding.tvTotalWorkouts.text = "Total Workouts: $totalWorkouts"
                    binding.tvTotalSets.text = "Total Sets: $totalSets"
                    binding.tvTotalReps.text = "Total Reps: $totalReps"
                    
                    val calories = String.format("%.2f", totalVolume * 0.1)
                    binding.tvTotalCalories.text = "Estimated Calories: $calories kcal"
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
