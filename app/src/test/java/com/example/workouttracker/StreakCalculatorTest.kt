package com.example.workouttracker

import com.example.workouttracker.model.Workout
import com.example.workouttracker.util.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * StreakCalculatorTest
 * Covers unit tests for consecutive day streak logic:
 * - Empty list returns 0.
 * - Single day workout returns 1.
 * - Multiple consecutive days returns correct count (e.g., 3).
 * - Broken streak (gap) resets the active streak to 1 (if today/yesterday is active).
 * - Timezone-safe date comparison using custom calendars with specific timezones.
 */
class StreakCalculatorTest {

    private fun createWorkout(timestamp: Long): Workout {
        return Workout(
            id = "test_id",
            name = "Pushups",
            sets = 3,
            reps = 10,
            weight = 0.0,
            category = "Strength",
            timestamp = timestamp
        )
    }

    @Test
    fun testEmptyListReturnsZero() {
        val workouts = emptyList<Workout>()
        val streak = StreakCalculator.calculateStreak(workouts)
        assertEquals(0, streak)
    }

    @Test
    fun testSingleDayStreak() {
        val anchorCalendar = Calendar.getInstance()
        // Workout is exactly today
        val todayWorkout = createWorkout(anchorCalendar.timeInMillis)
        
        val streak = StreakCalculator.calculateStreak(listOf(todayWorkout), anchorCalendar)
        assertEquals(1, streak)
    }

    @Test
    fun testMultipleConsecutiveDays() {
        val anchorCalendar = Calendar.getInstance()
        
        val today = anchorCalendar.timeInMillis
        
        val yesterdayCal = anchorCalendar.clone() as Calendar
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = yesterdayCal.timeInMillis

        val twoDaysAgoCal = anchorCalendar.clone() as Calendar
        twoDaysAgoCal.add(Calendar.DAY_OF_YEAR, -2)
        val twoDaysAgo = twoDaysAgoCal.timeInMillis

        val workouts = listOf(
            createWorkout(today),
            createWorkout(yesterday),
            createWorkout(twoDaysAgo)
        )

        val streak = StreakCalculator.calculateStreak(workouts, anchorCalendar)
        assertEquals(3, streak)
    }

    @Test
    fun testBrokenStreakResetsToOne() {
        val anchorCalendar = Calendar.getInstance()
        
        val today = anchorCalendar.timeInMillis
        
        // Skip yesterday
        
        val twoDaysAgoCal = anchorCalendar.clone() as Calendar
        twoDaysAgoCal.add(Calendar.DAY_OF_YEAR, -2)
        val twoDaysAgo = twoDaysAgoCal.timeInMillis

        val workouts = listOf(
            createWorkout(today),
            createWorkout(twoDaysAgo)
        )

        val streak = StreakCalculator.calculateStreak(workouts, anchorCalendar)
        assertEquals(1, streak) // Resets to 1 (only counts today, gap at yesterday)
    }

    @Test
    fun testStreakWithWorkoutOnlyYesterday() {
        val anchorCalendar = Calendar.getInstance()
        
        val yesterdayCal = anchorCalendar.clone() as Calendar
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = yesterdayCal.timeInMillis

        val twoDaysAgoCal = anchorCalendar.clone() as Calendar
        twoDaysAgoCal.add(Calendar.DAY_OF_YEAR, -2)
        val twoDaysAgo = twoDaysAgoCal.timeInMillis

        val workouts = listOf(
            createWorkout(yesterday),
            createWorkout(twoDaysAgo)
        )

        val streak = StreakCalculator.calculateStreak(workouts, anchorCalendar)
        assertEquals(2, streak)
    }

    @Test
    fun testBrokenStreakInactiveReturnsZero() {
        val anchorCalendar = Calendar.getInstance()
        
        // Skip today and yesterday
        
        val twoDaysAgoCal = anchorCalendar.clone() as Calendar
        twoDaysAgoCal.add(Calendar.DAY_OF_YEAR, -2)
        val twoDaysAgo = twoDaysAgoCal.timeInMillis

        val workouts = listOf(
            createWorkout(twoDaysAgo)
        )

        val streak = StreakCalculator.calculateStreak(workouts, anchorCalendar)
        assertEquals(0, streak)
    }

    @Test
    fun testTimezoneSafeDateComparison() {
        // Using two different time zones: Tokyo (GMT+9) and New York (GMT-5)
        // A timestamp might be day D in Tokyo, but day D-1 in New York.
        
        val tokyoTz = TimeZone.getTimeZone("Asia/Tokyo")
        val nyTz = TimeZone.getTimeZone("America/New_York")
        
        // Anchor at a specific time: 2026-06-03 02:00:00 UTC
        val baseCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.JUNE, 3, 2, 0, 0)
        }
        val timestamp = baseCal.timeInMillis // 2026-06-03 02:00:00 UTC
        
        // In Tokyo (GMT+9), this time is 2026-06-03 11:00:00 AM (Wednesday)
        // In New York (GMT-5), this time is 2026-06-02 09:00:00 PM (Tuesday)
        
        val tokyoCalendar = Calendar.getInstance(tokyoTz).apply {
            timeInMillis = timestamp
        }
        val nyCalendar = Calendar.getInstance(nyTz).apply {
            timeInMillis = timestamp
        }
        
        // Workout timestamp is 2026-06-03 02:00:00 UTC (Tokyo: June 3, New York: June 2)
        val workouts = listOf(createWorkout(timestamp))
        
        // If Tokyo anchor is June 3 (today for Tokyo): streak should be 1
        val tokyoStreak = StreakCalculator.calculateStreak(workouts, tokyoCalendar)
        assertEquals(1, tokyoStreak)
        
        // If New York anchor is June 2 (today for NY): streak should be 1
        val nyStreak = StreakCalculator.calculateStreak(workouts, nyCalendar)
        assertEquals(1, nyStreak)
    }
}
