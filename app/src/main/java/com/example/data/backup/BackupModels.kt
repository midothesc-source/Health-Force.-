package com.example.data.backup

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PulseLogBackup(
    val version: Int = 1,
    val exportedAtMillis: Long,
    val settings: BackupSettings,
    val dailyContexts: List<BackupDailyContext>,
    val foodPresets: List<BackupFoodPreset>,
    val foodEntries: List<BackupFoodEntry>,
    val walkEntries: List<BackupWalkEntry>,
    val weightCheckIns: List<BackupWeightCheckIn>
)

@JsonClass(generateAdapter = true)
data class BackupSettings(
    val bodyWeightKg: Double,
    val estimatedBmrKcal: Int,
    val baseNeatKcal: Int,
    val dailyWalkGoalMinutes: Int,
    val dailyWalkGoalKcal: Int
)

@JsonClass(generateAdapter = true)
data class BackupDailyContext(
    val dateEpochDay: Long,
    val metabolicState: String
)

@JsonClass(generateAdapter = true)
data class BackupFoodPreset(
    val name: String,
    val calories: Int,
    val category: String,
    val sortOrder: Int,
    val isDefault: Boolean
)

@JsonClass(generateAdapter = true)
data class BackupFoodEntry(
    val dateEpochDay: Long,
    val timestampMillis: Long,
    val name: String,
    val calories: Int,
    val category: String
)

@JsonClass(generateAdapter = true)
data class BackupWalkEntry(
    val dateEpochDay: Long,
    val timestampMillis: Long,
    val durationMinutes: Int,
    val pace: String,
    val distanceKm: Double?,
    val caloriesBurned: Int,
    val isManualCalories: Boolean
)

@JsonClass(generateAdapter = true)
data class BackupWeightCheckIn(
    val dateEpochDay: Long,
    val timestampMillis: Long,
    val weightKg: Double,
    val tag: String
)
