package com.example.workouttracker

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.AppRepository
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpdatePassword.setOnClickListener {
            if (validateForm()) {
                performPasswordUpdate()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val currentPassword = binding.etCurrentPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // 1. Current Password Validation
        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.error = "Current password is required"
            isValid = false
        } else {
            binding.tilCurrentPassword.error = null
        }

        // 2. New Password Validation
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = "New password is required"
            isValid = false
        } else if (newPassword.length < 6) {
            binding.tilNewPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else if (!newPassword.any { it.isUpperCase() } || !newPassword.any { it.isLowerCase() } || !newPassword.any { it.isDigit() }) {
            binding.tilNewPassword.error = "Must contain uppercase, lowercase and digit"
            isValid = false
        } else if (currentPassword.isNotEmpty() && newPassword == currentPassword) {
            binding.tilNewPassword.error = "New password cannot be the same as current password"
            isValid = false
        } else {
            binding.tilNewPassword.error = null
        }

        // 3. Confirm Password Validation
        if (confirmPassword != newPassword) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun performPasswordUpdate() {
        val currentPassword = binding.etCurrentPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()

        val user = FirebaseRepository.auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val email = user.email
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpdatePassword.isEnabled = false

        // Step 1: Reauthenticate the user with their current password
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Step 2: Reauthentication succeeded, update password in Firebase Auth
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        // Step 3: Update password in Firebase Database to keep User model aligned
                        val uid = user.uid
                        FirebaseRepository.database.child("users").child(uid).child("password").setValue(newPassword)
                            .addOnCompleteListener {
                                binding.progressBar.visibility = View.GONE
                                binding.btnUpdatePassword.isEnabled = true

                                // Step 4: Update local cache
                                val cachedUser = AppRepository.currentUser
                                if (cachedUser != null) {
                                    val updatedUser = cachedUser.copy(password = newPassword)
                                    AppRepository.currentUser = updatedUser
                                    AppRepository.registerUser(updatedUser)
                                }

                                Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                    }
                    .addOnFailureListener { e ->
                        binding.progressBar.visibility = View.GONE
                        binding.btnUpdatePassword.isEnabled = true
                        Toast.makeText(this, "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnUpdatePassword.isEnabled = true
                binding.tilCurrentPassword.error = "Incorrect current password"
                Toast.makeText(this, "Authentication failed. Please verify your current password.", Toast.LENGTH_SHORT).show()
            }
    }
}
