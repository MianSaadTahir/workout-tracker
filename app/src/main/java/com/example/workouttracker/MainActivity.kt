package com.example.workouttracker

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workouttracker.adapter.WorkoutAdapter
import com.example.workouttracker.database.UserProfileDatabase
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.databinding.ActivityMainBinding
import com.example.workouttracker.model.User
import com.example.workouttracker.model.Workout
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        syncWorkouts()

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
        FirebaseRepository.logout()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        refreshDrawerHeader()
    }

    private fun refreshDrawerHeader() {
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        FirebaseRepository.database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java) ?: return
                    
                    val headerView = binding.navView.getHeaderView(0)
                    val tvName = headerView.findViewById<TextView>(R.id.tvHeaderName)
                    val tvEmail = headerView.findViewById<TextView>(R.id.tvHeaderEmail)
                    
                    tvName.text = user.name
                    tvEmail.text = user.email
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // Load profile pic from Room DB
        lifecycleScope.launch {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val profile = withContext(Dispatchers.IO) {
                val db = UserProfileDatabase.getDatabase(applicationContext)
                db.userProfileDao().getProfile(currentUid)
            }
            profile?.let {
                val headerView = binding.navView.getHeaderView(0)
                val headerImageView = headerView.findViewById<ShapeableImageView>(R.id.ivHeaderProfile)
                val bytes = Base64.decode(it.profilePicBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                headerImageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = WorkoutAdapter(
            workouts = emptyList(),
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

    private fun syncWorkouts() {
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        binding.progressBar.visibility = View.VISIBLE
        binding.rvWorkouts.visibility = View.GONE

        FirebaseRepository.database.child("workouts").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    binding.rvWorkouts.visibility = View.VISIBLE
                    
                    val workoutList = mutableListOf<Workout>()
                    for (child in snapshot.children) {
                        val workout = child.getValue(Workout::class.java)
                        if (workout != null) {
                            workoutList.add(workout)
                        }
                    }
                    adapter.updateList(workoutList)
                    
                    if (workoutList.isEmpty()) {
                        binding.rvWorkouts.visibility = View.GONE
                        binding.tvEmptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvWorkouts.visibility = View.VISIBLE
                        binding.tvEmptyState.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    binding.rvWorkouts.visibility = View.VISIBLE
                    Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDeleteConfirmation(id: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Workout")
            .setMessage("Are you sure you want to delete this workout?")
            .setPositiveButton("Delete") { _, _ ->
                val uid = FirebaseRepository.getCurrentUserId()
                FirebaseRepository.database.child("workouts").child(uid).child(id)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Workout deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
