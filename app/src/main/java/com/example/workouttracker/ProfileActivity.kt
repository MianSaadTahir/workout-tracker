package com.example.workouttracker

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.workouttracker.database.UserProfileDatabase
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.data.AppRepository
import com.example.workouttracker.databinding.ActivityProfileBinding
import com.example.workouttracker.model.User
import com.example.workouttracker.model.UserProfileEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var selectedImageUri: Uri? = null
    private val weightUnits = arrayOf("kg", "lb")
    private val heightUnits = arrayOf("meters", "inches")

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfilePic.setImageURI(it)
            
            // Save to Room DB
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext
                    val base64 = uriToBase64(it) ?: return@withContext
                    val db = UserProfileDatabase.getDatabase(applicationContext)
                    db.userProfileDao().saveProfile(
                        UserProfileEntity(uid = uid, profilePicBase64 = base64)
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val weightAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, weightUnits)
        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWeightUnit.adapter = weightAdapter

        val heightAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, heightUnits)
        heightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerHeightUnit.adapter = heightAdapter

        binding.spinnerWeightUnit.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = weightUnits[position]
                binding.tvSelectedWeightUnit.text = "Unit: $selected"
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        binding.spinnerHeightUnit.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = heightUnits[position]
                binding.tvSelectedHeightUnit.text = "Unit: $selected"
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        loadUserData()
        loadProfilePicFromRoom()

        binding.ivProfilePic.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }

        binding.btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }

    private fun loadProfilePicFromRoom() {
        lifecycleScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val profile = withContext(Dispatchers.IO) {
                val db = UserProfileDatabase.getDatabase(applicationContext)
                db.userProfileDao().getProfile(uid)
            }
            profile?.let {
                val bitmap = base64ToBitmap(it.profilePicBase64)
                if (bitmap != null) {
                    binding.ivProfilePic.setImageBitmap(bitmap)
                }
            }
        }
    }

    // Convert URI to Base64 string
    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Compress bitmap to reduce size
            val outputStream = ByteArrayOutputStream()
            originalBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                40,              // 40% quality → small enough for SQLite
                outputStream
            )

            val compressedBytes = outputStream.toByteArray()
            Base64.encodeToString(compressedBytes, Base64.DEFAULT)

        } catch (e: Exception) {
            null
        }
    }

    // Convert Base64 string back to Bitmap
    private fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadUserData() {
        val cachedUser = AppRepository.currentUser
        if (cachedUser != null) {
            binding.etEmail.setText(cachedUser.email)
            binding.etName.setText(cachedUser.name)
            binding.etAge.setText(if (cachedUser.age > 0) cachedUser.age.toString() else "")
            
            if (cachedUser.gender == "Male") {
                binding.rbMale.isChecked = true
            } else if (cachedUser.gender == "Female") {
                binding.rbFemale.isChecked = true
            }
            binding.switchPublicProfile.isChecked = cachedUser.isPublic

            binding.etWeight.setText(if (cachedUser.weight > 0.0) cachedUser.weight.toString() else "")
            binding.etHeight.setText(if (cachedUser.height > 0.0) cachedUser.height.toString() else "")

            val wUnitIndex = weightUnits.indexOf(cachedUser.weightUnit)
            if (wUnitIndex != -1) binding.spinnerWeightUnit.setSelection(wUnitIndex)

            val hUnitIndex = heightUnits.indexOf(cachedUser.heightUnit)
            if (hUnitIndex != -1) binding.spinnerHeightUnit.setSelection(hUnitIndex)
        }

        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        FirebaseRepository.database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java) ?: return
                    
                    // Update cache
                    AppRepository.currentUser = user
                    AppRepository.registerUser(user)
                    
                    if (!binding.etEmail.isFocused) {
                        binding.etEmail.setText(user.email)
                    }
                    if (!binding.etName.isFocused) {
                        binding.etName.setText(user.name)
                    }
                    if (!binding.etAge.isFocused) {
                        binding.etAge.setText(if (user.age > 0) user.age.toString() else "")
                    }
                    
                    if (user.gender == "Male") {
                        binding.rbMale.isChecked = true
                    } else if (user.gender == "Female") {
                        binding.rbFemale.isChecked = true
                    }
                    binding.switchPublicProfile.isChecked = user.isPublic

                    if (!binding.etWeight.isFocused) {
                        binding.etWeight.setText(if (user.weight > 0.0) user.weight.toString() else "")
                    }
                    if (!binding.etHeight.isFocused) {
                        binding.etHeight.setText(if (user.height > 0.0) user.height.toString() else "")
                    }

                    val wUnitIndex = weightUnits.indexOf(user.weightUnit)
                    if (wUnitIndex != -1) binding.spinnerWeightUnit.setSelection(wUnitIndex)

                    val hUnitIndex = heightUnits.indexOf(user.heightUnit)
                    if (hUnitIndex != -1) binding.spinnerHeightUnit.setSelection(hUnitIndex)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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

        val weightStr = binding.etWeight.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()

        val weight = weightStr.toDoubleOrNull() ?: 0.0
        val height = heightStr.toDoubleOrNull() ?: 0.0

        if (weightStr.isNotEmpty() && weight <= 0.0) {
            binding.tilWeight.error = "Invalid weight"
            return
        } else {
            binding.tilWeight.error = null
        }

        if (heightStr.isNotEmpty() && height <= 0.0) {
            binding.tilHeight.error = "Invalid height"
            return
        } else {
            binding.tilHeight.error = null
        }

        val weightUnit = binding.spinnerWeightUnit.selectedItem.toString()
        val heightUnit = binding.spinnerHeightUnit.selectedItem.toString()

        val gender = if (binding.rbMale.isChecked) "Male" else "Female"
        val isPublic = binding.switchPublicProfile.isChecked
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        val bmi = com.example.workouttracker.util.BmiCalculator.calculateBmi(weight, height, weightUnit, heightUnit)

        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "age" to age,
            "gender" to gender,
            "isPublic" to isPublic,
            "weight" to weight,
            "weightUnit" to weightUnit,
            "height" to height,
            "heightUnit" to heightUnit,
            "bmi" to bmi
        )
        
        // profilePicUri is NOT saved to Firebase anymore
        binding.progressBar.visibility = View.VISIBLE
        FirebaseRepository.database.child("users").child(uid).updateChildren(updates)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                
                val cachedUser = AppRepository.currentUser
                val updatedUser = User(
                    name = name,
                    email = binding.etEmail.text.toString().trim(),
                    age = age,
                    gender = gender,
                    profilePicUri = cachedUser?.profilePicUri,
                    password = cachedUser?.password ?: "",
                    isPublic = isPublic,
                    weight = weight,
                    weightUnit = weightUnit,
                    height = height,
                    heightUnit = heightUnit,
                    bmi = bmi
                )
                AppRepository.currentUser = updatedUser
                AppRepository.registerUser(updatedUser)

                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}
