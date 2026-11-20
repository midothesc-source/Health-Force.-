package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DateUtils
import com.example.data.model.FoodCategory
import com.example.data.model.FoodEntryDoc
import com.example.data.model.MetabolicState
import com.example.data.model.WalkEntryDoc
import com.example.ui.components.BalanceEffortRing
import com.example.ui.components.EditFoodEntryDialog
import com.example.ui.components.EditWalkEntryDialog
import com.example.ui.components.PulseCard
import com.example.ui.state.TimelineItem
import com.example.ui.state.TrackerUiState
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PillSurface
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.RoseSoft
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VioletBorder
import com.example.ui.theme.VioletSoft
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TodayScreen(
    uiState: TrackerUiState,
    onSelectMetabolicState: (MetabolicState) -> Unit,
    onUpdateFood: (FoodEntryDoc) -> Unit,
    onDeleteFood: (String) -> Unit,
    onUpdateWalk: (WalkEntryDoc) -> Unit,
    onDeleteWalk: (String) -> Unit,
    onQuickNavigateFood: () -> Unit,
    onQuickNavigateWalk: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingFood by remember { mutableStateOf<FoodEntryDoc?>(null) }
    var editingWalk by remember { mutableStateOf<WalkEntryDoc?>(null) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (uiState.errorMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = RoseSoft,
                        border = BorderStroke(1.dp, RoseAccent.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = RoseAccent,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // 1. Daily Metabolic Context Selector (Top Pill Bar)
            item {
                MetabolicContextSelector(
                    selected = uiState.todayMetabolicState,
                    onSelect = onSelectMetabolicState
                )
            }

            // 2. Daily Progress Summary (Circular Progress Indicators for Calorie & Activity Targets)
            item {
                DailyProgressSummaryCard(
                    caloriesConsumed = uiState.caloriesConsumedToday,
                    calorieTarget = uiState.dailyCalorieTargetToday,
                    activeWalkMinutes = uiState.activeWalkMinutesToday,
                    walkGoalMinutes = uiState.settings.dailyWalkGoalMinutes,
                    activeWalkCalories = uiState.activeWalkBurnToday,
                    walkGoalCalories = uiState.settings.dailyWalkGoalKcal
                )
            }

            // 3. Effort Score & Net Balance Ring Card
            item {
                PulseCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DAILY BALANCE & EFFORT",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Effort-First Fuel",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSoft
                        ) {
                            Text(
                                text = "${uiState.consistencyStreakDays}d Streak",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = EmeraldAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        BalanceEffortRing(
                            caloriesConsumed = uiState.caloriesConsumedToday,
                            totalBurn = uiState.totalBurnToday,
                            netBalance = uiState.netBalanceToday,
                            effortScore = uiState.effortScore
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiMiniTile(
                            label = "Consumed",
                            value = "${uiState.caloriesConsumedToday} kcal",
                            accent = AmberAccent,
                            modifier = Modifier.weight(1f)
                        )
                        KpiMiniTile(
                            label = "Active Walk",
                            value = "${uiState.activeWalkBurnToday} kcal",
                            subValue = "${uiState.activeWalkMinutesToday} min",
                            accent = EmeraldAccent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiMiniTile(
                            label = "BMR + Base NEAT",
                            value = "${uiState.baseDailyBurnKcal} kcal",
                            subValue = "BMR ${uiState.estimatedBmrKcal}",
                            accent = VioletAccent,
                            modifier = Modifier.weight(1f)
                        )
                        KpiMiniTile(
                            label = "Total Burn",
                            value = "${uiState.totalBurnToday} kcal",
                            subValue = "Base + Walk",
                            accent = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Today's Timeline Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Today's Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${uiState.todayTimeline.size} logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            if (uiState.todayTimeline.isEmpty()) {
                item {
                    PulseCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Nothing logged yet today",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                            Text(
                                text = "Tap a quick Egyptian meal preset or log a walk to build today's effort score.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onQuickNavigateFood,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("empty_log_food_cta"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberAccent,
                                        contentColor = SlateBackground
                                    )
                                ) {
                                    Text("Log Meal", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = onQuickNavigateWalk,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("empty_log_walk_cta"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldAccent,
                                        contentColor = SlateBackground
                                    )
                                ) {
                                    Text("Log Walk", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                items(uiState.todayTimeline, key = { it.key }) { item ->
                    when (item) {
                        is TimelineItem.Food -> FoodTimelineRow(
                            entry = item.entry,
                            onEdit = { editingFood = item.entry },
                            onDelete = { onDeleteFood(item.entry.id) }
                        )
                        is TimelineItem.Walk -> WalkTimelineRow(
                            entry = item.entry,
                            onEdit = { editingWalk = item.entry },
                            onDelete = { onDeleteWalk(item.entry.id) }
                        )
                    }
                }
            }
        }
    }

    editingFood?.let { food ->
        EditFoodEntryDialog(
            entry = food,
            onDismiss = { editingFood = null },
            onSave = { updated ->
                onUpdateFood(updated)
                editingFood = null
            }
        )
    }

    editingWalk?.let { walk ->
        EditWalkEntryDialog(
            entry = walk,
            onDismiss = { editingWalk = null },
            onSave = { updated ->
                onUpdateWalk(updated)
                editingWalk = null
            }
        )
    }
}

@Composable
private fun DailyProgressSummaryCard(
    caloriesConsumed: Int,
    calorieTarget: Int,
    activeWalkMinutes: Int,
    walkGoalMinutes: Int,
    activeWalkCalories: Int,
    walkGoalCalories: Int
) {
    val rawCalorieRatio = (caloriesConsumed.toFloat() / calorieTarget.coerceAtLeast(1)).coerceIn(0f, 1f)
    val rawActivityRatio = (activeWalkMinutes.toFloat() / walkGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)

    val animatedCalorieProgress by animateFloatAsState(
        targetValue = rawCalorieRatio,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "calorie_target_progress"
    )
    val animatedActivityProgress by animateFloatAsState(
        targetValue = rawActivityRatio,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "activity_target_progress"
    )

    val caloriePercent = ((caloriesConsumed.toFloat() / calorieTarget.coerceAtLeast(1)) * 100f).roundToInt()
    val activityPercent = ((activeWalkMinutes.toFloat() / walkGoalMinutes.coerceAtLeast(1)) * 100f).roundToInt()
    val isOverCalorieTarget = caloriesConsumed > calorieTarget
    val calorieColor = if (isOverCalorieTarget) RoseAccent else AmberAccent
    val calorieSubtext = if (isOverCalorieTarget) {
        "${caloriesConsumed - calorieTarget} kcal over target"
    } else {
        "${calorieTarget - caloriesConsumed} kcal remaining"
    }

    PulseCard(modifier = Modifier.testTag("daily_progress_summary_card")) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Daily Progress Summary",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text = "Calorie intake & active movement targets",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TargetProgressTile(
                title = "Calorie Target",
                progress = animatedCalorieProgress,
                percentLabel = "$caloriePercent%",
                primaryStat = "$caloriesConsumed / $calorieTarget kcal",
                secondaryStat = calorieSubtext,
                ringColor = calorieColor,
                indicatorTag = "calorie_target_progress",
                modifier = Modifier.weight(1f)
            )
            TargetProgressTile(
                title = "Activity Target",
                progress = animatedActivityProgress,
                percentLabel = "$activityPercent%",
                primaryStat = "$activeWalkMinutes / $walkGoalMinutes min",
                secondaryStat = "$activeWalkCalories / $walkGoalCalories kcal",
                ringColor = EmeraldAccent,
                indicatorTag = "activity_target_progress",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TargetProgressTile(
    title: String,
    progress: Float,
    percentLabel: String,
    primaryStat: String,
    secondaryStat: String,
    ringColor: Color,
    indicatorTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(76.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .size(76.dp)
                        .testTag(indicatorTag),
                    color = ringColor,
                    trackColor = PillSurface,
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = percentLabel,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ringColor
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = primaryStat,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = secondaryStat,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MetabolicContextSelector(
    selected: MetabolicState,
    onSelect: (MetabolicState) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetabolicState.entries.forEach { state ->
                val isSelected = state == selected
                val icon = when (state) {
                    MetabolicState.NORMAL_ENERGY -> Icons.Filled.Bolt
                    MetabolicState.MEDICATION_DRAG -> Icons.Filled.Spa
                    MetabolicState.HIGH_STRESS_LOW_SLEEP -> Icons.Filled.NightsStay
                }
                Surface(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelect(state) }
                        .testTag("metabolic_pill_${state.name.lowercase()}"),
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) VioletSoft else CardSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) VioletBorder else SubtleBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) VioletAccent else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = state.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = true) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = VioletSoft.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, VioletBorder.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selected.headline,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = selected.guidance,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = selected.targetAdjustmentNote,
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = VioletAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiMiniTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
    subValue: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = accent,
                maxLines = 1
            )
            if (subValue != null) {
                Text(
                    text = subValue,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FoodTimelineRow(
    entry: FoodEntryDoc,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val icon = if (entry.categoryEnum == FoodCategory.DRINK_SNACK) {
        Icons.Filled.LocalCafe
    } else {
        Icons.Filled.Restaurant
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AmberSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${entry.caloriesInt} kcal",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = AmberAccent
                    )
                    Text(
                        text = "· ${entry.categoryEnum.shortLabel} · ${DateUtils.formatTime(entry.timestampMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("edit_food_${entry.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.cd_edit_entry),
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_food_${entry.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.cd_delete_entry),
                    tint = RoseAccent,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun WalkTimelineRow(
    entry: WalkEntryDoc,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(EmeraldSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = EmeraldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${entry.paceEnum.displayName} (${entry.durationInt} min)",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val distText = entry.distanceKm?.let { " · ${String.format(java.util.Locale.US, "%.1f", it)} km" } ?: ""
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "-${entry.caloriesInt} kcal",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = EmeraldAccent
                    )
                    Text(
                        text = "$distText · ${DateUtils.formatTime(entry.timestampMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("edit_walk_${entry.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.cd_edit_entry),
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("delete_walk_${entry.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.cd_delete_entry),
                    tint = RoseAccent,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
