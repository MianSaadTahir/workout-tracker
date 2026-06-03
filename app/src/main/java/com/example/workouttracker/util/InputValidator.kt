package com.example.workouttracker.util

/**
 * Utility object for password and profile input validation.
 */
object InputValidator {

    /**
     * Password requirements:
     * - Minimum 8 characters
     * - Contains at least one uppercase letter
     * - Contains at least one digit
     * - Contains at least one special character (non-letter, non-digit)
     */
    fun validatePassword(password: String?): Boolean {
        if (password == null) return false
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasMinLength && hasUppercase && hasDigit && hasSpecial
    }

    /**
     * Empty field detection: returns true if the input is null, empty or only whitespace.
     */
    fun isEmpty(input: String?): Boolean {
        return input.isNullOrBlank()
    }

    /**
     * Age validation: age is valid if it is greater than 0 and less than or equal to 120.
     */
    fun isValidAge(age: Int): Boolean {
        return age in 1..120
    }

    /**
     * Weight validation: weight must be strictly positive.
     */
    fun isValidWeight(weight: Double): Boolean {
        return weight > 0.0
    }

    /**
     * Height validation: height must be strictly positive.
     */
    fun isValidHeight(height: Double): Boolean {
        return height > 0.0
    }
}
