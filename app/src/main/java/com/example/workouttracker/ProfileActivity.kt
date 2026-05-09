package com.example.workouttracker

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.workouttracker.database.UserProfileDatabase
import com.example.workouttracker.data.FirebaseRepository
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

        loadUserData()
        loadProfilePicFromRoom()

        binding.ivProfilePic.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
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
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        FirebaseRepository.database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java) ?: return
                    binding.etEmail.setText(user.email)
                    binding.etName.setText(user.name)
                    binding.etAge.setText(if (user.age > 0) user.age.toString() else "")
                    
                    if (user.gender == "Male") {
                        binding.rbMale.isChecked = true
                    } else if (user.gender == "Female") {
                        binding.rbFemale.isChecked = true
                    }

                    // We no longer load profile picture from Firebase here as we use Room
                    /*
                    user.profilePicUri?.let {
                        binding.ivProfilePic.setImageURI(Uri.parse(it))
                    }
                    */
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

        val gender = if (binding.rbMale.isChecked) "Male" else "Female"
        val uid = FirebaseRepository.getCurrentUserId()
        if (uid.isEmpty()) return

        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "age" to age,
            "gender" to gender
        )
        
        // profilePicUri is NOT saved to Firebase anymore

        FirebaseRepository.database.child("users").child(uid).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
    }
}
