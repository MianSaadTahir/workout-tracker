package com.example.workouttracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.AppRepository
import com.example.workouttracker.databinding.ActivitySignUpBinding
import com.example.workouttracker.model.User

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            if (validateForm()) {
                val name = binding.etName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                val gender = if (binding.rbMale.isChecked) "Male" else "Female"

                val newUser = User(name, email, password, 0, gender)
                if (AppRepository.registerUser(newUser)) {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Name is required"
            isValid = false
        } else {
            binding.tilName.error = null
        }

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            isValid = false
        } else if (!password.any { it.isUpperCase() } || !password.any { it.isLowerCase() } || !password.any { it.isDigit() }) {
            binding.tilPassword.error = "Must contain uppercase, lowercase and digit"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }
}
