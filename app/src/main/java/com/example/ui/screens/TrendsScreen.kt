package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WeightTag
import com.example.ui.components.PulseCard
import com.example.ui.components.darkFieldColors
import com.example.ui.state.DayEffortSummary
import com.example.ui.state.TrackerUiState
import com.example.ui.state.WeightTrendPoint
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBorder
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CardSurfaceElevated
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.PillSurface
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.ui.theme.VioletSoft
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrendsScreen(
    uiState: TrackerUiState,
    onSaveWeightCheckIn: (weightKg: Double, tag: WeightTag) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialWeight = uiState.todayWeightCheckIn?.weightKg ?: uiState.latestKnownWeightKg
    var weightText by remember(uiState.todayWeightCheckIn) {
        mutableStateOf(String.format(Locale.US, "%.1f", initialWeight))
    }
    var selectedTag by remember(uiState.todayWeightCheckIn) {
        mutableStateOf(uiState.todayWeightCheckIn?.tagEnum ?: WeightTag.NORMAL)
    }

    LaunchedEffect(uiState.latestKnownWeightKg, uiState.todayWeightCheckIn) {
        if (uiState.todayWeightCheckIn != null) {
            weightText = String.format(Locale.US, "%.1f", uiState.todayWeightCheckIn.weightKg)
            selectedTag = uiState.todayWeightCheckIn.tagEnum
        }
    }

    val currentParsedWeight = weightText.toDoubleOrNull() ?: initialWeight

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Daily Weight Check-in Card right above the chart
            item {
                PulseCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Weight Check-In",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        if (uiState.todayWeightCheckIn != null) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = EmeraldSoft
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Logged",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldAccent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val next = ((currentParsedWeight - 0.1) * 10.0).roundToInt() / 10.0
                                weightText = String.format(Locale.US, "%.1f", next.coerceAtLeast(30.0))
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("weight_minus_button")
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease 0.1 kg", tint = TextPrimary)
                        }

                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { input -> weightText = input.filter { it.isDigit() || it == '.' } },
                            label = { Text("Scale weight (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = darkFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("weight_input")
                        )

                        IconButton(
                            onClick = {
                                val next = ((currentParsedWeight + 0.1) * 10.0).roundToInt() / 10.0
                                weightText = String.format(Locale.US, "%.1f", next.coerceAtMost(250.0))
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("weight_plus_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase 0.1 kg", tint = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Context Tag (optional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WeightTag.entries.forEach { tag ->
                            val tagColor = when (tag) {
                                WeightTag.NORMAL -> EmeraldAccent
                                WeightTag.BLOATED_WATER_RETENTION -> VioletAccent
                                WeightTag.POST_MEAL -> AmberAccent
                            }
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { selectedTag = tag },
                                label = { Text(tag.displayName, maxLines = 1) },
                                modifier = Modifier.testTag("weight_tag_${tag.name.lowercase()}"),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tagColor.copy(alpha = 0.20f),
                                    selectedLabelColor = tagColor,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            weightText.toDoubleOrNull()?.let { w ->
                                onSaveWeightCheckIn(w, selectedTag)
                            }
                        },
                        enabled = weightText.toDoubleOrNull() != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_weight_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = SlateBackground
                        )
                    ) {
                        Text("Save Check-In", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. Dual-Layer Weight Chart Card
            item {
                DualLayerWeightChartCard(points = uiState.weightTrendPoints)
            }

            // 3. Scale Noise vs. Real Trend Insight Card
            item {
                val insight = uiState.scaleNoiseInsight
                PulseCard(
                    borderColor = if (insight.isWaterNoiseDetected) AmberBorder else SubtleBorder
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(VioletSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoGraph,
                                contentDescription = null,
                                tint = VioletAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = insight.badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = VioletAccent
                            )
                            Text(
                                text = "Scale Noise vs. Real Trend",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = insight.headline,
                        style = MaterialTheme.typography.titleSmall,
                        color = AmberAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = insight.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    if (insight.latestRawKg != null || insight.latestRollingKg != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = CardSurfaceElevated
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Latest Raw", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = insight.latestRawKg?.let { String.format(Locale.US, "%.1f kg", it) } ?: "--",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = AmberSoft
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("7d Rolling Avg", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = insight.latestRollingKg?.let { String.format(Locale.US, "%.1f kg", it) } ?: "--",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = AmberAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. 7-Day Effort Summary Bar Chart
            item {
                SevenDayEffortSummaryCard(
                    days = uiState.sevenDayEffortSummary,
                    walkGoalKcal = uiState.settings.dailyWalkGoalKcal
                )
            }
        }
    }
}

@Composable
private fun DualLayerWeightChartCard(
    points: List<WeightTrendPoint>
) {
    val validWeights = points.mapNotNull { it.rawWeightKg } + points.mapNotNull { it.rollingAvgKg }
    var selectedIdx by remember(points) {
        mutableIntStateOf((points.indexOfLast { it.rawWeightKg != null }).coerceAtLeast(0))
    }

    PulseCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "7-Day Weight Trend",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Rolling average filters single-day water spikes",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 3.dp)
                        .background(AmberAccent, RoundedCornerShape(2.dp))
                )
                Text("7d Rolling Avg", style = MaterialTheme.typography.bodySmall, color = AmberAccent)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(TextSecondary)
                )
                Text("Raw Scale", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VioletAccent)
                )
                Text("Water/Meal Tag", style = MaterialTheme.typography.bodySmall, color = VioletAccent)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (validWeights.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(CardSurfaceElevated, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log your first weight check-in above to plot your 7-day curve.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            val minW = (validWeights.minOrNull() ?: 70.0) - 0.5
            val maxW = (validWeights.maxOrNull() ?: 90.0) + 0.5
            val span = (maxW - minW).coerceAtLeast(1.0)

            // Selected Day Inspector Pill
            val activePoint = points.getOrNull(selectedIdx)
            if (activePoint != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, SubtleBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activePoint.shortLabel} (${activePoint.dayMonthLabel})",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Raw: ${activePoint.rawWeightKg?.let { String.format(Locale.US, "%.1f", it) } ?: "--"}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Avg: ${activePoint.rollingAvgKg?.let { String.format(Locale.US, "%.1f", it) } ?: "--"}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AmberAccent
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                // Y-axis labels
                Column(
                    modifier = Modifier
                        .height(170.dp)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f", maxW),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", (minW + maxW) / 2.0),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", minW),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }

                // Dual-layer Canvas
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(170.dp)
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                if (points.isNotEmpty()) {
                                    val step = size.width / points.size.coerceAtLeast(1).toFloat()
                                    val idx = (offset.x / step).toInt().coerceIn(0, points.lastIndex)
                                    selectedIdx = idx
                                }
                            }
                        }
                ) {
                    val padH = 12.dp.toPx()
                    val padV = 12.dp.toPx()
                    val chartW = (size.width - padH * 2).coerceAtLeast(1f)
                    val chartH = (size.height - padV * 2).coerceAtLeast(1f)

                    // Horizontal subtle grid lines
                    listOf(0f, 0.5f, 1f).forEach { frac ->
                        val y = padV + chartH * frac
                        drawLine(
                            color = SubtleBorder,
                            start = Offset(padH, y),
                            end = Offset(size.width - padH, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    fun xFor(index: Int): Float {
                        if (points.size <= 1) return padH + chartW / 2f
                        return padH + (index.toFloat() / (points.size - 1)) * chartW
                    }

                    fun yFor(weight: Double): Float {
                        val norm = ((weight - minW) / span).toFloat().coerceIn(0f, 1f)
                        return padV + chartH * (1f - norm)
                    }

                    // 1) Raw Scale Weight dotted translucent path
                    val rawPath = Path()
                    var rawStarted = false
                    points.forEachIndexed { idx, pt ->
                        pt.rawWeightKg?.let { w ->
                            val p = Offset(xFor(idx), yFor(w))
                            if (!rawStarted) {
                                rawPath.moveTo(p.x, p.y)
                                rawStarted = true
                            } else {
                                rawPath.lineTo(p.x, p.y)
                            }
                        }
                    }
                    if (rawStarted) {
                        drawPath(
                            path = rawPath,
                            color = TextSecondary.copy(alpha = 0.50f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    }

                    // 2) Bold 7-Day Rolling Moving Average smooth line
                    val avgPoints = points.mapIndexedNotNull { idx, pt ->
                        pt.rollingAvgKg?.let { Offset(xFor(idx), yFor(it)) }
                    }
                    if (avgPoints.isNotEmpty()) {
                        val avgPath = Path().apply {
                            moveTo(avgPoints.first().x, avgPoints.first().y)
                            for (i in 1 until avgPoints.size) {
                                val prev = avgPoints[i - 1]
                                val curr = avgPoints[i]
                                val midX = (prev.x + curr.x) / 2f
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }
                        drawPath(
                            path = avgPath,
                            color = AmberAccent,
                            style = Stroke(
                                width = 3.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // 3) Dots for raw scale + rolling average
                    points.forEachIndexed { idx, pt ->
                        val x = xFor(idx)
                        if (idx == selectedIdx) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.16f),
                                start = Offset(x, padV),
                                end = Offset(x, size.height - padV),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                        pt.rawWeightKg?.let { w ->
                            val dotColor = when (pt.tag) {
                                com.example.data.model.WeightTag.BLOATED_WATER_RETENTION -> VioletAccent
                                com.example.data.model.WeightTag.POST_MEAL -> AmberAccent.copy(alpha = 0.7f)
                                else -> TextSecondary
                            }
                            drawCircle(
                                color = dotColor,
                                radius = if (idx == selectedIdx) 5.dp.toPx() else 3.8.dp.toPx(),
                                center = Offset(x, yFor(w))
                            )
                        }
                        pt.rollingAvgKg?.let { avg ->
                            drawCircle(
                                color = SlateBackground,
                                radius = 4.dp.toPx(),
                                center = Offset(x, yFor(avg))
                            )
                            drawCircle(
                                color = AmberAccent,
                                radius = 2.6.dp.toPx(),
                                center = Offset(x, yFor(avg))
                            )
                        }
                    }
                }
            }

            // X-axis day labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 34.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { pt ->
                    Text(
                        text = pt.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SevenDayEffortSummaryCard(
    days: List<DayEffortSummary>,
    walkGoalKcal: Int
) {
    val maxWalkKcal = (days.maxOfOrNull { it.walkCalories } ?: walkGoalKcal)
        .coerceAtLeast(walkGoalKcal)
        .coerceAtLeast(100)
    val loggedCount = days.count { it.isLogged }
    val totalWalkMins = days.sumOf { it.walkMinutes }
    val totalWalkKcal = days.sumOf { it.walkCalories }

    PulseCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "7-Day Effort Summary",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "$loggedCount/7 days active · $totalWalkMins min · $totalWalkKcal active kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            if (days.isEmpty()) return@Canvas
            val barSlot = size.width / days.size
            val barW = (barSlot * 0.44f).coerceAtMost(28.dp.toPx())
            val maxBarH = size.height - 16.dp.toPx()

            // Target walk kcal dashed reference line
            val goalRatio = (walkGoalKcal.toFloat() / maxWalkKcal).coerceIn(0f, 1f)
            val goalY = size.height - 6.dp.toPx() - (maxBarH * goalRatio)
            drawLine(
                color = EmeraldAccent.copy(alpha = 0.45f),
                start = Offset(0f, goalY),
                end = Offset(size.width, goalY),
                strokeWidth = 1.2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            days.forEachIndexed { idx, day ->
                val centerX = barSlot * idx + barSlot / 2f
                val left = centerX - barW / 2f
                val bottom = size.height - 6.dp.toPx()

                // Track bar
                drawRoundRect(
                    color = PillSurface,
                    topLeft = Offset(left, bottom - maxBarH),
                    size = Size(barW, maxBarH),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Active walk burn bar
                val burnRatio = (day.walkCalories.toFloat() / maxWalkKcal).coerceIn(0f, 1f)
                val barH = (maxBarH * burnRatio).coerceAtLeast(if (day.walkCalories > 0) 6.dp.toPx() else 0f)
                if (barH > 0f) {
                    drawRoundRect(
                        color = EmeraldAccent,
                        topLeft = Offset(left, bottom - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                // Calorie adherence dot above bar
                val dotColor = when {
                    !day.isLogged -> TextMuted.copy(alpha = 0.35f)
                    day.adherenceRatio >= 0.72f -> AmberAccent
                    else -> TextSecondary
                }
                drawCircle(
                    color = dotColor,
                    radius = 3.5.dp.toPx(),
                    center = Offset(centerX, 4.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = day.shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                    Text(
                        text = "${day.walkCalories}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = if (day.walkCalories > 0) EmeraldAccent else TextMuted,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(EmeraldAccent)
                )
                Text("Walk burn (kcal)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AmberAccent)
                )
                Text("Calorie adherence", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}
