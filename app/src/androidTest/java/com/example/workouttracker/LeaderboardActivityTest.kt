package com.example.workouttracker

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * LeaderboardActivityTest
 * Covers integration tests for LeaderboardActivity:
 * - Activity launches without crash.
 * - chipGroupSort (chip filter group) is displayed.
 * - rvLeaderboard (RecyclerView) is present in the layout.
 */
@RunWith(AndroidJUnit4::class)
class LeaderboardActivityTest {

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
        val usersRef = mock(DatabaseReference::class.java)
        val workoutsRef = mock(DatabaseReference::class.java)

        `when`(mockDatabaseRef.child("users")).thenReturn(usersRef)
        `when`(mockDatabaseRef.child("workouts")).thenReturn(workoutsRef)
        `when`(usersRef.child(anyString())).thenReturn(usersRef)
        `when`(workoutsRef.child(anyString())).thenReturn(workoutsRef)

        // Stub the single value event listener for users query
        `when`(usersRef.addListenerForSingleValueEvent(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            
            // Create a mock user who is public
            val mockUserSnapshot = mock(DataSnapshot::class.java)
            `when`(mockUserSnapshot.key).thenReturn("user_1")
            `when`(mockUserSnapshot.getValue(User::class.java)).thenReturn(
                User(email = "user1@example.com", name = "User One", isPublic = true)
            )
            
            val mockSnapshot = mock(DataSnapshot::class.java)
            `when`(mockSnapshot.children).thenReturn(listOf(mockUserSnapshot))
            listener.onDataChange(mockSnapshot)
            null
        }

        // Stub the single value event listener for workouts query
        `when`(workoutsRef.addListenerForSingleValueEvent(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            val mockSnapshot = mock(DataSnapshot::class.java)
            `when`(mockSnapshot.children).thenReturn(emptyList())
            listener.onDataChange(mockSnapshot)
            null
        }
        FirebaseRepository.databaseReference = mockDatabaseRef
    }

    @After
    fun teardown() {
        FirebaseRepository.authInstance = null
        FirebaseRepository.databaseReference = null
    }

    @Test
    fun testLeaderboardLaunchesAndDisplaysViews() {
        ActivityScenario.launch(LeaderboardActivity::class.java).use {
            onView(withId(R.id.chipGroupSort)).check(matches(isDisplayed()))
            onView(withId(R.id.rvLeaderboard)).check(matches(isDisplayed()))
        }
    }
}
