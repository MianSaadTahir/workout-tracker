package com.example.workouttracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.tilEmail.error = "Email is required"
                return@setOnClickListener
            } else {
                binding.tilEmail.error = null
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Password is required"
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }

            binding.progressBar.visibility = View.VISIBLE
            FirebaseRepository.auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
