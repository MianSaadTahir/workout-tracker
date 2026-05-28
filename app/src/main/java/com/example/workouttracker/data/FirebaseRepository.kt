package com.example.workouttracker.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

object FirebaseRepository {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance("https://device-streaming-c6b78136-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
    val currentUser: FirebaseUser? get() = auth.currentUser
    
    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""
    
    fun isLoggedIn(): Boolean = auth.currentUser != null
    
    fun logout() = auth.signOut()
}
