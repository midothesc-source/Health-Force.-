package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FoodCategory
import com.example.data.model.FoodEntryDoc
import com.example.data.model.FoodPresetDoc
import com.example.data.model.WalkEntryDoc
import com.example.data.model.WalkPace
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PillSurface
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.StrongBorder
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun PulseCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SubtleBorder,
    backgroundColor: Color = CardSurface,
    cornerRadius: Dp = 20.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun TopPulseHeader(
    dateLabel: String,
    streakDays: Int,
    onOpenThemePicker: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(EmeraldAccent, AmberAccent))
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HealthForce",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (dateLabel.isNotBlank()) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.testTag("streak_badge"),
                shape = RoundedCornerShape(50),
                color = EmeraldSoft,
                border = BorderStroke(1.dp, EmeraldBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = stringResource(R.string.cd_streak),
                        tint = EmeraldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${streakDays}d",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = EmeraldAccent
                    )
                }
            }

            IconButton(
                onClick = onOpenThemePicker,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CardSurface)
                    .border(1.dp, SubtleBorder, CircleShape)
                    .testTag("theme_picker_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = stringResource(R.string.cd_theme_picker),
                    tint = EmeraldAccent
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CardSurface)
                    .border(1.dp, SubtleBorder, CircleShape)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = TextPrimary
                )
            }
        }
    }
}

@Composable
fun BalanceEffortRing(
    caloriesConsumed: Int,
    totalBurn: Int,
    netBalance: Int,
    effortScore: Int,
    modifier: Modifier = Modifier
) {
    val intakeRatio = (caloriesConsumed.toFloat() / totalBurn.coerceAtLeast(1)).coerceIn(0f, 1.15f)
    val effortRatio = (effortScore / 100f).coerceIn(0f, 1f)

    val animatedIntakeSweep by animateFloatAsState(
        targetValue = (intakeRatio.coerceAtMost(1f)) * 270f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "intake_sweep"
    )
    val animatedEffortSweep by animateFloatAsState(
        targetValue = effortRatio * 270f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "effort_sweep"
    )

    val isDeficit = netBalance <= 0
    val netColor = when {
        netBalance <= -100 -> EmeraldAccent
        netBalance <= 150 -> AmberAccent
        else -> RoseAccent
    }
    val netHeadline = if (isDeficit) "-${abs(netBalance)}" else "+$netBalance"
    val netSubLabel = if (isDeficit) "kcal deficit" else "kcal surplus"

    val trackColor = PillSurface
    val amberColor = AmberAccent
    val roseColor = RoseAccent
    val emeraldColor = EmeraldAccent

    Box(
        modifier = modifier.size(164.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(164.dp)) {
            val outerStroke = 12.dp.toPx()
            val innerStroke = 7.dp.toPx()
            val outerInset = outerStroke / 2f + 2.dp.toPx()
            val innerInset = outerInset + outerStroke / 2f + 7.dp.toPx() + innerStroke / 2f

            val outerSize = Size(size.width - outerInset * 2, size.height - outerInset * 2)
            val innerSize = Size(size.width - innerInset * 2, size.height - innerInset * 2)

            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(outerInset, outerInset),
                size = outerSize,
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(amberColor, if (intakeRatio > 1.02f) roseColor else amberColor)
                ),
                startAngle = 135f,
                sweepAngle = animatedIntakeSweep,
                useCenter = false,
                topLeft = Offset(outerInset, outerInset),
                size = outerSize,
                style = Stroke(width = outerStroke, cap = StrokeCap.Round)
            )

            drawArc(
                color = trackColor.copy(alpha = 0.7f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(innerInset, innerInset),
                size = innerSize,
                style = Stroke(width = innerStroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = emeraldColor,
                startAngle = 135f,
                sweepAngle = animatedEffortSweep,
                useCenter = false,
                topLeft = Offset(innerInset, innerInset),
                size = innerSize,
                style = Stroke(width = innerStroke, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = netHeadline,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = netColor
            )
            Text(
                text = netSubLabel,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = EmeraldSoft,
                border = BorderStroke(1.dp, EmeraldBorder)
            ) {
                Text(
                    text = "Effort $effortScore%",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = EmeraldAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun darkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = EmeraldAccent,
    unfocusedBorderColor = StrongBorder,
    focusedLabelColor = EmeraldAccent,
    unfocusedLabelColor = TextSecondary,
    cursorColor = EmeraldAccent,
    focusedContainerColor = SlateBackground,
    unfocusedContainerColor = SlateBackground
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditFoodEntryDialog(
    entry: FoodEntryDoc,
    onDismiss: () -> Unit,
    onSave: (FoodEntryDoc) -> Unit
) {
    var name by remember(entry) { mutableStateOf(entry.name) }
    var caloriesText by remember(entry) { mutableStateOf(entry.caloriesInt.toString()) }
    var category by remember(entry) { mutableStateOf(entry.categoryEnum) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text("Edit Meal Entry", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Meal name") },
                    singleLine = true,
                    colors = darkFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { input -> caloriesText = input.filter { it.isDigit() } },
                    label = { Text("Calories (kcal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = darkFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FoodCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.shortLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberAccent.copy(alpha = 0.2f),
                                selectedLabelColor = AmberAccent,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val kcal = (caloriesText.toIntOrNull() ?: entry.caloriesInt).toLong()
                    if (name.isNotBlank()) {
                        onSave(entry.copy(name = name.trim(), calories = kcal, category = category.name))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = SlateBackground)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditPresetDialog(
    preset: FoodPresetDoc,
    onDismiss: () -> Unit,
    onSave: (FoodPresetDoc) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember(preset) { mutableStateOf(preset.name) }
    var caloriesText by remember(preset) { mutableStateOf(preset.caloriesInt.toString()) }
    var category by remember(preset) { mutableStateOf(preset.categoryEnum) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text("Edit Quick Preset", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Preset name") },
                    colors = darkFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { input -> caloriesText = input.filter { it.isDigit() } },
                    label = { Text("Calories (kcal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = darkFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FoodCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.shortLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberAccent.copy(alpha = 0.2f),
                                selectedLabelColor = AmberAccent,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
                TextButton(
                    onClick = { onDelete(preset.id) },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("Delete Preset", color = RoseAccent)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val kcal = (caloriesText.toIntOrNull() ?: preset.caloriesInt).toLong()
                    if (name.isNotBlank()) {
                        onSave(preset.copy(name = name.trim(), calories = kcal, category = category.name))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = SlateBackground)
            ) {
                Text("Update Preset", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditWalkEntryDialog(
    entry: WalkEntryDoc,
    onDismiss: () -> Unit,
    onSave: (WalkEntryDoc) -> Unit
) {
    var durationText by remember(entry) { mutableStateOf(entry.durationInt.toString()) }
    var caloriesText by remember(entry) { mutableStateOf(entry.caloriesInt.toString()) }
    var distanceText by remember(entry) { mutableStateOf(entry.distanceKm?.toString() ?: "") }
    var pace by remember(entry) { mutableStateOf(entry.paceEnum) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurfaceElevated,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = { Text("Edit Walk Session", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { input -> durationText = input.filter { it.isDigit() } },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = darkFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { input -> caloriesText = input.filter { it.isDigit() } },
                    label = { Text("Active calories burned (kcal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = darkFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = distanceText,
                    onValueChange = { input -> distanceText = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Distance km (optional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = darkFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WalkPace.entries.forEach { p ->
                        FilterChip(
                            selected = pace == p,
                            onClick = { pace = p },
                            label = { Text(p.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldSoft,
                                selectedLabelColor = EmeraldAccent,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = (durationText.toIntOrNull() ?: entry.durationInt).coerceAtLeast(1).toLong()
                    val kcal = (caloriesText.toIntOrNull() ?: entry.caloriesInt).coerceAtLeast(0).toLong()
                    val dist = distanceText.toDoubleOrNull()
                    onSave(
                        entry.copy(
                            durationMinutes = mins,
                            pace = pace.name,
                            distanceKm = dist,
                            caloriesBurned = kcal,
                            manualCalories = kcal != entry.caloriesBurned || entry.manualCalories
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = SlateBackground)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
