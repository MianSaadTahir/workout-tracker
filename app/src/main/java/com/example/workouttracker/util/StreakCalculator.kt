package com.example.workouttracker.util

import com.example.workouttracker.model.Workout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility object for consecutive day workout streaks.
 * Timezone-safe date comparison via customized anchor calendars.
 */
object StreakCalculator {
    fun calculateStreak(workouts: List<Workout>, anchorCalendar: Calendar = Calendar.getInstance()): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = anchorCalendar.timeZone
        }
        val uniqueDays = workouts
            .filter { it.timestamp > 0L }
            .map { sdf.format(Date(it.timestamp)) }
            .toSet()

        if (uniqueDays.isEmpty()) return 0

        val todayStr = sdf.format(anchorCalendar.time)

        val yesterdayCal = anchorCalendar.clone() as Calendar
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(yesterdayCal.time)

        if (!uniqueDays.contains(todayStr) && !uniqueDays.contains(yesterdayStr)) {
            return 0
        }

        val checkCalendar = if (uniqueDays.contains(todayStr)) {
            anchorCalendar.clone() as Calendar
        } else {
            val cal = anchorCalendar.clone() as Calendar
            cal.add(Calendar.DAY_OF_YEAR, -1)
            cal
        }

        var streak = 0
        while (true) {
            val dateStr = sdf.format(checkCalendar.time)
            if (uniqueDays.contains(dateStr)) {
                streak++
                checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }
}
