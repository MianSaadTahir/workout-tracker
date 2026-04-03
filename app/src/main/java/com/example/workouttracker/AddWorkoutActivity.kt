package com.example.workouttracker

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.WorkoutRepository
import com.example.workouttracker.databinding.ActivityAddWorkoutBinding
import com.example.workouttracker.model.Workout

class AddWorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWorkoutBinding
    private var workoutId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            
            when (it.category) {
                "Chest" -> binding.rbChest.isChecked = true
                "Legs" -> binding.rbLegs.isChecked = true
                "Back" -> binding.rbBack.isChecked = true
                "Arms" -> binding.rbArms.isChecked = true
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

        val category = when (binding.rgCategory.checkedRadioButtonId) {
            binding.rbChest.id -> "Chest"
            binding.rbLegs.id -> "Legs"
            binding.rbBack.id -> "Back"
            binding.rbArms.id -> "Arms"
            else -> "Chest"
        }

        val workout = Workout(workoutId, name, sets, reps, weight, category)
        WorkoutRepository.addWorkout(workout)

        Toast.makeText(this, "Workout Saved", Toast.LENGTH_SHORT).show()
        finish()
    }
}
