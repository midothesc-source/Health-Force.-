package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.UserSettings
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AppThemeOption
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModalSheet(
    userEmail: String?,
    userDisplayName: String?,
    settings: UserSettings,
    currentTheme: AppThemeOption,
    onSelectTheme: (AppThemeOption) -> Unit,
    onDismiss: () -> Unit,
    onSaveSettings: (weightKg: Double, bmr: Int, neat: Int, walkMins: Int, walkKcal: Int) -> Unit,
    onExportToFile: () -> Unit,
    onCopyJsonBackup: () -> Unit,
    onImportFromFile: () -> Unit,
    onPasteJsonBackup: (String) -> Unit,
    onClearSampleData: () -> Unit,
    onRestoreSampleData: () -> Unit,
    onSignOut: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var weightText by remember(settings) { mutableStateOf(settings.bodyWeightKg.toString()) }
    var bmrText by remember(settings) { mutableStateOf(settings.estimatedBmrKcal.toString()) }
    var neatText by remember(settings) { mutableStateOf(settings.baseNeatKcal.toString()) }
    var walkMinsText by remember(settings) { mutableStateOf(settings.dailyWalkGoalMinutes.toString()) }
    var walkKcalText by remember(settings) { mutableStateOf(settings.dailyWalkGoalKcal.toString()) }

    var showPasteDialog by remember { mutableStateOf(false) }
    var showConfirmClear by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardSurfaceElevated,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Settings & Cloud Backup",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )

            // Signed-in Account Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, SubtleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountCircle,
                            contentDescription = null,
                            tint = EmeraldAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = userDisplayName?.takeIf { it.isNotBlank() } ?: "Google Account",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = userEmail ?: "Synced to Cloud Firestore",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = null,
                            tint = RoseAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sign Out", color = RoseAccent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Theme Studio Section
            Text(
                text = "App Theme (${currentTheme.displayName})",
                style = MaterialTheme.typography.titleSmall,
                color = VioletAccent
            )
            ThemeSelectorGrid(
                currentTheme = currentTheme,
                onSelectTheme = onSelectTheme
            )

            HorizontalDivider(color = SubtleBorder)

            // Metabolism & Goals Section
            Text(
                text = "Metabolism & Daily Goals",
                style = MaterialTheme.typography.titleSmall,
                color = EmeraldAccent
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = darkFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bmrText,
                    onValueChange = { bmrText = it.filter { c -> c.isDigit() } },
                    label = { Text("BMR (kcal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = darkFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = neatText,
                    onValueChange = { neatText = it.filter { c -> c.isDigit() } },
                    label = { Text("Base NEAT (kcal)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = darkFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = walkMinsText,
                    onValueChange = { walkMinsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Walk Goal (min)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = darkFieldColors(),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = walkKcalText,
                onValueChange = { walkKcalText = it.filter { c -> c.isDigit() } },
                label = { Text("Daily Active Walk Goal (kcal)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = darkFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val w = weightText.toDoubleOrNull() ?: settings.bodyWeightKg
                    val bmr = bmrText.toIntOrNull() ?: settings.estimatedBmrKcal
                    val neat = neatText.toIntOrNull() ?: settings.baseNeatKcal
                    val mins = walkMinsText.toIntOrNull() ?: settings.dailyWalkGoalMinutes
                    val kcal = walkKcalText.toIntOrNull() ?: settings.dailyWalkGoalKcal
                    onSaveSettings(w, bmr, neat, mins, kcal)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAccent,
                    contentColor = SlateBackground
                )
            ) {
                Text("Save Targets", fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(color = SubtleBorder)

            // JSON Backup Section
            Text(
                text = "JSON Export & Import Backup",
                style = MaterialTheme.typography.titleSmall,
                color = AmberAccent
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onExportToFile,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_json_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, EmeraldBorder)
                ) {
                    Icon(
                        Icons.Outlined.FileDownload,
                        contentDescription = null,
                        tint = EmeraldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Export JSON", color = EmeraldAccent, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onImportFromFile,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("import_json_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AmberBorder)
                ) {
                    Icon(
                        Icons.Outlined.FileUpload,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Import JSON", color = AmberAccent, maxLines = 1)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onCopyJsonBackup,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_json_button")
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Copy JSON", color = TextSecondary)
                }
                TextButton(
                    onClick = { showPasteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("paste_json_button")
                ) {
                    Text("Paste JSON Backup", color = VioletAccent)
                }
            }

            HorizontalDivider(color = SubtleBorder)

            // Sample Data Controls
            Text(
                text = "Sample Data",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary
            )
            OutlinedButton(
                onClick = { showConfirmClear = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("clear_sample_data_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, RoseAccent.copy(alpha = 0.45f))
            ) {
                Icon(
                    Icons.Outlined.DeleteSweep,
                    contentDescription = null,
                    tint = RoseAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Clear Sample Data & Start Fresh",
                    color = RoseAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!settings.isSampleDataActive) {
                TextButton(
                    onClick = onRestoreSampleData,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(48.dp)
                        .testTag("restore_sample_data_button")
                ) {
                    Icon(
                        Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Reload 7-Day Sample Data", color = TextSecondary)
                }
            }
        }
    }

    if (showConfirmClear) {
        AlertDialog(
            onDismissRequest = { showConfirmClear = false },
            containerColor = CardSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Start Fresh?") },
            text = {
                Text("This clears all logged meals, walks, weight check-ins, and daily states while keeping your Egyptian & personal food presets intact.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmClear = false
                        onClearSampleData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseAccent, contentColor = SlateBackground)
                ) {
                    Text("Clear Logs", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClear = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showPasteDialog) {
        var jsonInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            containerColor = CardSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextPrimary,
            title = { Text("Paste JSON Backup") },
            text = {
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    label = { Text("HealthForce JSON") },
                    minLines = 5,
                    maxLines = 8,
                    colors = darkFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (jsonInput.isNotBlank()) {
                            onPasteJsonBackup(jsonInput)
                            showPasteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = SlateBackground)
                ) {
                    Text("Restore", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
