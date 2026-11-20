package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TrackerRepository
import com.example.data.model.DailyContextDoc
import com.example.data.model.DateUtils
import com.example.data.model.FoodCategory
import com.example.data.model.FoodEntryDoc
import com.example.data.model.FoodPresetDoc
import com.example.data.model.MetabolicState
import com.example.data.model.UserSettings
import com.example.data.model.WalkEntryDoc
import com.example.data.model.WalkPace
import com.example.data.model.WeightCheckInDoc
import com.example.data.model.WeightTag
import com.example.data.model.calculateWalkActiveCalories
import com.example.ui.state.DayEffortSummary
import com.example.ui.state.ScaleNoiseInsight
import com.example.ui.state.TimelineItem
import com.example.ui.state.TrackerUiState
import com.example.ui.state.WeightTrendPoint
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

class TrackerViewModel(
    private val repository: TrackerRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized(currentUserId).onFailure { err ->
                _events.tryEmit("Sync error: ${err.localizedMessage ?: "Unable to initialize profile"}")
            }
        }
    }

    private data class RawDataBundle(
        val settings: UserSettings,
        val contexts: List<DailyContextDoc>,
        val foods: List<FoodEntryDoc>,
        val presets: List<FoodPresetDoc>,
        val walks: List<WalkEntryDoc>,
        val weights: List<WeightCheckInDoc>
    )

    val uiState: StateFlow<TrackerUiState> = combine(
        repository.observeUserSettings(currentUserId),
        repository.observeContexts(currentUserId),
        repository.observeFoodEntries(currentUserId),
        repository.observePresets(currentUserId),
        repository.observeWalkEntries(currentUserId),
        repository.observeWeightCheckIns(currentUserId)
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        RawDataBundle(
            settings = values[0] as UserSettings,
            contexts = values[1] as List<DailyContextDoc>,
            foods = values[2] as List<FoodEntryDoc>,
            presets = values[3] as List<FoodPresetDoc>,
            walks = values[4] as List<WalkEntryDoc>,
            weights = values[5] as List<WeightCheckInDoc>
        )
    }.map { bundle ->
        computeUiState(bundle)
    }.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing tracker state", rootError)
        emit(
            TrackerUiState(
                isLoading = false,
                errorMessage = rootError.localizedMessage ?: "Failed to sync data from Firestore"
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = TrackerUiState(isLoading = true)
    )

    private fun computeUiState(bundle: RawDataBundle): TrackerUiState {
        val today = DateUtils.currentEpochDay()
        val settings = bundle.settings
        val contextMap = bundle.contexts.associateBy { it.dateEpochDay }
        val todayState = contextMap[today]?.stateEnum ?: MetabolicState.NORMAL_ENERGY

        val todayFoods = bundle.foods.filter { it.dateEpochDay == today }
        val todayWalks = bundle.walks.filter { it.dateEpochDay == today }

        val caloriesConsumed = todayFoods.sumOf { it.caloriesInt }
        val activeWalkBurn = todayWalks.sumOf { it.caloriesInt }
        val activeWalkMinutes = todayWalks.sumOf { it.durationInt }

        val baseDailyBurn = settings.baseDailyBurnKcal
        val totalBurn = baseDailyBurn + activeWalkBurn
        val netBalance = caloriesConsumed - totalBurn
        val dailyCalorieTarget = (totalBurn - 300 + todayState.calorieTargetOffset).coerceAtLeast(1400)

        val timeline = (todayFoods.map { TimelineItem.Food(it) } + todayWalks.map { TimelineItem.Walk(it) })
            .sortedByDescending { it.timestampMillis }

        val loggedDaysSet = (bundle.foods.map { it.dateEpochDay } + bundle.walks.map { it.dateEpochDay }).toSet()
        val streak = calculateStreak(today, loggedDaysSet)

        val foodLoggedScore = if (todayFoods.isNotEmpty()) 30 else 0
        val mealCoverageScore = (todayFoods.size.coerceAtMost(3) * 10)
        val walkGoalRatio = (activeWalkMinutes.toFloat() / settings.dailyWalkGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)
        val walkScore = (walkGoalRatio * 30f).roundToInt()
        val streakBonus = if (streak >= 3) 10 else (streak * 3)
        val effortScore = (foodLoggedScore + mealCoverageScore + walkScore + streakBonus).coerceIn(0, 100)

        val weightMap = bundle.weights.associateBy { it.dateEpochDay }
        val windowDays = ((today - 6)..today).toList()

        val trendPoints = windowDays.map { day ->
            val checkIn = weightMap[day]
            val trailingWeights = bundle.weights
                .filter { it.dateEpochDay in (day - 6)..day }
                .map { it.weightKg }
            val rollingAvg = if (trailingWeights.isNotEmpty()) {
                (trailingWeights.average() * 10.0).roundToInt() / 10.0
            } else null

            WeightTrendPoint(
                epochDay = day,
                shortLabel = DateUtils.formatShortDay(day),
                dayMonthLabel = DateUtils.formatDayMonth(day),
                rawWeightKg = checkIn?.weightKg,
                rollingAvgKg = rollingAvg,
                tag = checkIn?.tagEnum
            )
        }

        val effortSummary = windowDays.map { day ->
            val dayFoods = bundle.foods.filter { it.dateEpochDay == day }
            val dayWalks = bundle.walks.filter { it.dateEpochDay == day }
            val consumed = dayFoods.sumOf { it.caloriesInt }
            val walkKcal = dayWalks.sumOf { it.caloriesInt }
            val walkMins = dayWalks.sumOf { it.durationInt }
            val dayBurn = baseDailyBurn + walkKcal
            val dayState = contextMap[day]?.stateEnum ?: MetabolicState.NORMAL_ENERGY
            val targetIntake = (dayBurn - 300 + dayState.calorieTargetOffset).coerceAtLeast(1400)
            val logged = dayFoods.isNotEmpty() || dayWalks.isNotEmpty()

            val moveProgress = (walkKcal.toFloat() / settings.dailyWalkGoalKcal.coerceAtLeast(1)).coerceIn(0f, 1f)
            val intakeAdherence = if (consumed == 0) 0f else {
                val diff = abs(consumed - targetIntake)
                (1f - (diff / 900f)).coerceIn(0.2f, 1f)
            }
            val adherence = if (!logged) 0f else ((moveProgress * 0.5f) + (intakeAdherence * 0.5f)).coerceIn(0.15f, 1f)

            DayEffortSummary(
                epochDay = day,
                shortLabel = DateUtils.formatShortDay(day),
                walkMinutes = walkMins,
                walkCalories = walkKcal,
                caloriesConsumed = consumed,
                totalBurn = dayBurn,
                isLogged = logged,
                adherenceRatio = adherence
            )
        }

        val insight = buildScaleNoiseInsight(
            weights = bundle.weights,
            trendPoints = trendPoints,
            effortSummary = effortSummary,
            todayState = todayState,
            streak = streak
        )

        val todayWeight = weightMap[today]
        val latestKnownWeight = bundle.weights.lastOrNull()?.weightKg ?: settings.bodyWeightKg

        return TrackerUiState(
            isLoading = false,
            errorMessage = null,
            todayEpochDay = today,
            todayFormattedDate = DateUtils.formatFullDate(today),
            settings = settings,
            todayMetabolicState = todayState,
            caloriesConsumedToday = caloriesConsumed,
            dailyCalorieTargetToday = dailyCalorieTarget,
            activeWalkBurnToday = activeWalkBurn,
            activeWalkMinutesToday = activeWalkMinutes,
            estimatedBmrKcal = settings.estimatedBmrKcal,
            baseDailyBurnKcal = baseDailyBurn,
            totalBurnToday = totalBurn,
            netBalanceToday = netBalance,
            effortScore = effortScore,
            consistencyStreakDays = streak,
            todayTimeline = timeline,
            foodPresets = bundle.presets,
            todayWeightCheckIn = todayWeight,
            latestKnownWeightKg = latestKnownWeight,
            weightTrendPoints = trendPoints,
            sevenDayEffortSummary = effortSummary,
            scaleNoiseInsight = insight
        )
    }

    private fun calculateStreak(today: Long, loggedDays: Set<Long>): Int {
        if (loggedDays.isEmpty()) return 0
        var cursor = if (loggedDays.contains(today)) today else today - 1
        if (!loggedDays.contains(cursor)) return 0
        var count = 0
        while (loggedDays.contains(cursor)) {
            count++
            cursor--
        }
        return count
    }

    private fun buildScaleNoiseInsight(
        weights: List<WeightCheckInDoc>,
        trendPoints: List<WeightTrendPoint>,
        effortSummary: List<DayEffortSummary>,
        todayState: MetabolicState,
        streak: Int
    ): ScaleNoiseInsight {
        val validRawPoints = trendPoints.filter { it.rawWeightKg != null }
        val validRollingPoints = trendPoints.filter { it.rollingAvgKg != null }
        val latestRaw = validRawPoints.lastOrNull()
        val latestRolling = validRollingPoints.lastOrNull()?.rollingAvgKg
        val firstRolling = validRollingPoints.firstOrNull()?.rollingAvgKg
        val rollingDelta = if (latestRolling != null && firstRolling != null) {
            ((latestRolling - firstRolling) * 10.0).roundToInt() / 10.0
        } else null

        val activeDays = effortSummary.count { it.isLogged }
        val avgWalkBurn = if (activeDays > 0) effortSummary.filter { it.isLogged }.map { it.walkCalories }.average().roundToInt() else 0
        val hasRecentRetentionSpike = weights.takeLast(5).any {
            it.tagEnum == WeightTag.BLOATED_WATER_RETENTION || it.tagEnum == WeightTag.POST_MEAL
        }

        val rawAboveRolling = (latestRaw?.rawWeightKg != null && latestRolling != null && latestRaw.rawWeightKg > latestRolling + 0.2)
        val isWaterNoise = hasRecentRetentionSpike || rawAboveRolling || todayState != MetabolicState.NORMAL_ENERGY

        val headline = when {
            validRawPoints.isEmpty() -> "No scale noise yet—log your first check-in"
            rollingDelta != null && rollingDelta <= -0.2 && isWaterNoise ->
                "Real trend is down ${abs(rollingDelta)} kg despite water noise"
            rollingDelta != null && rollingDelta < 0.0 ->
                "7-day rolling trend is down ${abs(rollingDelta)} kg"
            todayState == MetabolicState.MEDICATION_DRAG ->
                "Medication Drag active: scale weight reflects fluid shifts"
            else ->
                "$activeDays/7 days logged · Avg $avgWalkBurn kcal/day active burn"
        }

        val detail = when {
            validRawPoints.isEmpty() ->
                "Log daily check-ins with context tags (Normal, Bloated, Post-Meal) so the 7-day rolling curve isolates real tissue change."
            hasRecentRetentionSpike ->
                "A tagged water/post-meal spike lifted raw scale readings mid-week, while your $streak-day effort streak kept the 7-day moving average steady at ${latestRolling ?: "--"} kg."
            todayState == MetabolicState.MEDICATION_DRAG ->
                "With Medication Drag selected, short-term scale plateaus are expected fluid retention. Your $avgWalkBurn kcal/day walk average is driving real metabolic momentum."
            else ->
                "Consistency beats single weigh-ins: $activeDays of the last 7 days tracked with $avgWalkBurn kcal/day average walk burn."
        }

        return ScaleNoiseInsight(
            badge = if (isWaterNoise) "WATER NOISE FILTERED" else "SIGNAL VS NOISE",
            headline = headline,
            detail = detail,
            latestRawKg = latestRaw?.rawWeightKg,
            latestRollingKg = latestRolling,
            rollingDeltaKg = rollingDelta,
            isWaterNoiseDetected = isWaterNoise
        )
    }

    fun setMetabolicState(state: MetabolicState) {
        viewModelScope.launch {
            repository.setMetabolicState(DateUtils.currentEpochDay(), state)
                .onFailure { _events.tryEmit("Failed to update state: ${it.localizedMessage}") }
        }
    }

    fun logFoodFromPreset(preset: FoodPresetDoc) {
        viewModelScope.launch {
            repository.logFood(
                name = preset.name,
                calories = preset.caloriesInt,
                category = preset.categoryEnum,
                saveAsPreset = false
            ).onSuccess {
                _events.tryEmit("Logged ${preset.name} · ${preset.caloriesInt} kcal")
            }.onFailure {
                _events.tryEmit("Could not log meal: ${it.localizedMessage}")
            }
        }
    }

    fun logCustomFood(name: String, calories: Int, category: FoodCategory, saveAsPreset: Boolean) {
        viewModelScope.launch {
            val nextOrder = uiState.value.foodPresets.size
            repository.logFood(
                name = name,
                calories = calories,
                category = category,
                saveAsPreset = saveAsPreset,
                nextPresetSortOrder = nextOrder
            ).onSuccess {
                val presetNote = if (saveAsPreset) " & saved preset" else ""
                _events.tryEmit("Logged $name · $calories kcal$presetNote")
            }.onFailure {
                _events.tryEmit("Could not log meal: ${it.localizedMessage}")
            }
        }
    }

    fun updateFoodEntry(entry: FoodEntryDoc) {
        viewModelScope.launch {
            repository.updateFoodEntry(entry)
                .onSuccess { _events.tryEmit("Updated ${entry.name}") }
                .onFailure { _events.tryEmit("Update failed: ${it.localizedMessage}") }
        }
    }

    fun deleteFoodEntry(id: String) {
        viewModelScope.launch {
            repository.deleteFoodEntry(id)
                .onSuccess { _events.tryEmit("Meal entry deleted") }
                .onFailure { _events.tryEmit("Delete failed: ${it.localizedMessage}") }
        }
    }

    fun savePreset(preset: FoodPresetDoc) {
        viewModelScope.launch {
            repository.savePreset(preset)
                .onSuccess { _events.tryEmit("Saved preset: ${preset.name}") }
                .onFailure { _events.tryEmit("Preset update failed: ${it.localizedMessage}") }
        }
    }

    fun deletePreset(id: String) {
        viewModelScope.launch {
            repository.deletePreset(id)
                .onSuccess { _events.tryEmit("Preset removed") }
                .onFailure { _events.tryEmit("Preset delete failed: ${it.localizedMessage}") }
        }
    }

    fun logWalk(
        durationMinutes: Int,
        pace: WalkPace,
        distanceKm: Double?,
        manualCalories: Int?
    ) {
        viewModelScope.launch {
            val weight = uiState.value.settings.bodyWeightKg
            val calculated = calculateWalkActiveCalories(durationMinutes, pace, weight)
            val finalCalories = manualCalories ?: calculated
            repository.logWalk(
                durationMinutes = durationMinutes,
                pace = pace,
                distanceKm = distanceKm,
                caloriesBurned = finalCalories,
                manualCalories = manualCalories != null
            ).onSuccess {
                _events.tryEmit("Logged ${durationMinutes}m ${pace.displayName} · $finalCalories kcal")
            }.onFailure {
                _events.tryEmit("Could not log walk: ${it.localizedMessage}")
            }
        }
    }

    fun updateWalkEntry(entry: WalkEntryDoc) {
        viewModelScope.launch {
            repository.updateWalkEntry(entry)
                .onSuccess { _events.tryEmit("Updated walk session") }
                .onFailure { _events.tryEmit("Update failed: ${it.localizedMessage}") }
        }
    }

    fun deleteWalkEntry(id: String) {
        viewModelScope.launch {
            repository.deleteWalkEntry(id)
                .onSuccess { _events.tryEmit("Walk entry deleted") }
                .onFailure { _events.tryEmit("Delete failed: ${it.localizedMessage}") }
        }
    }

    fun saveWeightCheckIn(weightKg: Double, tag: WeightTag) {
        viewModelScope.launch {
            repository.saveWeightCheckIn(weightKg, tag)
                .onSuccess {
                    _events.tryEmit("Saved weight: ${String.format(Locale.US, "%.1f", weightKg)} kg (${tag.shortLabel})")
                }
                .onFailure {
                    _events.tryEmit("Could not save weight: ${it.localizedMessage}")
                }
        }
    }

    fun updateSettings(
        bodyWeightKg: Double,
        estimatedBmrKcal: Int,
        baseNeatKcal: Int,
        dailyWalkGoalMinutes: Int,
        dailyWalkGoalKcal: Int
    ) {
        viewModelScope.launch {
            repository.updateSettings(
                bodyWeightKg = bodyWeightKg,
                estimatedBmrKcal = estimatedBmrKcal,
                baseNeatKcal = baseNeatKcal,
                dailyWalkGoalMinutes = dailyWalkGoalMinutes,
                dailyWalkGoalKcal = dailyWalkGoalKcal
            ).onSuccess {
                _events.tryEmit("Settings saved")
            }.onFailure {
                _events.tryEmit("Could not save settings: ${it.localizedMessage}")
            }
        }
    }

    fun clearSampleDataAndStartFresh() {
        viewModelScope.launch {
            repository.clearSampleDataAndStartFresh()
                .onSuccess { _events.tryEmit("Sample logs cleared — ready for your fresh start") }
                .onFailure { _events.tryEmit("Clear failed: ${it.localizedMessage}") }
        }
    }

    fun restoreSampleData() {
        viewModelScope.launch {
            repository.restoreSampleData()
                .onSuccess { _events.tryEmit("7-day sample data restored") }
                .onFailure { _events.tryEmit("Restore failed: ${it.localizedMessage}") }
        }
    }

    suspend fun exportBackupJson(): Result<String> = repository.exportBackupJson()

    fun importBackupJson(json: String) {
        viewModelScope.launch {
            val result = repository.importBackupJson(json)
            result.onSuccess { count ->
                _events.tryEmit("Backup restored ($count records)")
            }.onFailure { err ->
                _events.tryEmit("Import failed: ${err.message ?: "Invalid JSON"}")
            }
        }
    }

    private companion object {
        const val TAG = "TrackerVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
