package com.example.workouttracker

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workouttracker.adapter.LeaderboardAdapter
import com.example.workouttracker.adapter.LeaderboardFilter
import com.example.workouttracker.adapter.LeaderboardUser
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.databinding.ActivityLeaderboardBinding
import com.example.workouttracker.model.User
import com.example.workouttracker.model.Workout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var adapter: LeaderboardAdapter
    private var allLeaderboardUsers = listOf<LeaderboardUser>()
    private var currentFilter = LeaderboardFilter.STREAKS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        setupFilters()

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadLeaderboardData()
        }

        loadLeaderboardData()
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter(emptyList(), currentFilter)
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.rvLeaderboard.adapter = adapter
    }

    private fun setupFilters() {
        binding.chipGroupSort.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.chipStreaks -> LeaderboardFilter.STREAKS
                R.id.chipWorkouts -> LeaderboardFilter.WORKOUTS
                R.id.chipCalories -> LeaderboardFilter.CALORIES
                else -> return@setOnCheckedChangeListener
            }
            sortAndDisplayData(filter)
        }
    }

    private fun loadLeaderboardData() {
        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        binding.tvEmptyState.visibility = View.GONE

        val database = FirebaseRepository.database

        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(usersSnapshot: DataSnapshot) {
                database.child("workouts").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(workoutsSnapshot: DataSnapshot) {
                        processData(usersSnapshot, workoutsSnapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        hideLoadingViews()
                        Toast.makeText(this@LeaderboardActivity, "Failed to load workouts: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                hideLoadingViews()
                Toast.makeText(this@LeaderboardActivity, "Failed to load users: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hideLoadingViews() {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun processData(usersSnapshot: DataSnapshot, workoutsSnapshot: DataSnapshot) {
        val uidToUser = HashMap<String, User>()
        for (child in usersSnapshot.children) {
            val user = child.getValue(User::class.java)
            val uid = child.key
            if (user != null && uid != null && user.isPublic) {
                uidToUser[uid] = user
            }
        }

        val uidToWorkouts = HashMap<String, List<Workout>>()
        for (userWorkoutsSnapshot in workoutsSnapshot.children) {
            val userUid = userWorkoutsSnapshot.key ?: continue
            if (!uidToUser.containsKey(userUid)) continue

            val userWorkouts = mutableListOf<Workout>()
            for (workoutSnapshot in userWorkoutsSnapshot.children) {
                val workout = workoutSnapshot.getValue(Workout::class.java)
                if (workout != null) {
                    userWorkouts.add(workout)
                }
            }
            uidToWorkouts[userUid] = userWorkouts
        }

        val leaderboardUsers = mutableListOf<LeaderboardUser>()
        for ((uid, user) in uidToUser) {
            val workouts = uidToWorkouts[uid] ?: emptyList()

            val totalWorkouts = workouts.size
            val totalCalories = workouts.sumOf { it.sets * it.reps * it.weight } * 0.1
            val streak = calculateStreak(workouts)

            leaderboardUsers.add(
                LeaderboardUser(
                    uid = uid,
                    name = user.name.ifEmpty { "Anonymous User" },
                    streak = streak,
                    workoutsCount = totalWorkouts,
                    caloriesBurned = totalCalories
                )
            )
        }

        allLeaderboardUsers = leaderboardUsers
        sortAndDisplayData(currentFilter)
    }

    private fun sortAndDisplayData(filter: LeaderboardFilter) {
        currentFilter = filter
        val sortedList = when (filter) {
            LeaderboardFilter.STREAKS -> allLeaderboardUsers.sortedByDescending { it.streak }
            LeaderboardFilter.WORKOUTS -> allLeaderboardUsers.sortedByDescending { it.workoutsCount }
            LeaderboardFilter.CALORIES -> allLeaderboardUsers.sortedByDescending { it.caloriesBurned }
        }

        adapter.updateData(sortedList, filter)
        hideLoadingViews()

        if (sortedList.isEmpty()) {
            binding.rvLeaderboard.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvLeaderboard.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun calculateStreak(workouts: List<Workout>): Int {
        return com.example.workouttracker.util.StreakCalculator.calculateStreak(workouts)
    }
}
