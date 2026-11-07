package tn.esprit.drivesafeandroid.data

import androidx.room.TypeConverter
import tn.esprit.drivesafeandroid.logic.Severity

class SeverityConverters {
    @TypeConverter
    fun fromSeverity(severity: Severity): String = severity.name

    @TypeConverter
    fun toSeverity(value: String): Severity = Severity.valueOf(value)
}
