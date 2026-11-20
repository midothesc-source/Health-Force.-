package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class MetabolicState(
    val displayName: String,
    val shortLabel: String,
    val headline: String,
    val guidance: String,
    val targetAdjustmentNote: String,
    val calorieTargetOffset: Int
) {
    NORMAL_ENERGY(
        displayName = "Normal Energy",
        shortLabel = "Normal",
        headline = "Standard Metabolic Baseline",
        guidance = "Steady energy today. Aim for consistent logging and your regular 45-min movement goal.",
        targetAdjustmentNote = "Standard BMR + activity burn target",
        calorieTargetOffset = 0
    ),
    MEDICATION_DRAG(
        displayName = "Medication Drag",
        shortLabel = "Med Drag",
        headline = "Water Retention & Metabolic Drag Mode",
        guidance = "Scale bumps today likely reflect fluid retention or slowed motility—not fat gain. Focus on hydration, gentle walks, and non-scale consistency wins.",
        targetAdjustmentNote = "Scale weight de-emphasized · Effort prioritized",
        calorieTargetOffset = 150
    ),
    HIGH_STRESS_LOW_SLEEP(
        displayName = "High Stress / Low Sleep",
        shortLabel = "Low Sleep",
        headline = "Cortisol Recovery & Maintenance Mode",
        guidance = "Poor sleep elevates hunger cues and cortisol water retention. Prioritize gentle movement and hitting near maintenance rather than forcing a steep deficit.",
        targetAdjustmentNote = "Maintenance buffer (+250 kcal) · Gentle pace",
        calorieTargetOffset = 250
    );

    companion object {
        fun fromRaw(raw: String?): MetabolicState =
            entries.firstOrNull { it.name == raw } ?: NORMAL_ENERGY
    }
}

enum class FoodCategory(val displayName: String, val shortLabel: String) {
    BREAKFAST("Breakfast", "Breakfast"),
    LUNCH("Lunch", "Lunch"),
    DINNER("Dinner", "Dinner"),
    DRINK_SNACK("Drink / Snack", "Snack");

    companion object {
        fun fromRaw(raw: String?): FoodCategory =
            entries.firstOrNull { it.name == raw } ?: LUNCH
    }
}

enum class WalkPace(
    val displayName: String,
    val subtitle: String,
    val metValue: Double,
    val speedKmPerHour: Double
) {
    CASUAL_STROLL("Casual Stroll", "Gentle recovery pace", 2.8, 3.5),
    BRISK_WALK("Brisk Walk", "Steady aerobic stride", 4.0, 5.2),
    FAST_PACE("Fast Pace", "Power walk / incline", 5.3, 6.4);

    companion object {
        fun fromRaw(raw: String?): WalkPace =
            entries.firstOrNull { it.name == raw } ?: BRISK_WALK
    }
}

enum class WeightTag(val displayName: String, val shortLabel: String) {
    NORMAL("Normal", "Normal"),
    BLOATED_WATER_RETENTION("Bloated / Water Retention", "Water / Bloated"),
    POST_MEAL("Post-Meal", "Post-Meal");

    companion object {
        fun fromRaw(raw: String?): WeightTag =
            entries.firstOrNull { it.name == raw } ?: NORMAL
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfileDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val bodyWeightKg: Double = 87.6,
    val estimatedBmrKcal: Long = 1850L,
    val baseNeatKcal: Long = 300L,
    val dailyWalkGoalMinutes: Long = 45L,
    val dailyWalkGoalKcal: Long = 300L,
    val initialized: Boolean = false,
    val sampleDataActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DailyContextDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateEpochDay: Long = 0L,
    val metabolicState: String = MetabolicState.NORMAL_ENERGY.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val stateEnum: MetabolicState get() = MetabolicState.fromRaw(metabolicState)
}

@Keep
@IgnoreExtraProperties
data class FoodPresetDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val calories: Long = 0L,
    val category: String = FoodCategory.LUNCH.name,
    val sortOrder: Long = 0L,
    val defaultPreset: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val categoryEnum: FoodCategory get() = FoodCategory.fromRaw(category)
    val caloriesInt: Int get() = calories.toInt()
}

@Keep
@IgnoreExtraProperties
data class FoodEntryDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateEpochDay: Long = 0L,
    val timestampMillis: Long = 0L,
    val name: String = "",
    val calories: Long = 0L,
    val category: String = FoodCategory.LUNCH.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val categoryEnum: FoodCategory get() = FoodCategory.fromRaw(category)
    val caloriesInt: Int get() = calories.toInt()
}

@Keep
@IgnoreExtraProperties
data class WalkEntryDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateEpochDay: Long = 0L,
    val timestampMillis: Long = 0L,
    val durationMinutes: Long = 0L,
    val pace: String = WalkPace.BRISK_WALK.name,
    val distanceKm: Double? = null,
    val caloriesBurned: Long = 0L,
    val manualCalories: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val paceEnum: WalkPace get() = WalkPace.fromRaw(pace)
    val durationInt: Int get() = durationMinutes.toInt()
    val caloriesInt: Int get() = caloriesBurned.toInt()
}

@Keep
@IgnoreExtraProperties
data class WeightCheckInDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val dateEpochDay: Long = 0L,
    val timestampMillis: Long = 0L,
    val weightKg: Double = 87.6,
    val tag: String = WeightTag.NORMAL.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val tagEnum: WeightTag get() = WeightTag.fromRaw(tag)
}

data class UserSettings(
    val bodyWeightKg: Double = 87.6,
    val estimatedBmrKcal: Int = 1850,
    val baseNeatKcal: Int = 300,
    val dailyWalkGoalMinutes: Int = 45,
    val dailyWalkGoalKcal: Int = 300,
    val isInitialized: Boolean = false,
    val isSampleDataActive: Boolean = true
) {
    val baseDailyBurnKcal: Int get() = estimatedBmrKcal + baseNeatKcal
}

fun UserProfileDoc.toUserSettings(): UserSettings = UserSettings(
    bodyWeightKg = bodyWeightKg,
    estimatedBmrKcal = estimatedBmrKcal.toInt(),
    baseNeatKcal = baseNeatKcal.toInt(),
    dailyWalkGoalMinutes = dailyWalkGoalMinutes.toInt(),
    dailyWalkGoalKcal = dailyWalkGoalKcal.toInt(),
    isInitialized = initialized,
    isSampleDataActive = sampleDataActive
)

fun calculateWalkActiveCalories(
    durationMinutes: Int,
    pace: WalkPace,
    bodyWeightKg: Double
): Int {
    if (durationMinutes <= 0) return 0
    val netMet = (pace.metValue - 1.0).coerceAtLeast(1.2)
    val kcalPerMin = (netMet * 3.5 * bodyWeightKg) / 200.0
    return (kcalPerMin * durationMinutes).roundToInt().coerceAtLeast(1)
}

fun estimateDistanceKm(durationMinutes: Int, pace: WalkPace): Double {
    if (durationMinutes <= 0) return 0.0
    val raw = pace.speedKmPerHour * (durationMinutes / 60.0)
    return (raw * 100.0).roundToInt() / 100.0
}

object DateUtils {
    private const val ONE_DAY_MILLIS = 86_400_000L

    fun currentEpochDay(): Long = millisToEpochDay(System.currentTimeMillis())

    fun millisToEpochDay(millis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val tzOffset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)
        return (cal.timeInMillis + tzOffset) / ONE_DAY_MILLIS
    }

    fun epochDayToNoonMillis(epochDay: Long): Long {
        val cal = Calendar.getInstance()
        val todayDay = currentEpochDay()
        val diff = (epochDay - todayDay).toInt()
        cal.add(Calendar.DAY_OF_YEAR, diff)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun epochDayToMillisAtTime(epochDay: Long, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        val todayDay = currentEpochDay()
        val diff = (epochDay - todayDay).toInt()
        cal.add(Calendar.DAY_OF_YEAR, diff)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun formatTime(millis: Long): String {
        val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
        return fmt.format(Date(millis))
    }

    fun formatShortDay(epochDay: Long): String {
        val today = currentEpochDay()
        if (epochDay == today) return "Today"
        if (epochDay == today - 1) return "Yest"
        val fmt = SimpleDateFormat("EEE", Locale.getDefault())
        return fmt.format(Date(epochDayToNoonMillis(epochDay)))
    }

    fun formatDayMonth(epochDay: Long): String {
        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
        return fmt.format(Date(epochDayToNoonMillis(epochDay)))
    }

    fun formatFullDate(epochDay: Long): String {
        val fmt = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        return fmt.format(Date(epochDayToNoonMillis(epochDay)))
    }
}
