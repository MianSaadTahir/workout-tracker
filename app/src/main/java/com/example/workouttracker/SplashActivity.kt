package com.example.workouttracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.workouttracker.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Clear any leftover mock overrides from test runs to restore production services
        com.example.workouttracker.data.FirebaseRepository.authInstance = null
        com.example.workouttracker.data.FirebaseRepository.databaseReference = null
        com.example.workouttracker.data.WeatherRetrofitClient.serviceOverride = null
        MainActivity.permissionOverride = null

        try {
            android.util.Log.d("FIREBASE_TEST", "auth=${com.google.firebase.auth.FirebaseAuth.getInstance().app.name}")
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_TEST", "Firebase initialization failed: ", e)
        }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Delayed navigation after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, AuthActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
