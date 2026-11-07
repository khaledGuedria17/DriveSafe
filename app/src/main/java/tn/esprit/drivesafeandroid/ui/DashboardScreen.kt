package tn.esprit.drivesafeandroid.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tn.esprit.drivesafeandroid.data.EventRepository

@Composable
fun DashboardScreen() {
    var selectedRange by remember { mutableStateOf("Today") }

    val surfaceColor = Color(0xFFF1F5F9)
    val cardBg = Color(0xFFFFFFFF)
    val borderColor = Color(0xFFE2E8F0)
    val primaryBlue = Color(0xFF2563EB)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    val stats = EventRepository.getTimeFilteredStats(selectedRange)
    val totalBreakdownCount = stats.interactionBreakdown.sumOf { it.second }.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFCBDBF5), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = tn.esprit.drivesafeandroid.R.drawable.ic_shield_logo),
                        contentDescription = "Shield Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text("LWMS", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Safety Dashboard", fontSize = 12.sp, color = textSecondary)
                }
            }

            Surface(
                color = Color(0xFFDBE1FF),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFFB4C5FF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(primaryBlue, CircleShape))
                    Text("SIMULATOR ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryBlue)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text("Safety Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                    Text("Last updated: Just now · Active evaluation", fontSize = 12.sp, color = textSecondary)
                }
            }

            // Segmented Time Range Selector matching UIKit photo: Today, Last 7 Days, Overall
            item {
                SegmentedTimeRangeSelector(
                    selectedRange = selectedRange,
                    onRangeSelected = { selectedRange = it }
                )
            }

            // Session Stats & Total Alerts Cards (Filtered by selected range)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Sessions",
                        value = "${stats.totalSessions}",
                        subtitle = stats.subtitleText
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Alerts",
                        value = "${stats.totalAlerts}",
                        subtitle = "Avg ${String.format(java.util.Locale.US, "%.1f", stats.averageAlertsPerSession)} alerts/session"
                    )
                }
            }

            // Severity Distribution Card matching UIKit photo (Progress bar + % + Cards)
            item {
                SeverityDistributionCard(
                    highCount = stats.highCount,
                    moderateCount = stats.moderateCount,
                    lowCount = stats.lowCount,
                    cardBg = cardBg,
                    borderColor = borderColor,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            // Dynamic Interaction Categorization Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Interaction Categorization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (stats.interactionBreakdown.all { it.second == 0 }) {
                            Text("No alerts recorded yet.", fontSize = 13.sp, color = textSecondary)
                        } else {
                            stats.interactionBreakdown.forEach { (category, count) ->
                                val percentage = (count.toFloat() / totalBreakdownCount.toFloat()) * 100f
                                CategoryRow(category, count, percentage, primaryBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeverityDistributionCard(
    highCount: Int,
    moderateCount: Int,
    lowCount: Int,
    cardBg: Color,
    borderColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val total = (highCount + moderateCount + lowCount).coerceAtLeast(1)
    val highPct = (highCount.toFloat() / total.toFloat()) * 100f
    val modPct = (moderateCount.toFloat() / total.toFloat()) * 100f
    val lowPct = (lowCount.toFloat() / total.toFloat()) * 100f

    val redColor = Color(0xFFEF4444)
    val blueColor = Color(0xFF2563EB)
    val cyanColor = Color(0xFF38BDF8)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Severity Distribution", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                Text("Total ${highCount + moderateCount + lowCount} Alerts", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textSecondary)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Segmented Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (highCount > 0) {
                        Box(modifier = Modifier.weight(highCount.toFloat()).fillMaxHeight().background(redColor))
                    }
                    if (moderateCount > 0) {
                        Box(modifier = Modifier.weight(moderateCount.toFloat()).fillMaxHeight().background(blueColor))
                    }
                    if (lowCount > 0) {
                        Box(modifier = Modifier.weight(lowCount.toFloat()).fillMaxHeight().background(cyanColor))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Cards Below
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeverityDetailCard(
                    modifier = Modifier.weight(1f),
                    label = "HIGH",
                    count = highCount,
                    percentage = highPct.toInt(),
                    dotColor = redColor,
                    textColor = redColor,
                    cardBg = cardBg,
                    borderColor = borderColor
                )
                SeverityDetailCard(
                    modifier = Modifier.weight(1f),
                    label = "MODERATE",
                    count = moderateCount,
                    percentage = modPct.toInt(),
                    dotColor = blueColor,
                    textColor = blueColor,
                    cardBg = cardBg,
                    borderColor = borderColor
                )
                SeverityDetailCard(
                    modifier = Modifier.weight(1f),
                    label = "LOW",
                    count = lowCount,
                    percentage = lowPct.toInt(),
                    dotColor = cyanColor,
                    textColor = Color(0xFF0369A1),
                    cardBg = cardBg,
                    borderColor = borderColor
                )
            }
        }
    }
}

@Composable
fun SeverityDetailCard(
    modifier: Modifier = Modifier,
    label: String,
    count: Int,
    percentage: Int,
    dotColor: Color,
    textColor: Color,
    cardBg: Color,
    borderColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(modifier = Modifier.size(6.dp).background(dotColor, CircleShape))
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("$count", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(2.dp))
            Text("$percentage%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun SegmentedTimeRangeSelector(
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {
    val options = listOf("Today", "Last 7 Days", "Overall")
    val containerBg = Color(0xFFE5EEFF)
    val selectedBg = Color(0xFFFFFFFF)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF434655)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerBg, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            options.forEach { option ->
                val isSelected = selectedRange == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) selectedBg else Color.Transparent)
                        .clickable { onRangeSelected(option) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) textPrimary else textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color = Color(0xFF0F172A)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun CategoryRow(title: String, count: Int, percentage: Float, barColor: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
            Text("$count  (${percentage.toInt()}%)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp)),
            color = barColor,
        )
    }
}
