package com.example.workouttracker

import com.example.workouttracker.util.BmiCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * BmiCalculatorTest
 * Covers unit tests for:
 * - BMI value computation with kg/meters.
 * - BMI value computation with lb/inches unit conversions using 1 lb = 0.453592 kg and 1 inch = 0.0254 m.
 * - BMI category classifications (Underweight, Normal Weight, Overweight, Obese) with boundary values (18.5, 25.0, 30.0).
 */
class BmiCalculatorTest {

    @Test
    fun testBmiCalculationMetric() {
        // 70 kg, 1.75 meters
        // expected BMI = 70 / (1.75 * 1.75) = 22.857...
        val bmi = BmiCalculator.calculateBmi(70.0, 1.75, "kg", "meters")
        assertEquals(22.857, bmi, 0.01)
    }

    @Test
    fun testBmiCalculationImperial() {
        // 154 lb, 68 inches
        // weight in kg = 154 * 0.453592 = 69.853168
        // height in m = 68 * 0.0254 = 1.7272
        // expected BMI = 69.853168 / (1.7272 * 1.7272) = 23.415...
        val bmi = BmiCalculator.calculateBmi(154.0, 68.0, "lb", "inches")
        assertEquals(23.415, bmi, 0.01)
    }

    @Test
    fun testBmiCategoryUnderweight() {
        // Just below 18.5 boundary
        val category = BmiCalculator.getCategory(18.49)
        assertEquals("Underweight", category)
    }

    @Test
    fun testBmiCategoryNormalWeightBoundary() {
        // Exact lower boundary
        val categoryLower = BmiCalculator.getCategory(18.5)
        assertEquals("Normal Weight", categoryLower)

        // Just below upper boundary
        val categoryUpper = BmiCalculator.getCategory(24.99)
        assertEquals("Normal Weight", categoryUpper)
    }

    @Test
    fun testBmiCategoryOverweightBoundary() {
        // Exact lower boundary
        val categoryLower = BmiCalculator.getCategory(25.0)
        assertEquals("Overweight", categoryLower)

        // Just below upper boundary
        val categoryUpper = BmiCalculator.getCategory(29.99)
        assertEquals("Overweight", categoryUpper)
    }

    @Test
    fun testBmiCategoryObeseBoundary() {
        // Exact boundary
        val category = BmiCalculator.getCategory(30.0)
        assertEquals("Obese", category)

        // Well within obese
        val categoryHigh = BmiCalculator.getCategory(35.2)
        assertEquals("Obese", categoryHigh)
    }
}
