package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WalkPace
import com.example.data.model.calculateWalkActiveCalories
import com.example.data.model.estimateDistanceKm
import com.example.ui.components.PulseCard
import com.example.ui.components.darkFieldColors
import com.example.ui.state.TrackerUiState
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PillSurface
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LogWalkScreen(
    uiState: TrackerUiState,
    onLogWalk: (durationMinutes: Int, pace: WalkPace, distanceKm: Double?, manualCalories: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var durationText by remember { mutableStateOf("30") }
    var selectedPace by remember { mutableStateOf(WalkPace.BRISK_WALK) }
    var distanceText by remember { mutableStateOf("") }
    var manualCaloriesText by remember { mutableStateOf("") }

    val durationMinutes = (durationText.toIntOrNull() ?: 0).coerceIn(0, 600)
    val autoKcal = calculateWalkActiveCalories(
        durationMinutes = durationMinutes,
        pace = selectedPace,
        bodyWeightKg = uiState.settings.bodyWeightKg
    )
    val autoDistance = estimateDistanceKm(durationMinutes, selectedPace)
    val manualKcal = manualCaloriesText.toIntOrNull()
    val effectiveKcal = manualKcal ?: autoKcal

    val goalMins = uiState.settings.dailyWalkGoalMinutes.coerceAtLeast(1)
    val goalKcal = uiState.settings.dailyWalkGoalKcal.coerceAtLeast(1)
    val minsProgress = (uiState.activeWalkMinutesToday.toFloat() / goalMins).coerceIn(0f, 1f)
    val kcalProgress = (uiState.activeWalkBurnToday.toFloat() / goalKcal).coerceIn(0f, 1f)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Daily Movement Goal Bar Card
            item {
                PulseCard(borderColor = EmeraldBorder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Movement Goal",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = EmeraldSoft
                        ) {
                            Text(
                                text = "${(minsProgress * 100).toInt()}%",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = EmeraldAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Minutes progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Active Minutes",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${uiState.activeWalkMinutesToday} / $goalMins min",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = EmeraldAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { minsProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = EmeraldAccent,
                        trackColor = PillSurface,
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Active kcal progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Active Walk Burn",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${uiState.activeWalkBurnToday} / $goalKcal kcal",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = EmeraldAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { kcalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50)),
                        color = EmeraldAccent,
                        trackColor = PillSurface,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            // Quick Walk Logger Card
            item {
                PulseCard {
                    Text(
                        text = "Quick Walk Logger",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Auto-calculates active burn for ${String.format(java.util.Locale.US, "%.1f", uiState.settings.bodyWeightKg)} kg or override from your fitness tracker.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // One-handed Quick Duration Chips
                    Text(
                        text = "Duration (minutes)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { presetMins ->
                            val isSelected = durationMinutes == presetMins
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { durationText = presetMins.toString() }
                                    .testTag("walk_preset_${presetMins}m"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) EmeraldSoft else CardSurfaceElevated,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) EmeraldBorder else SubtleBorder
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${presetMins}m",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EmeraldAccent else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stepper + Direct minutes input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val next = (durationMinutes - 5).coerceAtLeast(5)
                                durationText = next.toString()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("walk_minus_5")
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease 5 minutes", tint = TextPrimary)
                        }

                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { input -> durationText = input.filter { it.isDigit() } },
                            label = { Text("Minutes") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = darkFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("walk_duration_input")
                        )

                        IconButton(
                            onClick = {
                                val next = (durationMinutes + 5).coerceAtMost(300)
                                durationText = next.toString()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .testTag("walk_plus_5")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase 5 minutes", tint = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pace Selector
                    Text(
                        text = "Pace",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WalkPace.entries.forEach { pace ->
                            val isSelected = selectedPace == pace
                            val estForPace = calculateWalkActiveCalories(
                                durationMinutes = durationMinutes,
                                pace = pace,
                                bodyWeightKg = uiState.settings.bodyWeightKg
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedPace = pace }
                                    .testTag("walk_pace_${pace.name.lowercase()}"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) EmeraldSoft else CardSurfaceElevated,
                                border = BorderStroke(1.dp, if (isSelected) EmeraldBorder else SubtleBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pace.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (isSelected) EmeraldAccent else TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = pace.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = "~$estForPace kcal",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) EmeraldAccent else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Optional Distance & Manual Calories Override
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = distanceText,
                            onValueChange = { input -> distanceText = input.filter { it.isDigit() || it == '.' } },
                            label = { Text("Distance km (opt)") },
                            placeholder = { Text(String.format(java.util.Locale.US, "%.1f", autoDistance)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = darkFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("walk_distance_input")
                        )
                        OutlinedTextField(
                            value = manualCaloriesText,
                            onValueChange = { input -> manualCaloriesText = input.filter { it.isDigit() } },
                            label = { Text("Override kcal (opt)") },
                            placeholder = { Text("$autoKcal") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = darkFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("walk_manual_calories_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Burn Preview + Submit Button
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (manualKcal != null) "Using tracker override" else "Auto-estimated burn",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "$effectiveKcal active kcal",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = EmeraldAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (durationMinutes > 0) {
                                val dist = distanceText.toDoubleOrNull() ?: autoDistance
                                onLogWalk(durationMinutes, selectedPace, dist, manualKcal)
                                distanceText = ""
                                manualCaloriesText = ""
                            }
                        },
                        enabled = durationMinutes > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("log_walk_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldAccent,
                            contentColor = SlateBackground,
                            disabledContainerColor = CardSurface,
                            disabledContentColor = TextMuted
                        )
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsWalk,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Walk · $effectiveKcal kcal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
