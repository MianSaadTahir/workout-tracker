package com.example.workouttracker.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.workouttracker.databinding.ItemWorkoutBinding
import com.example.workouttracker.model.Workout

class WorkoutAdapter(
    private var workouts: List<Workout>,
    private val onEdit: (Workout) -> Unit,
    private val onDelete: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    inner class WorkoutViewHolder(private val binding: ItemWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(workout: Workout) {
            binding.tvWorkoutName.text = workout.name
            binding.tvCategory.text = workout.category
            binding.tvWorkoutDetails.text = "${workout.sets} Sets x ${workout.reps} Reps @ ${workout.weight} kg"

            binding.btnEdit.setOnClickListener { onEdit(workout) }
            binding.btnDelete.setOnClickListener { onDelete(workout) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        holder.bind(workouts[position])
    }

    override fun getItemCount(): Int = workouts.size

    fun updateList(newList: List<Workout>) {
        workouts = newList
        notifyDataSetChanged()
    }
}
