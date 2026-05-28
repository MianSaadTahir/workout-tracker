package com.example.workouttracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.data.AppRepository
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

        setupTemplatesSpinner()

        binding.btnDeleteTemplate.setOnClickListener {
            val position = binding.spinnerTemplates.selectedItemPosition
            if (position > 0) {
                val templates = AppRepository.getTemplatesForCurrentUser()
                val selectedTemplate = templates[position - 1]
                deleteTemplate(selectedTemplate)
            }
        }

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

    private fun setupTemplatesSpinner() {
        val templates = AppRepository.getTemplatesForCurrentUser()
        val templateNames = mutableListOf<String>()
        templateNames.add("Select a template...")
        for (temp in templates) {
            templateNames.add(temp.name)
        }

        val tempAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, templateNames)
        tempAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTemplates.adapter = tempAdapter

        binding.spinnerTemplates.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedTemplate = templates[position - 1]
                    binding.etName.setText(selectedTemplate.name)
                    binding.etSets.setText(selectedTemplate.sets.toString())
                    binding.etReps.setText(selectedTemplate.reps.toString())
                    binding.etWeight.setText(selectedTemplate.weight.toString())
                    
                    val categoryIndex = categories.indexOf(selectedTemplate.category)
                    if (categoryIndex != -1) {
                        binding.spinnerCategory.setSelection(categoryIndex)
                    }
                    binding.btnDeleteTemplate.visibility = View.VISIBLE
                } else {
                    binding.btnDeleteTemplate.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun deleteTemplate(template: Workout) {
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        binding.progressBar.visibility = View.VISIBLE
        FirebaseRepository.database.child("templates").child(uid).child(template.id)
            .removeValue()
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Template deleted", Toast.LENGTH_SHORT).show()
                
                val templates = AppRepository.getTemplatesForCurrentUser()
                templates.removeAll { it.id == template.id }
                
                setupTemplatesSpinner()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
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

        // Validate template uniqueness by matching name AND all details (sets, reps, weight, category)
        if (binding.cbSaveAsTemplate.isChecked) {
            val trimmedName = name.trim().lowercase()
            val templates = AppRepository.getTemplatesForCurrentUser()
            val exactDuplicateExists = templates.any { 
                it.name.trim().lowercase() == trimmedName &&
                it.sets == sets &&
                it.reps == reps &&
                it.weight == weight &&
                it.category == category
            }
            if (exactDuplicateExists) {
                Toast.makeText(this, "A template with these exact details already exists", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return
        
        val ref = FirebaseRepository.database.child("workouts").child(uid)
        val id = workoutId ?: ref.push().key ?: return
        
        val workout = Workout(id, name, sets, reps, weight, category)
        binding.progressBar.visibility = View.VISIBLE
        ref.child(id).setValue(workout)
            .addOnSuccessListener {
                if (binding.cbSaveAsTemplate.isChecked) {
                    val templateRef = FirebaseRepository.database.child("templates").child(uid)
                    val templateId = templateRef.push().key ?: ""
                    if (templateId.isNotEmpty()) {
                        val template = Workout(templateId, name, sets, reps, weight, category)
                        templateRef.child(templateId).setValue(template)
                        AppRepository.getTemplatesForCurrentUser().add(template)
                    }
                }
                
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
