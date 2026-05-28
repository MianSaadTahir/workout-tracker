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
        binding.tvTotalCalories.text = "Calories: $calories kcal"

        val streak = calculateStreak(workouts)
        val dayText = if (streak == 1) "day" else "days"
        binding.tvSummaryStreakVal.text = "Streak: $streak $dayText"
    }

    private fun calculateStreak(workouts: List<Workout>): Int {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val uniqueDays = workouts
            .filter { it.timestamp > 0L }
            .map { sdf.format(java.util.Date(it.timestamp)) }
            .toSet()

        if (uniqueDays.isEmpty()) return 0

        val calendar = java.util.Calendar.getInstance()
        val todayStr = sdf.format(calendar.time)

        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(calendar.time)

        if (!uniqueDays.contains(todayStr) && !uniqueDays.contains(yesterdayStr)) {
            return 0
        }

        var checkDate = if (uniqueDays.contains(todayStr)) {
            java.util.Calendar.getInstance()
        } else {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            cal
        }

        var streak = 0
        while (true) {
            val dateStr = sdf.format(checkDate.time)
            if (uniqueDays.contains(dateStr)) {
                streak++
                checkDate.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
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
