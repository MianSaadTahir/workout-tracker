package com.example.workouttracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.data.AppRepository
import com.example.workouttracker.data.WorkoutRepository
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

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadFreshWorkouts()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun displaySummary() {
        val workouts = WorkoutRepository.getAllWorkouts()
        val totalWorkouts = workouts.size
        val totalSets = workouts.sumOf { it.sets }
        val totalReps = workouts.sumOf { it.reps }
        val totalVolume = workouts.sumOf { it.sets * it.reps * it.weight }

        binding.tvTotalWorkouts.text = "Total Workouts: $totalWorkouts"
        binding.tvTotalSets.text = "Total Sets: $totalSets"
        binding.tvTotalReps.text = "Total Reps: $totalReps"
        
        val calories = String.format("%.2f", totalVolume * 0.1)
        binding.tvTotalCalories.text = "Estimated Calories: $calories kcal"
    }

    private fun loadFreshWorkouts() {
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) {
            binding.swipeRefreshLayout.isRefreshing = false
            return
        }

        FirebaseRepository.database.child("workouts").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val workoutList = mutableListOf<Workout>()
                    for (child in snapshot.children) {
                        val workout = child.getValue(Workout::class.java)
                        if (workout != null) {
                            workoutList.add(workout)
                        }
                    }

                    // Update memory cache
                    val email = FirebaseRepository.auth.currentUser?.email ?: ""
                    if (email.isNotEmpty()) {
                        val cacheList = AppRepository.getWorkoutsForCurrentUser()
                        cacheList.clear()
                        cacheList.addAll(workoutList)
                    }

                    // Recalculate summary metrics instantly
                    displaySummary()

                    binding.swipeRefreshLayout.isRefreshing = false
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(this@SummaryActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
}
