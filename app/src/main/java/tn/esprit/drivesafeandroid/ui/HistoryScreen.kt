package tn.esprit.drivesafeandroid.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import tn.esprit.drivesafeandroid.logic.Severity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen() {
    val eventLogs by EventRepository.logs.collectAsState()
    var selectedFilter by remember { mutableStateOf("All events") }

    val surfaceColor = Color(0xFFF1F5F9)
    val cardBg = Color(0xFFFFFFFF)
    val borderColor = Color(0xFFE2E8F0)
    val primaryBlue = Color(0xFF2563EB)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    val filteredLogs = when (selectedFilter) {
        "High only" -> eventLogs.filter { it.severity == Severity.HIGH }
        "Mid only" -> eventLogs.filter { it.severity == Severity.MODERATE }
        "Low only" -> eventLogs.filter { it.severity == Severity.LOW }
        else -> eventLogs
    }

    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.US)
    val groupedLogs = filteredLogs.groupBy { dateFormat.format(Date(it.timestamp)) }

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
                    Text("History Logs", fontSize = 12.sp, color = textSecondary)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stationary Review Mode Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(primaryBlue, CircleShape))
                            Column {
                                Text("STATIONARY REVIEW MODE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text("Vehicle at 0 km/h · In-Motion Lock Compliant", fontSize = 11.sp, color = textSecondary)
                            }
                        }
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Text(
                                "SAFE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Segmented Filter Selector matching Dashboard design
            item {
                HistoryFilterSelector(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            if (filteredLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Text(
                            "No events logged for this filter.",
                            modifier = Modifier.padding(24.dp),
                            color = textSecondary
                        )
                    }
                }
            }

            // Split list by day / date
            groupedLogs.forEach { (dateStr, logsForDate) ->
                item(key = "date_$dateStr") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textSecondary
                    )
                }

                items(logsForDate, key = { it.id }) { log ->
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
                                Text(
                                    text = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(log.timestamp)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        color = when (log.severity) {
                                            Severity.HIGH -> Color(0xFFFEE2E2)
                                            Severity.MODERATE -> Color(0xFFFEF08A)
                                            Severity.LOW -> Color(0xFFDCFCE7)
                                            Severity.NONE -> Color(0xFFDCFCE7)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = when (log.severity) {
                                                Severity.HIGH -> "HIGH"
                                                Severity.MODERATE -> "MODERATE"
                                                Severity.LOW -> "LOW"
                                                Severity.NONE -> "SAFE"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (log.severity) {
                                                Severity.HIGH -> Color(0xFFDC2626)
                                                Severity.MODERATE -> Color(0xFFEA580C)
                                                Severity.LOW -> Color(0xFF16A34A)
                                                Severity.NONE -> Color(0xFF16A34A)
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        color = Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, borderColor)
                                    ) {
                                        Text(
                                            text = "${log.speed.toInt()} km/h",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryBlue,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = log.details, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Rule: ${log.firedRule}", fontSize = 12.sp, color = primaryBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryFilterSelector(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val options = listOf("All events", "High only", "Mid only", "Low only")
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
                val isSelected = selectedFilter == option
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) selectedBg else Color.Transparent)
                        .clickable { onFilterSelected(option) }
                        .padding(vertical = 10.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) textPrimary else textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
