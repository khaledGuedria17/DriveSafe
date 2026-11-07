package tn.esprit.drivesafeandroid.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import tn.esprit.drivesafeandroid.logic.Severity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class EventLogItem(
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val severity: Severity,
    val firedRule: String,
    val speed: Double,
    val details: String
)

data class TimeFilteredStats(
    val totalSessions: Int,
    val subtitleText: String,
    val totalAlerts: Int,
    val averageAlertsPerSession: Double,
    val highCount: Int,
    val moderateCount: Int,
    val lowCount: Int,
    val interactionBreakdown: List<Pair<String, Int>>
)

object EventRepository {
    private var dao: EventDao? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _logs = MutableStateFlow<List<EventLogItem>>(emptyList())
    val logs: StateFlow<List<EventLogItem>> = _logs

    fun init(context: Context) {
        if (dao == null) {
            val db = LwmsDatabase.getDatabase(context)
            dao = db.eventDao()
            scope.launch {
                dao?.getAllLogs()?.map { entities -> entities.map { it.toItem() } }?.collect { items ->
                    _logs.value = items
                }
            }
        }
    }

    fun logEvent(eventType: String, severity: Severity, firedRule: String, speed: Double, details: String) {
        val entity = EventLogEntity(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            severity = severity,
            firedRule = firedRule,
            speed = speed,
            details = details
        )
        scope.launch {
            dao?.insert(entity)
        }
    }

    fun getTimeFilteredStats(range: String): TimeFilteredStats {
        val allLogs = _logs.value
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(calendar.time)

        val filteredLogs = when (range) {
            "Today" -> allLogs.filter { dateFormat.format(Date(it.timestamp)) == todayStr }
            "Last 7 Days" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val threshold = calendar.timeInMillis
                allLogs.filter { it.timestamp >= threshold }
            }
            else -> allLogs
        }

        val alerts = filteredLogs.filter { it.severity != Severity.NONE }
        val totalAlerts = alerts.size

        val sessionsCount = when (range) {
            "Today" -> 1
            "Last 7 Days" -> 7
            else -> {
                val distinctDays = allLogs.map { dateFormat.format(Date(it.timestamp)) }.distinct().size
                distinctDays.coerceAtLeast(1)
            }
        }

        val subtitle = when (range) {
            "Today" -> "Active today"
            "Last 7 Days" -> "Past 7 days"
            else -> "Overall sessions"
        }

        val avg = if (sessionsCount > 0) totalAlerts.toDouble() / sessionsCount.toDouble() else 0.0

        val highCount = alerts.count { it.severity == Severity.HIGH }
        val moderateCount = alerts.count { it.severity == Severity.MODERATE }
        val lowCount = alerts.count { it.severity == Severity.LOW }

        val grouping = alerts.groupBy { it.firedRule }
        val breakdown = if (alerts.isEmpty()) {
            listOf(
                "Texting (R3)" to 0,
                "Phone Call (R4)" to 0,
                "Scrolling (R5)" to 0,
                "Tapping Screen (R5)" to 0
            )
        } else {
            grouping.map { (rule, list) ->
                val label = when {
                    rule.contains("TEXTING") -> "Texting (R3)"
                    rule.contains("CALL") -> "Phone Call (R4)"
                    rule.contains("SCROLL") -> "Scrolling (R5)"
                    rule.contains("TAPPING") -> "Tapping Screen (R5)"
                    else -> rule
                }
                label to list.size
            }.sortedByDescending { it.second }
        }

        return TimeFilteredStats(
            totalSessions = sessionsCount,
            subtitleText = subtitle,
            totalAlerts = totalAlerts,
            averageAlertsPerSession = avg,
            highCount = highCount,
            moderateCount = moderateCount,
            lowCount = lowCount,
            interactionBreakdown = breakdown
        )
    }

    fun clear() {
        scope.launch {
            dao?.clear()
        }
    }
}
