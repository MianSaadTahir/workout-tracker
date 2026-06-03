package com.example.workouttracker

import android.Manifest
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.workouttracker.model.Workout
import com.example.workouttracker.data.FirebaseRepository
import com.example.workouttracker.data.MainData
import com.example.workouttracker.data.WeatherData
import com.example.workouttracker.data.WeatherApiService
import com.example.workouttracker.data.WeatherResponse
import com.example.workouttracker.data.WeatherRetrofitClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyDouble
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

/**
 * DashboardActivityTest
 * Covers integration tests for the Dashboard (MainActivity):
 * - Activity launches without crash.
 * - Weather card view is displayed.
 * - Stats overview card (summaryCard) is visible.
 * - RecyclerView for workouts is present in the layout.
 */
@RunWith(AndroidJUnit4::class)
class DashboardActivityTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockDatabaseRef: DatabaseReference
    private lateinit var mockWeatherService: WeatherApiService

    @Before
    fun setup() {
        // Bypass runtime permission request
        MainActivity.permissionOverride = android.content.pm.PackageManager.PERMISSION_GRANTED

        // Mock Firebase Auth and User
        mockAuth = mock(FirebaseAuth::class.java)
        mockUser = mock(FirebaseUser::class.java)
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("test_uid")
        `when`(mockUser.email).thenReturn("test@example.com")
        FirebaseRepository.authInstance = mockAuth

        // Mock Firebase Database reference with chaining support
        mockDatabaseRef = mock(DatabaseReference::class.java)
        `when`(mockDatabaseRef.child(anyString())).thenReturn(mockDatabaseRef)
        
        // Mock the single value event listener for syncWorkouts (returning 1 workout)
        `when`(mockDatabaseRef.addListenerForSingleValueEvent(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            val mockSnapshot = mock(DataSnapshot::class.java)
            
            val mockWorkoutSnapshot = mock(DataSnapshot::class.java)
            `when`(mockWorkoutSnapshot.getValue(Workout::class.java)).thenReturn(
                Workout(
                    id = "workout_1",
                    name = "Pushups",
                    sets = 3,
                    reps = 10,
                    weight = 0.0,
                    category = "Strength",
                    timestamp = System.currentTimeMillis()
                )
            )
            
            `when`(mockSnapshot.children).thenReturn(listOf(mockWorkoutSnapshot))
            listener.onDataChange(mockSnapshot)
            null
        }
        FirebaseRepository.databaseReference = mockDatabaseRef

        // Mock Weather API Service to avoid real network calls
        mockWeatherService = mock(WeatherApiService::class.java)
        WeatherRetrofitClient.serviceOverride = mockWeatherService
    }

    @After
    fun teardown() {
        MainActivity.permissionOverride = null
        FirebaseRepository.authInstance = null
        FirebaseRepository.databaseReference = null
        WeatherRetrofitClient.serviceOverride = null
    }

    @Test
    fun testDashboardLaunchesAndDisplaysViews() {
        runBlocking {
            // Given a mock weather response
            val mockResponse = WeatherResponse(
                main = MainData(temp = 24.0),
                weather = listOf(WeatherData(main = "Clear", description = "pleasant weather", icon = "01d")),
                name = "Lahore"
            )
            // Stub the suspend functions of WeatherApiService
            `when`(mockWeatherService.getWeatherByCity(anyString(), anyString(), anyString())).thenReturn(mockResponse)
            `when`(mockWeatherService.getWeatherByCoordinates(anyDouble(), anyDouble(), anyString(), anyString())).thenReturn(mockResponse)

            // Launch MainActivity
            ActivityScenario.launch(MainActivity::class.java).use {
                // Verify activity launches and views are displayed
                onView(withId(R.id.summaryCard)).check(matches(isDisplayed()))
                onView(withId(R.id.weatherCard)).check(matches(isDisplayed()))
                onView(withId(R.id.rvWorkouts)).check(matches(isDisplayed()))
            }
        }
    }
}
