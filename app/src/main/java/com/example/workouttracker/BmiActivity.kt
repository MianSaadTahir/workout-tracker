package com.example.workouttracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.data.AppRepository
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.databinding.ActivityBmiBinding
import com.example.workouttracker.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BmiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBmiBinding

    private val updateProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        loadUserData(forceReload = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        val openProfileClick = View.OnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            updateProfileLauncher.launch(intent)
        }

        binding.btnUpdateProfile.setOnClickListener(openProfileClick)
        binding.btnRecalculate.setOnClickListener(openProfileClick)

        binding.swipeRefreshLayout.setOnRefreshListener {
            loadUserData(forceReload = true)
        }

        loadUserData(forceReload = false)
    }

    private fun loadUserData(forceReload: Boolean = false) {
        val cachedUser = AppRepository.currentUser
        if (!forceReload && cachedUser != null && cachedUser.weight > 0.0 && cachedUser.height > 0.0) {
            displayBmi(cachedUser, saveToDb = false)
            return
        }

        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        if (!binding.swipeRefreshLayout.isRefreshing) {
            binding.progressBar.visibility = View.VISIBLE
        }
        binding.layoutMissingDetails.visibility = View.GONE
        binding.layoutBmiResult.visibility = View.GONE

        FirebaseRepository.database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        AppRepository.currentUser = user
                        AppRepository.registerUser(user)
                        displayBmi(user, saveToDb = true)
                    } else {
                        hideLoadingViews()
                        binding.layoutMissingDetails.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    hideLoadingViews()
                    binding.layoutMissingDetails.visibility = View.VISIBLE
                    Toast.makeText(this@BmiActivity, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun hideLoadingViews() {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun displayBmi(user: User, saveToDb: Boolean) {
        val weight = user.weight
        val height = user.height
        val weightUnit = user.weightUnit
        val heightUnit = user.heightUnit

        if (weight <= 0.0 || height <= 0.0) {
            binding.layoutMissingDetails.visibility = View.VISIBLE
            binding.layoutBmiResult.visibility = View.GONE
            hideLoadingViews()
            return
        }

        // Convert weight to kg
        val weightKg = if (weightUnit == "lb") {
            weight * 0.45359237
        } else {
            weight
        }

        // Convert height to meters
        val heightMeters = if (heightUnit == "inches") {
            height * 0.0254
        } else {
            height
        }

        if (heightMeters <= 0.0) {
            binding.layoutMissingDetails.visibility = View.VISIBLE
            binding.layoutBmiResult.visibility = View.GONE
            hideLoadingViews()
            return
        }

        val bmi = weightKg / (heightMeters * heightMeters)

        // Save calculated BMI to Firebase under users/$uid/bmi
        if (saveToDb) {
            val uid = FirebaseRepository.getCurrentUserId()
            if (uid.isNotEmpty()) {
                FirebaseRepository.database.child("users").child(uid).child("bmi").setValue(bmi)
            }
        }

        binding.tvBmiScore.text = String.format("%.2f", bmi)
        binding.tvBmiMetricsSummary.text = "Height: $height $heightUnit | Weight: $weight $weightUnit"

        val category: String
        val colorHex: String
        val adviceTitle: String
        val adviceDesc: String

        when {
            bmi < 18.5 -> {
                category = "Underweight"
                colorHex = "#FFC107" // Amber
                adviceTitle = "Underweight Range"
                adviceDesc = "Your Body Mass Index is in the underweight range. Focus on consuming a balanced diet rich in nutrient-dense foods and consult a healthcare provider or nutritionist for guidance."
            }
            bmi < 25.0 -> {
                category = "Normal Weight"
                colorHex = "#4CAF50" // Green
                adviceTitle = "Healthy Weight Range"
                adviceDesc = "Great job! Your Body Mass Index is within the normal and healthy weight range. Keep up your active lifestyle and nutritious eating habits to maintain this balance!"
            }
            bmi < 30.0 -> {
                category = "Overweight"
                colorHex = "#FF9800" // Orange
                adviceTitle = "Overweight Range"
                adviceDesc = "Your Body Mass Index is in the overweight range. Consider incorporating regular physical activity (such as your tracked workouts!) and adjusting your caloric intake for healthy weight management."
            }
            else -> {
                category = "Obese"
                colorHex = "#F44336" // Red
                adviceTitle = "Obese Range"
                adviceDesc = "Your Body Mass Index is in the obese range. Prioritizing consistent workout routines, portion control, and consulting a healthcare professional is recommended for support on your wellness journey."
            }
        }

        binding.tvBmiCategory.text = category
        binding.tvBmiCategory.setTextColor(Color.parseColor(colorHex))
        binding.tvAdviceTitle.text = adviceTitle
        binding.tvAdviceDescription.text = adviceDesc

        binding.layoutMissingDetails.visibility = View.GONE
        binding.layoutBmiResult.visibility = View.VISIBLE
        hideLoadingViews()
    }
}
