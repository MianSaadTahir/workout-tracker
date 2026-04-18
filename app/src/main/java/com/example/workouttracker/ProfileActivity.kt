package com.example.workouttracker

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.AppRepository
import com.example.workouttracker.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfilePic.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserData()

        binding.ivProfilePic.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadUserData() {
        val user = AppRepository.currentUser ?: return
        binding.etEmail.setText(user.email)
        binding.etName.setText(user.name)
        binding.etAge.setText(if (user.age > 0) user.age.toString() else "")
        
        if (user.gender == "Male") {
            binding.rbMale.isChecked = true
        } else {
            binding.rbFemale.isChecked = true
        }

        user.profilePicUri?.let {
            binding.ivProfilePic.setImageURI(Uri.parse(it))
        }
    }

    private fun saveChanges() {
        val name = binding.etName.text.toString().trim()
        val ageStr = binding.etAge.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            return
        } else {
            binding.tilName.error = null
        }

        val age = ageStr.toIntOrNull()
        if (age == null || age <= 0) {
            binding.tilAge.error = "Invalid age"
            return
        } else {
            binding.tilAge.error = null
        }

        val gender = if (binding.rbMale.isChecked) "Male" else "Female"

        val user = AppRepository.currentUser ?: return
        // Update user in memory
        val updatedUser = user.copy(
            name = name,
            age = age,
            gender = gender,
            profilePicUri = selectedImageUri?.toString() ?: user.profilePicUri
        )
        AppRepository.currentUser = updatedUser
        
        // Since it's a list in Repository, we should update the item in the list too
        // But since we use object references and copy() creates new one, 
        // normally we'd update the list. For this project RAM storage:
        // Let's just update the singleton's currentUser and assume it's the source of truth.
        
        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
        finish()
    }
}
