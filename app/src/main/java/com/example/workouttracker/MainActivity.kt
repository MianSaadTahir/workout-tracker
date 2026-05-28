package com.example.workouttracker

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workouttracker.adapter.WorkoutAdapter
import com.example.workouttracker.database.UserProfileDatabase
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.data.AppRepository
import com.example.workouttracker.data.WorkoutRepository
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
    private var searchItem: MenuItem? = null

    private val addWorkoutLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            syncWorkouts()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarAndDrawer()
        setupRecyclerView()
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            syncWorkouts()
        }
        
        syncWorkouts()

        binding.btnAddWorkout.setOnClickListener {
            val intent = Intent(this, AddWorkoutActivity::class.java)
            addWorkoutLauncher.launch(intent)
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
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_summary -> {
                    startActivity(Intent(this, SummaryActivity::class.java))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
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

    override fun onPause() {
        super.onPause()
        searchItem?.collapseActionView()
    }

    private fun refreshDrawerHeader() {
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        FirebaseRepository.database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java) ?: return
                    
                    // Cache the user profile
                    AppRepository.currentUser = user
                    AppRepository.registerUser(user)
                    
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
                addWorkoutLauncher.launch(intent)
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
        if (uid.isEmpty()) {
            binding.swipeRefreshLayout.isRefreshing = false
            return
        }

        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
            binding.rvWorkouts.visibility = View.GONE
        }

        // Fetch and cache templates in background
        FirebaseRepository.database.child("templates").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val templateList = mutableListOf<Workout>()
                    for (child in snapshot.children) {
                        val template = child.getValue(Workout::class.java)
                        if (template != null) {
                            templateList.add(template)
                        }
                    }
                    val email = FirebaseRepository.auth.currentUser?.email ?: ""
                    if (email.isNotEmpty()) {
                        if (AppRepository.currentUser == null || AppRepository.currentUser?.email != email) {
                            AppRepository.currentUser = User(email = email)
                        }
                        AppRepository.currentUser?.let { user ->
                            AppRepository.registerUser(user)
                            val cacheTemplates = AppRepository.getTemplatesForCurrentUser()
                            cacheTemplates.clear()
                            cacheTemplates.addAll(templateList)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        FirebaseRepository.database.child("workouts").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar.visibility = View.GONE
                    binding.rvWorkouts.visibility = View.VISIBLE
                    binding.swipeRefreshLayout.isRefreshing = false
                    
                    val workoutList = mutableListOf<Workout>()
                    for (child in snapshot.children) {
                        val workout = child.getValue(Workout::class.java)
                        if (workout != null) {
                            workoutList.add(workout)
                        }
                    }
                    workoutList.reverse()
                    
                    // Update memory cache
                    val email = FirebaseRepository.auth.currentUser?.email ?: ""
                    if (email.isNotEmpty()) {
                        if (AppRepository.currentUser == null || AppRepository.currentUser?.email != email) {
                            AppRepository.currentUser = User(email = email)
                        }
                        AppRepository.currentUser?.let { user ->
                            AppRepository.registerUser(user)
                            val cacheList = AppRepository.getWorkoutsForCurrentUser()
                            cacheList.clear()
                            cacheList.addAll(workoutList)
                        }
                    }
                    
                    // Calculate summary metrics dynamically
                    val totalCount = workoutList.size
                    val thisWeekCount = workoutList.size
                    val caloriesSum = workoutList.sumOf { it.sets * it.reps * it.weight } * 0.1
                    val calStr = if (caloriesSum % 1 == 0.0) caloriesSum.toInt().toString() else String.format("%.1f", caloriesSum)
                    
                    binding.tvSummaryTotal.text = totalCount.toString()
                    binding.tvSummaryThisWeek.text = thisWeekCount.toString()
                    binding.tvSummaryCalories.text = calStr
                    
                    filterWorkouts(getCurrentSearchQuery())
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    binding.rvWorkouts.visibility = View.VISIBLE
                    binding.swipeRefreshLayout.isRefreshing = false
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
                
                // Show spinner while deleting
                binding.progressBar.visibility = View.VISIBLE
                
                FirebaseRepository.database.child("workouts").child(uid).child(id)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Workout deleted", Toast.LENGTH_SHORT).show()
                        
                        // Instantly update memory cache
                        WorkoutRepository.deleteWorkout(id)
                        val updatedList = WorkoutRepository.getAllWorkouts()
                        
                        // Recalculate dashboard metrics dynamically
                        val totalCount = updatedList.size
                        val caloriesSum = updatedList.sumOf { it.sets * it.reps * it.weight } * 0.1
                        val calStr = if (caloriesSum % 1 == 0.0) caloriesSum.toInt().toString() else String.format("%.1f", caloriesSum)
                        
                        binding.tvSummaryTotal.text = totalCount.toString()
                        binding.tvSummaryThisWeek.text = totalCount.toString()
                        binding.tvSummaryCalories.text = calStr
                        
                        filterWorkouts(getCurrentSearchQuery())
                        binding.progressBar.visibility = View.GONE
                    }
                    .addOnFailureListener {
                        binding.progressBar.visibility = View.GONE
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView
        
        searchView?.queryHint = "Search exercises..."
        searchView?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterWorkouts(newText ?: "")
                return true
            }
        })

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean = true

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                filterWorkouts("") // Reset filter on collapse
                return true
            }
        })

        return true
    }

    private fun getCurrentSearchQuery(): String {
        val searchView = searchItem?.actionView as? androidx.appcompat.widget.SearchView
        return if (searchItem?.isActionViewExpanded == true) searchView?.query?.toString() ?: "" else ""
    }

    private fun filterWorkouts(query: String) {
        val trimmedQuery = query.trim().lowercase()
        val allWorkouts = WorkoutRepository.getAllWorkouts()
        val filteredList = if (trimmedQuery.isEmpty()) {
            allWorkouts
        } else {
            allWorkouts.filter { it.name.trim().lowercase().contains(trimmedQuery) }
        }
        adapter.updateList(filteredList)

        if (filteredList.isEmpty()) {
            binding.rvWorkouts.visibility = View.GONE
            binding.tvEmptyState.text = if (trimmedQuery.isEmpty()) "No workouts added yet. Start your journey!" else "No workouts found matching \"$query\""
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvWorkouts.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }
}
