package com.example.workouttracker

import com.example.workouttracker.util.InputValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * InputValidationTest
 * Covers unit tests for:
 * - Password requirements (min 8 chars, uppercase, number, special character).
 * - Empty/whitespace/null field detection.
 * - Age boundaries (valid in [1, 120], invalid if <= 0 or > 120).
 * - Weight boundaries (invalid if <= 0).
 * - Height boundaries (invalid if <= 0).
 */
class InputValidationTest {

    @Test
    fun testPasswordValidation() {
        // Valid password
        assertTrue(InputValidator.validatePassword("StrongPass1!"))

        // Too short (7 characters)
        assertFalse(InputValidator.validatePassword("Str1!pa"))

        // Missing uppercase
        assertFalse(InputValidator.validatePassword("strongpass1!"))

        // Missing digit
        assertFalse(InputValidator.validatePassword("StrongPass!"))

        // Missing special character
        assertFalse(InputValidator.validatePassword("StrongPass1"))

        // Null password
        assertFalse(InputValidator.validatePassword(null))
    }

    @Test
    fun testEmptyFieldDetection() {
        assertTrue(InputValidator.isEmpty(null))
        assertTrue(InputValidator.isEmpty(""))
        assertTrue(InputValidator.isEmpty("   "))
        assertFalse(InputValidator.isEmpty("valid text"))
    }

    @Test
    fun testAgeValidation() {
        // Valid ages
        assertTrue(InputValidator.isValidAge(1))
        assertTrue(InputValidator.isValidAge(25))
        assertTrue(InputValidator.isValidAge(120))

        // Invalid ages
        assertFalse(InputValidator.isValidAge(0))
        assertFalse(InputValidator.isValidAge(-10))
        assertFalse(InputValidator.isValidAge(121))
    }

    @Test
    fun testWeightValidation() {
        // Valid weight
        assertTrue(InputValidator.isValidWeight(70.5))

        // Invalid weight
        assertFalse(InputValidator.isValidWeight(0.0))
        assertFalse(InputValidator.isValidWeight(-1.0))
    }

    @Test
    fun testHeightValidation() {
        // Valid height
        assertTrue(InputValidator.isValidHeight(1.75))

        // Invalid height
        assertFalse(InputValidator.isValidHeight(0.0))
        assertFalse(InputValidator.isValidHeight(-1.5))
    }
}
