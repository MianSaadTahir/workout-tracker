package com.example.workouttracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workouttracker.adapter.WorkoutAdapter
import com.example.workouttracker.data.AppRepository
import com.example.workouttracker.data.WorkoutRepository
import com.example.workouttracker.databinding.ActivityMainBinding
import com.google.android.material.imageview.ShapeableImageView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: WorkoutAdapter
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarAndDrawer()
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

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.app_name, R.string.app_name)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_add_workout -> {
                    startActivity(Intent(this, AddWorkoutActivity::class.java))
                }
                R.id.nav_summary -> {
                    startActivity(Intent(this, SummaryActivity::class.java))
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_logout -> {
                    logout()
                }
            }
            true
        }

        // Header clicks
        val headerView = binding.navView.getHeaderView(0)
        val ivProfile = headerView.findViewById<ShapeableImageView>(R.id.ivHeaderProfile)
        val tvName = headerView.findViewById<TextView>(R.id.tvHeaderName)
        
        val openProfile = View.OnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
        
        ivProfile.setOnClickListener(openProfile)
        tvName.setOnClickListener(openProfile)
    }

    private fun logout() {
        AppRepository.logout()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        refreshDrawerHeader()
    }

    private fun refreshDrawerHeader() {
        val user = AppRepository.currentUser ?: return
        val headerView = binding.navView.getHeaderView(0)
        val ivProfile = headerView.findViewById<ShapeableImageView>(R.id.ivHeaderProfile)
        val tvName = headerView.findViewById<TextView>(R.id.tvHeaderName)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvHeaderEmail)

        tvName.text = user.name
        tvEmail.text = user.email
        
        user.profilePicUri?.let {
            ivProfile.setImageURI(Uri.parse(it))
        } ?: run {
            // Default avatar if needed, or just leave it
        }
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

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
