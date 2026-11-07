package tn.esprit.drivesafeandroid.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import tn.esprit.drivesafeandroid.logic.Severity

@Entity(tableName = "event_logs")
data class EventLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long,
    val eventType: String,
    val severity: Severity,
    val firedRule: String,
    val speed: Double,
    val details: String
) {
    fun toItem() = EventLogItem(
        id = id,
        timestamp = timestamp,
        eventType = eventType,
        severity = severity,
        firedRule = firedRule,
        speed = speed,
        details = details
    )
}
