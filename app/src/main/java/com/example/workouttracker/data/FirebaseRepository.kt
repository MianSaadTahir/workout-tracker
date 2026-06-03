package com.example.workouttracker.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseRepository {
    // Nullable overrides for mocking in tests
    var authInstance: FirebaseAuth? = null
    var databaseReference: DatabaseReference? = null

    val auth: FirebaseAuth
        get() = authInstance ?: try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            throw IllegalStateException("Firebase not initialized. Set authInstance in tests.")
        }

    val database: DatabaseReference
        get() = databaseReference ?: try {
            FirebaseDatabase.getInstance("https://device-streaming-c6b78136-default-rtdb.asia-southeast1.firebasedatabase.app/").reference
        } catch (e: Exception) {
            throw IllegalStateException("Firebase not initialized. Set databaseReference in tests.")
        }

    val currentUser: FirebaseUser? get() = auth.currentUser
    
    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""
    
    fun isLoggedIn(): Boolean = auth.currentUser != null
    
    fun logout() = auth.signOut()
}
