package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.TrackerRepository
import com.example.ui.TrackerViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutUser
import com.example.ui.components.SettingsModalSheet
import com.example.ui.components.ThemePickerModalSheet
import com.example.ui.components.TopPulseHeader
import com.example.ui.screens.LogFoodScreen
import com.example.ui.screens.LogWalkScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.TrendsScreen
import com.example.ui.state.AppTab
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AppThemeOption
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.HealthForceTheme
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThemePreferences
import com.example.ui.theme.VioletAccent
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )
        setContent {
            val context = LocalContext.current
            var currentTheme by remember { mutableStateOf(ThemePreferences.loadTheme(context)) }
            val onSelectTheme: (AppThemeOption) -> Unit = { selected ->
                currentTheme = selected
                ThemePreferences.saveTheme(context, selected)
            }

            HealthForceTheme(themeOption = currentTheme) {
                AppNavigation(
                    currentTheme = currentTheme,
                    onSelectTheme = onSelectTheme
                )
            }
        }
    }
}

@Composable
fun AppNavigation(
    currentTheme: AppThemeOption,
    onSelectTheme: (AppThemeOption) -> Unit
) {
    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    var showThemePicker by rememberSaveable { mutableStateOf(false) }

    if (user == null) {
        AuthScreen(
            onOpenThemePicker = { showThemePicker = true },
            onAuthSuccess = { /* AuthStateListener drives navigation */ }
        )
    } else {
        key(user.uid) {
            val viewModel: TrackerViewModel = viewModel(
                key = user.uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        TrackerViewModel(
                            repository = TrackerRepository(db, Firebase.auth),
                            currentUserId = user.uid
                        )
                    }
                }
            )
            HealthForceApp(
                viewModel = viewModel,
                userEmail = user.email,
                userDisplayName = user.displayName,
                currentTheme = currentTheme,
                onSelectTheme = onSelectTheme,
                onOpenThemePicker = { showThemePicker = true }
            )
        }
    }

    if (showThemePicker) {
        ThemePickerModalSheet(
            currentTheme = currentTheme,
            onSelectTheme = onSelectTheme,
            onDismiss = { showThemePicker = false }
        )
    }
}

@Composable
fun HealthForceApp(
    viewModel: TrackerViewModel,
    userEmail: String?,
    userDisplayName: String?,
    currentTheme: AppThemeOption,
    onSelectTheme: (AppThemeOption) -> Unit,
    onOpenThemePicker: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.TODAY) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val glowPrimary = EmeraldAccent
    val glowTertiary = VioletAccent

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    BackHandler(enabled = selectedTab != AppTab.TODAY && !showSettings) {
        selectedTab = AppTab.TODAY
    }

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val jsonResult = viewModel.exportBackupJson()
                jsonResult.onSuccess { json ->
                    val writeResult = withContext(Dispatchers.IO) {
                        runCatching {
                            context.contentResolver.openOutputStream(uri)?.use { out ->
                                out.write(json.toByteArray(Charsets.UTF_8))
                            } ?: error("Unable to open output stream")
                        }
                    }
                    writeResult.onSuccess {
                        snackbarHostState.showSnackbar("Backup exported to JSON file")
                    }.onFailure { err ->
                        snackbarHostState.showSnackbar("Export failed: ${err.message}")
                    }
                }.onFailure { err ->
                    snackbarHostState.showSnackbar("Export failed: ${err.message}")
                }
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val readResult = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                            it.readText()
                        } ?: error("Unable to read selected file")
                    }
                }
                readResult.onSuccess { json ->
                    viewModel.importBackupJson(json)
                    showSettings = false
                }.onFailure { err ->
                    snackbarHostState.showSnackbar("Import failed: ${err.message}")
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowPrimary.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.14f, size.height * 0.10f),
                        radius = size.maxDimension * 0.48f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowTertiary.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.85f),
                        radius = size.maxDimension * 0.45f
                    )
                )
            },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CardSurfaceElevated,
                    contentColor = TextPrimary,
                    actionColor = EmeraldAccent
                )
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = SubtleBorder)
                NavigationBar(
                    containerColor = CardSurface,
                    contentColor = TextPrimary,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    AppTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val activeColor = when (tab) {
                            AppTab.LOG_FOOD -> AmberAccent
                            else -> EmeraldAccent
                        }
                        val icon = when (tab) {
                            AppTab.TODAY -> if (isSelected) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
                            AppTab.LOG_FOOD -> if (isSelected) Icons.Filled.Restaurant else Icons.Outlined.Restaurant
                            AppTab.LOG_WALK -> if (isSelected) Icons.AutoMirrored.Filled.DirectionsWalk else Icons.AutoMirrored.Outlined.DirectionsWalk
                            AppTab.TRENDS -> if (isSelected) Icons.Filled.Analytics else Icons.Outlined.Analytics
                        }
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}"),
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = activeColor,
                                selectedTextColor = activeColor,
                                indicatorColor = EmeraldSoft.copy(alpha = 0.7f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            TopPulseHeader(
                dateLabel = uiState.todayFormattedDate,
                streakDays = uiState.consistencyStreakDays,
                onOpenThemePicker = onOpenThemePicker,
                onOpenSettings = { showSettings = true }
            )

            Box(modifier = Modifier.weight(1f)) {
                Crossfade(targetState = selectedTab, label = "tab_crossfade") { tab ->
                    when (tab) {
                        AppTab.TODAY -> TodayScreen(
                            uiState = uiState,
                            onSelectMetabolicState = viewModel::setMetabolicState,
                            onUpdateFood = viewModel::updateFoodEntry,
                            onDeleteFood = viewModel::deleteFoodEntry,
                            onUpdateWalk = viewModel::updateWalkEntry,
                            onDeleteWalk = viewModel::deleteWalkEntry,
                            onQuickNavigateFood = { selectedTab = AppTab.LOG_FOOD },
                            onQuickNavigateWalk = { selectedTab = AppTab.LOG_WALK }
                        )
                        AppTab.LOG_FOOD -> LogFoodScreen(
                            uiState = uiState,
                            onTapPreset = viewModel::logFoodFromPreset,
                            onSavePreset = viewModel::savePreset,
                            onDeletePreset = viewModel::deletePreset,
                            onLogCustomFood = viewModel::logCustomFood
                        )
                        AppTab.LOG_WALK -> LogWalkScreen(
                            uiState = uiState,
                            onLogWalk = viewModel::logWalk
                        )
                        AppTab.TRENDS -> TrendsScreen(
                            uiState = uiState,
                            onSaveWeightCheckIn = viewModel::saveWeightCheckIn
                        )
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsModalSheet(
            userEmail = userEmail,
            userDisplayName = userDisplayName,
            settings = uiState.settings,
            currentTheme = currentTheme,
            onSelectTheme = onSelectTheme,
            onDismiss = { showSettings = false },
            onSaveSettings = { weight, bmr, neat, walkMins, walkKcal ->
                viewModel.updateSettings(weight, bmr, neat, walkMins, walkKcal)
                showSettings = false
            },
            onExportToFile = {
                exportFileLauncher.launch("healthforce_backup_${System.currentTimeMillis()}.json")
            },
            onCopyJsonBackup = {
                coroutineScope.launch {
                    viewModel.exportBackupJson().onSuccess { json ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("HealthForce Backup JSON", json))
                        snackbarHostState.showSnackbar("JSON backup copied to clipboard")
                    }.onFailure { err ->
                        snackbarHostState.showSnackbar("Export failed: ${err.message}")
                    }
                }
            },
            onImportFromFile = {
                importFileLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            },
            onPasteJsonBackup = { json ->
                viewModel.importBackupJson(json)
                showSettings = false
            },
            onClearSampleData = {
                viewModel.clearSampleDataAndStartFresh()
                showSettings = false
            },
            onRestoreSampleData = {
                viewModel.restoreSampleData()
                showSettings = false
            },
            onSignOut = {
                showSettings = false
                signOutUser(credentialManager, coroutineScope)
            }
        )
    }
}
