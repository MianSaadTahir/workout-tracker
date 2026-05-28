package com.example.workouttracker.data

import com.example.workouttracker.model.User
import com.example.workouttracker.model.Workout

object AppRepository {
    private val userList = mutableListOf<User>()
    private val workoutMap = HashMap<String, MutableList<Workout>>()
    private val templatesMap = HashMap<String, MutableList<Workout>>()
    var currentUser: User? = null

    fun registerUser(user: User): Boolean {
        if (userList.any { it.email == user.email }) {
            return false
        }
        userList.add(user)
        workoutMap[user.email] = mutableListOf()
        templatesMap[user.email] = mutableListOf()
        return true
    }

    fun loginUser(email: String, password: String): User? {
        val user = userList.find { it.email == email && it.password == password }
        currentUser = user
        return user
    }

    fun getWorkoutsForCurrentUser(): MutableList<Workout> {
        return currentUser?.let { workoutMap[it.email] } ?: mutableListOf()
    }

    fun getTemplatesForCurrentUser(): MutableList<Workout> {
        return currentUser?.let { templatesMap[it.email] } ?: mutableListOf()
    }

    fun logout() {
        currentUser = null
    }
}
