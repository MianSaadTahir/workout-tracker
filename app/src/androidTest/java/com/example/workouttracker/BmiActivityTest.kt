package com.example.workouttracker

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.workouttracker.data.AppRepository
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * BmiActivityTest
 * Covers integration tests for BmiActivity:
 * - When height/weight are missing/zero: layoutMissingDetails is visible, layoutBmiResult is not.
 * - When valid data exists: layoutBmiResult is visible.
 */
@RunWith(AndroidJUnit4::class)
class BmiActivityTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockDatabaseRef: DatabaseReference

    @Before
    fun setup() {
        mockAuth = mock(FirebaseAuth::class.java)
        mockUser = mock(FirebaseUser::class.java)
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("test_uid")
        `when`(mockUser.email).thenReturn("test@example.com")
        FirebaseRepository.authInstance = mockAuth

        mockDatabaseRef = mock(DatabaseReference::class.java)
        `when`(mockDatabaseRef.child(anyString())).thenReturn(mockDatabaseRef)
        FirebaseRepository.databaseReference = mockDatabaseRef
        
        AppRepository.currentUser = null
    }

    @After
    fun teardown() {
        FirebaseRepository.authInstance = null
        FirebaseRepository.databaseReference = null
        AppRepository.currentUser = null
    }

    @Test
    fun testBmiMissingDetailsState() {
        // Setup Firebase to return a user with missing details
        val user = User(email = "test@example.com", name = "Test User", weight = 0.0, height = 0.0)
        `when`(mockDatabaseRef.addListenerForSingleValueEvent(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            val mockSnapshot = mock(DataSnapshot::class.java)
            `when`(mockSnapshot.getValue(User::class.java)).thenReturn(user)
            listener.onDataChange(mockSnapshot)
            null
        }

        ActivityScenario.launch(BmiActivity::class.java).use {
            onView(withId(R.id.layoutMissingDetails)).check(matches(isDisplayed()))
            onView(withId(R.id.layoutBmiResult)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun testBmiValidState() {
        // Setup Firebase/Cache to return a user with valid details
        val user = User(
            email = "test@example.com",
            name = "Test User",
            weight = 70.0,
            height = 1.75,
            weightUnit = "kg",
            heightUnit = "meters"
        )
        AppRepository.currentUser = user
        
        `when`(mockDatabaseRef.addListenerForSingleValueEvent(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            val mockSnapshot = mock(DataSnapshot::class.java)
            `when`(mockSnapshot.getValue(User::class.java)).thenReturn(user)
            listener.onDataChange(mockSnapshot)
            null
        }

        ActivityScenario.launch(BmiActivity::class.java).use {
            onView(withId(R.id.layoutBmiResult)).check(matches(isDisplayed()))
            onView(withId(R.id.layoutMissingDetails)).check(matches(not(isDisplayed())))
        }
    }
}
