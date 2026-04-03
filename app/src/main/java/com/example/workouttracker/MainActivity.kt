package com.example.workouttracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workouttracker.adapter.WorkoutAdapter
import com.example.workouttracker.data.WorkoutRepository
import com.example.workouttracker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.btnAddWorkout.setOnClickListener {
            val intent = Intent(this, AddWorkoutActivity::class.java)
            startActivity(intent)
        }

        binding.btnViewSummary.setOnClickListener {
            val intent = Intent(this, SummaryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setupRecyclerView() {
        adapter = WorkoutAdapter(
            workouts = WorkoutRepository.getAllWorkouts(),
            onEdit = { workout ->
                val intent = Intent(this, AddWorkoutActivity::class.java)
                intent.putExtra("WORKOUT_ID", workout.id)
                startActivity(intent)
            },
            onDelete = { workout ->
                showDeleteConfirmation(workout.id)
            }
        )
        binding.rvWorkouts.layoutManager = LinearLayoutManager(this)
        binding.rvWorkouts.adapter = adapter
    }

    private fun showDeleteConfirmation(id: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete this workout?")
            .setPositiveButton("Delete") { _, _ ->
                WorkoutRepository.deleteWorkout(id)
                updateUI()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUI() {
        val workouts = WorkoutRepository.getAllWorkouts()
        adapter.updateList(workouts)
        
        if (workouts.isEmpty()) {
            binding.rvWorkouts.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvWorkouts.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }
}
