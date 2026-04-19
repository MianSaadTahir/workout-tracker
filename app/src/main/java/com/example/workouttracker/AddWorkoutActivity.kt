package com.example.workouttracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.WorkoutRepository
import com.example.workouttracker.databinding.ActivityAddWorkoutBinding
import com.example.workouttracker.model.Workout

class AddWorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWorkoutBinding
    private var workoutId: Int = -1
    private val categories = arrayOf("Chest", "Back", "Legs", "Biceps", "Triceps", "Shoulders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        workoutId = intent.getIntExtra("WORKOUT_ID", -1)
        if (workoutId != -1) {
            setupEditMode()
        }

        binding.btnSave.setOnClickListener {
            saveWorkout()
        }
    }

    private fun setupEditMode() {
        binding.tvTitle.text = "Edit Workout"
        val workout = WorkoutRepository.getWorkoutById(workoutId)
        workout?.let {
            binding.etName.setText(it.name)
            binding.etSets.setText(it.sets.toString())
            binding.etReps.setText(it.reps.toString())
            binding.etWeight.setText(it.weight.toString())
            
            val categoryIndex = categories.indexOf(it.category)
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

        val workout = Workout(workoutId, name, sets, reps, weight, category)
        WorkoutRepository.addWorkout(workout)

        Toast.makeText(this, "Workout Saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
