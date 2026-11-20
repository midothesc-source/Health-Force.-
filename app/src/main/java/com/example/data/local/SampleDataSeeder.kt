package com.example.data.local

import com.example.data.model.DailyContextDoc
import com.example.data.model.DateUtils
import com.example.data.model.FoodCategory
import com.example.data.model.FoodEntryDoc
import com.example.data.model.FoodPresetDoc
import com.example.data.model.MetabolicState
import com.example.data.model.WalkEntryDoc
import com.example.data.model.WalkPace
import com.example.data.model.WeightCheckInDoc
import com.example.data.model.WeightTag
import com.example.data.model.calculateWalkActiveCalories
import com.example.data.model.estimateDistanceKm

object SampleDataSeeder {

    fun defaultPresets(userId: String = ""): List<FoodPresetDoc> = listOf(
        FoodPresetDoc(
            userId = userId,
            name = "Boiled Pasta with Eggs (1 plate)",
            calories = 480L,
            category = FoodCategory.LUNCH.name,
            sortOrder = 0L,
            defaultPreset = true
        ),
        FoodPresetDoc(
            userId = userId,
            name = "Koshary (Medium bowl)",
            calories = 650L,
            category = FoodCategory.LUNCH.name,
            sortOrder = 1L,
            defaultPreset = true
        ),
        FoodPresetDoc(
            userId = userId,
            name = "Cottage Cheese (Gebna Qareesh) + 2 Eggs + 1 Balady Bread",
            calories = 430L,
            category = FoodCategory.BREAKFAST.name,
            sortOrder = 2L,
            defaultPreset = true
        ),
        FoodPresetDoc(
            userId = userId,
            name = "Shakshouka with Rice (1 serving)",
            calories = 510L,
            category = FoodCategory.DINNER.name,
            sortOrder = 3L,
            defaultPreset = true
        ),
        FoodPresetDoc(
            userId = userId,
            name = "Sausage Pizza (2 slices)",
            calories = 560L,
            category = FoodCategory.DINNER.name,
            sortOrder = 4L,
            defaultPreset = true
        ),
        FoodPresetDoc(
            userId = userId,
            name = "Tea with Milk & 2 Spoons Sugar",
            calories = 95L,
            category = FoodCategory.DRINK_SNACK.name,
            sortOrder = 5L,
            defaultPreset = true
        ),
        FoodPresetDoc(
            userId = userId,
            name = "Fury Energy Drink (1 can)",
            calories = 115L,
            category = FoodCategory.DRINK_SNACK.name,
            sortOrder = 6L,
            defaultPreset = true
        ),
        FoodPresetDoc(
            userId = userId,
            name = "1 Balady Bread Loaf",
            calories = 230L,
            category = FoodCategory.BREAKFAST.name,
            sortOrder = 7L,
            defaultPreset = true
        )
    )

    data class SampleDataset(
        val contexts: List<DailyContextDoc>,
        val foodEntries: List<FoodEntryDoc>,
        val walkEntries: List<WalkEntryDoc>,
        val weightCheckIns: List<WeightCheckInDoc>
    )

    fun buildSampleDataset(
        todayEpochDay: Long = DateUtils.currentEpochDay(),
        userId: String = ""
    ): SampleDataset {
        val bodyWeight = 87.6
        val contexts = mutableListOf<DailyContextDoc>()
        val foods = mutableListOf<FoodEntryDoc>()
        val walks = mutableListOf<WalkEntryDoc>()
        val weights = mutableListOf<WeightCheckInDoc>()

        val dayConfigs = listOf(
            DaySampleSpec(
                offset = -6,
                state = MetabolicState.NORMAL_ENERGY,
                weightKg = 88.6,
                weightTag = WeightTag.NORMAL,
                meals = listOf(
                    Triple("Cottage Cheese (Gebna Qareesh) + 2 Eggs + 1 Balady Bread", 430, FoodCategory.BREAKFAST),
                    Triple("Tea with Milk & 2 Spoons Sugar", 95, FoodCategory.DRINK_SNACK),
                    Triple("Koshary (Medium bowl)", 650, FoodCategory.LUNCH),
                    Triple("Shakshouka with Rice (1 serving)", 510, FoodCategory.DINNER)
                ),
                walkMinutes = 40,
                walkPace = WalkPace.BRISK_WALK
            ),
            DaySampleSpec(
                offset = -5,
                state = MetabolicState.NORMAL_ENERGY,
                weightKg = 88.3,
                weightTag = WeightTag.NORMAL,
                meals = listOf(
                    Triple("Cottage Cheese (Gebna Qareesh) + 2 Eggs + 1 Balady Bread", 430, FoodCategory.BREAKFAST),
                    Triple("Boiled Pasta with Eggs (1 plate)", 480, FoodCategory.LUNCH),
                    Triple("Fury Energy Drink (1 can)", 115, FoodCategory.DRINK_SNACK),
                    Triple("Grilled Chicken & Salad", 490, FoodCategory.DINNER)
                ),
                walkMinutes = 48,
                walkPace = WalkPace.BRISK_WALK
            ),
            DaySampleSpec(
                offset = -4,
                state = MetabolicState.MEDICATION_DRAG,
                weightKg = 89.2,
                weightTag = WeightTag.BLOATED_WATER_RETENTION,
                meals = listOf(
                    Triple("1 Balady Bread Loaf", 230, FoodCategory.BREAKFAST),
                    Triple("Tea with Milk & 2 Spoons Sugar", 95, FoodCategory.DRINK_SNACK),
                    Triple("Boiled Pasta with Eggs (1 plate)", 480, FoodCategory.LUNCH),
                    Triple("Sausage Pizza (2 slices)", 560, FoodCategory.DINNER)
                ),
                walkMinutes = 30,
                walkPace = WalkPace.CASUAL_STROLL
            ),
            DaySampleSpec(
                offset = -3,
                state = MetabolicState.MEDICATION_DRAG,
                weightKg = 88.8,
                weightTag = WeightTag.POST_MEAL,
                meals = listOf(
                    Triple("Cottage Cheese (Gebna Qareesh) + 2 Eggs + 1 Balady Bread", 430, FoodCategory.BREAKFAST),
                    Triple("Koshary (Medium bowl)", 650, FoodCategory.LUNCH),
                    Triple("Tea with Milk & 2 Spoons Sugar", 95, FoodCategory.DRINK_SNACK),
                    Triple("Shakshouka with Rice (1 serving)", 510, FoodCategory.DINNER)
                ),
                walkMinutes = 45,
                walkPace = WalkPace.BRISK_WALK
            ),
            DaySampleSpec(
                offset = -2,
                state = MetabolicState.HIGH_STRESS_LOW_SLEEP,
                weightKg = 88.1,
                weightTag = WeightTag.NORMAL,
                meals = listOf(
                    Triple("Cottage Cheese (Gebna Qareesh) + 2 Eggs + 1 Balady Bread", 430, FoodCategory.BREAKFAST),
                    Triple("Fury Energy Drink (1 can)", 115, FoodCategory.DRINK_SNACK),
                    Triple("Boiled Pasta with Eggs (1 plate)", 480, FoodCategory.LUNCH),
                    Triple("Sausage Pizza (2 slices)", 560, FoodCategory.DINNER)
                ),
                walkMinutes = 35,
                walkPace = WalkPace.CASUAL_STROLL
            ),
            DaySampleSpec(
                offset = -1,
                state = MetabolicState.NORMAL_ENERGY,
                weightKg = 87.9,
                weightTag = WeightTag.NORMAL,
                meals = listOf(
                    Triple("Cottage Cheese (Gebna Qareesh) + 2 Eggs + 1 Balady Bread", 430, FoodCategory.BREAKFAST),
                    Triple("Tea with Milk & 2 Spoons Sugar", 95, FoodCategory.DRINK_SNACK),
                    Triple("Koshary (Medium bowl)", 650, FoodCategory.LUNCH),
                    Triple("Shakshouka with Rice (1 serving)", 510, FoodCategory.DINNER)
                ),
                walkMinutes = 52,
                walkPace = WalkPace.FAST_PACE
            ),
            DaySampleSpec(
                offset = 0,
                state = MetabolicState.MEDICATION_DRAG,
                weightKg = 87.6,
                weightTag = WeightTag.NORMAL,
                meals = listOf(
                    Triple("Cottage Cheese (Gebna Qareesh) + 2 Eggs + 1 Balady Bread", 430, FoodCategory.BREAKFAST),
                    Triple("Tea with Milk & 2 Spoons Sugar", 95, FoodCategory.DRINK_SNACK),
                    Triple("Boiled Pasta with Eggs (1 plate)", 480, FoodCategory.LUNCH)
                ),
                walkMinutes = 28,
                walkPace = WalkPace.BRISK_WALK
            )
        )

        dayConfigs.forEach { spec ->
            val day = todayEpochDay + spec.offset
            contexts.add(
                DailyContextDoc(
                    id = "day_$day",
                    userId = userId,
                    dateEpochDay = day,
                    metabolicState = spec.state.name
                )
            )

            weights.add(
                WeightCheckInDoc(
                    id = "day_$day",
                    userId = userId,
                    dateEpochDay = day,
                    timestampMillis = DateUtils.epochDayToMillisAtTime(day, 7, 30),
                    weightKg = spec.weightKg,
                    tag = spec.weightTag.name
                )
            )

            val mealHours = listOf(8 to 45, 11 to 15, 14 to 30, 20 to 0)
            spec.meals.forEachIndexed { idx, (name, kcal, cat) ->
                val (hr, min) = mealHours.getOrElse(idx) { 18 to 0 }
                foods.add(
                    FoodEntryDoc(
                        userId = userId,
                        dateEpochDay = day,
                        timestampMillis = DateUtils.epochDayToMillisAtTime(day, hr, min),
                        name = name,
                        calories = kcal.toLong(),
                        category = cat.name
                    )
                )
            }

            walks.add(
                WalkEntryDoc(
                    userId = userId,
                    dateEpochDay = day,
                    timestampMillis = DateUtils.epochDayToMillisAtTime(day, 17, 15),
                    durationMinutes = spec.walkMinutes.toLong(),
                    pace = spec.walkPace.name,
                    distanceKm = estimateDistanceKm(spec.walkMinutes, spec.walkPace),
                    caloriesBurned = calculateWalkActiveCalories(spec.walkMinutes, spec.walkPace, bodyWeight).toLong(),
                    manualCalories = false
                )
            )
        }

        return SampleDataset(
            contexts = contexts,
            foodEntries = foods,
            walkEntries = walks,
            weightCheckIns = weights
        )
    }

    private data class DaySampleSpec(
        val offset: Int,
        val state: MetabolicState,
        val weightKg: Double,
        val weightTag: WeightTag,
        val meals: List<Triple<String, Int, FoodCategory>>,
        val walkMinutes: Int,
        val walkPace: WalkPace
    )
}
