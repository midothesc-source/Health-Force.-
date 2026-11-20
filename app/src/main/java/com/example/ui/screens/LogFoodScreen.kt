package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FoodCategory
import com.example.data.model.FoodPresetDoc
import com.example.ui.components.EditPresetDialog
import com.example.ui.components.PulseCard
import com.example.ui.components.darkFieldColors
import com.example.ui.state.TrackerUiState
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.CardSurface
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogFoodScreen(
    uiState: TrackerUiState,
    onTapPreset: (FoodPresetDoc) -> Unit,
    onSavePreset: (FoodPresetDoc) -> Unit,
    onDeletePreset: (String) -> Unit,
    onLogCustomFood: (name: String, calories: Int, category: FoodCategory, saveAsPreset: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var customName by remember { mutableStateOf("") }
    var customCalories by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FoodCategory.LUNCH) }
    var saveAsPreset by remember { mutableStateOf(false) }
    var editingPreset by remember { mutableStateOf<FoodPresetDoc?>(null) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AmberSoft,
                    border = BorderStroke(1.dp, AmberBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Today's Intake",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${uiState.caloriesConsumedToday} kcal logged",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = AmberAccent
                            )
                        }
                        Text(
                            text = "One-tap to log",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Egyptian & Personal Presets",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap card to log · Pencil to edit kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            items(uiState.foodPresets, key = { it.id.ifBlank { it.name } }) { preset ->
                PresetQuickCard(
                    preset = preset,
                    onQuickLog = { onTapPreset(preset) },
                    onEdit = { editingPreset = preset }
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                PulseCard {
                    Text(
                        text = "Custom Meal Entry",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Food or drink name") },
                        placeholder = { Text("e.g., Ful Medames + 1 Balady Bread") },
                        singleLine = true,
                        colors = darkFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_food_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customCalories,
                        onValueChange = { input -> customCalories = input.filter { it.isDigit() } },
                        label = { Text("Calories (kcal)") },
                        placeholder = { Text("e.g., 420") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = darkFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_food_calories_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FoodCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName) },
                                modifier = Modifier.testTag("food_category_${cat.name.lowercase()}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmberSoft,
                                    selectedLabelColor = AmberAccent,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { saveAsPreset = !saveAsPreset }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = saveAsPreset,
                            onCheckedChange = { saveAsPreset = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = EmeraldAccent,
                                uncheckedColor = TextSecondary,
                                checkmarkColor = SlateBackground
                            ),
                            modifier = Modifier.testTag("save_as_preset_checkbox")
                        )
                        Text(
                            text = "+ Save as Preset for one-tap logging",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val validCalories = customCalories.toIntOrNull()
                    val canSubmit = customName.isNotBlank() && validCalories != null && validCalories > 0
                    Button(
                        onClick = {
                            if (canSubmit && validCalories != null) {
                                onLogCustomFood(customName.trim(), validCalories, selectedCategory, saveAsPreset)
                                customName = ""
                                customCalories = ""
                                saveAsPreset = false
                            }
                        },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("log_custom_food_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = SlateBackground,
                            disabledContainerColor = CardSurface,
                            disabledContentColor = TextMuted
                        )
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Custom Food", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    editingPreset?.let { preset ->
        EditPresetDialog(
            preset = preset,
            onDismiss = { editingPreset = null },
            onSave = { updated ->
                onSavePreset(updated)
                editingPreset = null
            },
            onDelete = { id ->
                onDeletePreset(id)
                editingPreset = null
            }
        )
    }
}

@Composable
private fun PresetQuickCard(
    preset: FoodPresetDoc,
    onQuickLog: () -> Unit,
    onEdit: () -> Unit
) {
    val icon = if (preset.categoryEnum == FoodCategory.DRINK_SNACK) {
        Icons.Filled.LocalCafe
    } else {
        Icons.Filled.Restaurant
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onQuickLog)
            .testTag("preset_card_${preset.id}"),
        shape = RoundedCornerShape(16.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
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
                    text = preset.name,
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
                        text = "~${preset.caloriesInt} kcal",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AmberAccent
                    )
                    Text(
                        text = "· ${preset.categoryEnum.shortLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AmberSoft
            ) {
                Text(
                    text = "+ Log",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberAccent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("edit_preset_${preset.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.cd_edit_preset),
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
