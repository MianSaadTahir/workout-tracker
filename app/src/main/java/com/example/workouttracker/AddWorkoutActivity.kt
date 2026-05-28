package com.example.workouttracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.data.WorkoutRepository
import com.example.workouttracker.databinding.ActivityAddWorkoutBinding
import com.example.workouttracker.model.Workout
import android.view.View

class AddWorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWorkoutBinding
    private var workoutId: String? = null
    private val categories = arrayOf("Chest", "Back", "Legs", "Biceps", "Triceps", "Shoulders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        workoutId = intent.getStringExtra("WORKOUT_ID")
        if (workoutId != null) {
            setupEditMode()
        }

        binding.btnSave.setOnClickListener {
            saveWorkout()
        }
    }

    private fun setupEditMode() {
        binding.tvTitle.text = "Edit Workout"
        val id = workoutId ?: return
        
        val workout = WorkoutRepository.getWorkoutById(id)
        if (workout != null) {
            binding.etName.setText(workout.name)
            binding.etSets.setText(workout.sets.toString())
            binding.etReps.setText(workout.reps.toString())
            binding.etWeight.setText(workout.weight.toString())
            
            val categoryIndex = categories.indexOf(workout.category)
            if (categoryIndex != -1) {
                binding.spinnerCategory.setSelection(categoryIndex)
            }
        }
    }

    private fun saveWorkout() {
        val name = binding.etName.text.toString().trim()
        val setsStr = binding.etSets.text.toString().trim()
        val repsStr = binding.etReps.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()

        if (name.isEmpty() || setsStr.isEmpty() || repsStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val sets = setsStr.toIntOrNull() ?: 0
        val reps = repsStr.toIntOrNull() ?: 0
        val weight = weightStr.toDoubleOrNull() ?: 0.0

        val category = binding.spinnerCategory.selectedItem.toString()

        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return
        
        val ref = FirebaseRepository.database.child("workouts").child(uid)
        val id = workoutId ?: ref.push().key ?: return
        
        val workout = Workout(id, name, sets, reps, weight, category)
        binding.progressBar.visibility = View.VISIBLE
        ref.child(id).setValue(workout)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Workout Saved", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}
