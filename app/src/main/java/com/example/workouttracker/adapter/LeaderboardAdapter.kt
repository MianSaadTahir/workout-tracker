package com.example.workouttracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workouttracker.databinding.ItemLeaderboardBinding

data class LeaderboardUser(
    val uid: String,
    val name: String,
    val streak: Int,
    val workoutsCount: Int,
    val caloriesBurned: Double
)

enum class LeaderboardFilter {
    STREAKS, WORKOUTS, CALORIES
}

class LeaderboardAdapter(
    private var users: List<LeaderboardUser>,
    private var activeFilter: LeaderboardFilter = LeaderboardFilter.STREAKS
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    inner class LeaderboardViewHolder(private val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: LeaderboardUser, position: Int) {
            val rank = position + 1
            binding.tvRank.text = when (rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "#$rank"
            }
            binding.tvUserName.text = user.name
            
            val caloriesFormatted = if (user.caloriesBurned % 1 == 0.0) {
                user.caloriesBurned.toInt().toString()
            } else {
                String.format("%.1f", user.caloriesBurned)
            }
            
            val streakText = if (user.streak == 1) "1 day" else "${user.streak} days"
            val workoutsText = if (user.workoutsCount == 1) "1 workout" else "${user.workoutsCount} workouts"
            
            // Substats row text
            binding.tvUserStats.text = "⚡ $streakText | 💪 ${user.workoutsCount} | 🔥 $caloriesFormatted kcal"

            // Highlighted stat based on active filter
            binding.tvHighlightedStat.text = when (activeFilter) {
                LeaderboardFilter.STREAKS -> streakText
                LeaderboardFilter.WORKOUTS -> workoutsText
                LeaderboardFilter.CALORIES -> "$caloriesFormatted kcal"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(users[position], position)
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<LeaderboardUser>, filter: LeaderboardFilter) {
        users = newUsers
        activeFilter = filter
        notifyDataSetChanged()
    }
}
