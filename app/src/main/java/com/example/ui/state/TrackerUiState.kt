package com.example.ui.state

import com.example.data.model.FoodEntryDoc
import com.example.data.model.FoodPresetDoc
import com.example.data.model.MetabolicState
import com.example.data.model.UserSettings
import com.example.data.model.WalkEntryDoc
import com.example.data.model.WeightCheckInDoc
import com.example.data.model.WeightTag

enum class AppTab(val label: String) {
    TODAY("Today"),
    LOG_FOOD("Log Food"),
    LOG_WALK("Log Walk"),
    TRENDS("Trends")
}

sealed class TimelineItem {
    abstract val timestampMillis: Long
    abstract val key: String

    data class Food(val entry: FoodEntryDoc) : TimelineItem() {
        override val timestampMillis: Long get() = entry.timestampMillis
        override val key: String get() = "food_${entry.id}"
    }

    data class Walk(val entry: WalkEntryDoc) : TimelineItem() {
        override val timestampMillis: Long get() = entry.timestampMillis
        override val key: String get() = "walk_${entry.id}"
    }
}

data class WeightTrendPoint(
    val epochDay: Long,
    val shortLabel: String,
    val dayMonthLabel: String,
    val rawWeightKg: Double?,
    val rollingAvgKg: Double?,
    val tag: WeightTag?
)

data class DayEffortSummary(
    val epochDay: Long,
    val shortLabel: String,
    val walkMinutes: Int,
    val walkCalories: Int,
    val caloriesConsumed: Int,
    val totalBurn: Int,
    val isLogged: Boolean,
    val adherenceRatio: Float
)

data class ScaleNoiseInsight(
    val badge: String,
    val headline: String,
    val detail: String,
    val latestRawKg: Double?,
    val latestRollingKg: Double?,
    val rollingDeltaKg: Double?,
    val isWaterNoiseDetected: Boolean
)

data class TrackerUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val todayEpochDay: Long = 0L,
    val todayFormattedDate: String = "",
    val settings: UserSettings = UserSettings(),
    val todayMetabolicState: MetabolicState = MetabolicState.NORMAL_ENERGY,
    val caloriesConsumedToday: Int = 0,
    val dailyCalorieTargetToday: Int = 1850,
    val activeWalkBurnToday: Int = 0,
    val activeWalkMinutesToday: Int = 0,
    val estimatedBmrKcal: Int = 1850,
    val baseDailyBurnKcal: Int = 2150,
    val totalBurnToday: Int = 2150,
    val netBalanceToday: Int = -2150,
    val effortScore: Int = 0,
    val consistencyStreakDays: Int = 0,
    val todayTimeline: List<TimelineItem> = emptyList(),
    val foodPresets: List<FoodPresetDoc> = emptyList(),
    val todayWeightCheckIn: WeightCheckInDoc? = null,
    val latestKnownWeightKg: Double = 87.6,
    val weightTrendPoints: List<WeightTrendPoint> = emptyList(),
    val sevenDayEffortSummary: List<DayEffortSummary> = emptyList(),
    val scaleNoiseInsight: ScaleNoiseInsight = ScaleNoiseInsight(
        badge = "TREND ENGINE",
        headline = "Log weight & effort to unlock trend intelligence",
        detail = "A 7-day rolling average smooths out sodium, glycogen, and medication-related fluid shifts.",
        latestRawKg = null,
        latestRollingKg = null,
        rollingDeltaKg = null,
        isWaterNoiseDetected = false
    )
)
