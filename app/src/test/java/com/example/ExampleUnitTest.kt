package com.example

import com.example.data.local.SampleDataSeeder
import com.example.data.model.WalkPace
import com.example.data.model.calculateWalkActiveCalories
import com.example.data.model.estimateDistanceKm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `walk active calorie calculation scales with pace and duration`() {
        val casual30 = calculateWalkActiveCalories(30, WalkPace.CASUAL_STROLL, 85.0)
        val brisk30 = calculateWalkActiveCalories(30, WalkPace.BRISK_WALK, 85.0)
        val fast30 = calculateWalkActiveCalories(30, WalkPace.FAST_PACE, 85.0)

        assertTrue(casual30 in 60..110)
        assertTrue(brisk30 > casual30)
        assertTrue(fast30 > brisk30)
    }

    @Test
    fun `distance estimation matches pace speed`() {
        val dist = estimateDistanceKm(30, WalkPace.BRISK_WALK)
        assertEquals(2.6, dist, 0.05)
    }

    @Test
    fun `sample data seeder includes all 8 Egyptian and everyday presets and 7 days of data`() {
        val presets = SampleDataSeeder.defaultPresets("test_uid")
        assertEquals(8, presets.size)
        assertTrue(presets.any { it.name.contains("Koshary") && it.calories == 650L })
        assertTrue(presets.any { it.name.contains("Gebna Qareesh") && it.calories == 430L })

        val sample = SampleDataSeeder.buildSampleDataset(todayEpochDay = 20000L, userId = "test_uid")
        assertEquals(7, sample.contexts.size)
        assertEquals(7, sample.weightCheckIns.size)
        assertEquals(7, sample.walkEntries.size)
        assertTrue(sample.foodEntries.size >= 20)
    }
}
