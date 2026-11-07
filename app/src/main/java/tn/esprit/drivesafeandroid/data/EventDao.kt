package tn.esprit.drivesafeandroid.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM event_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<EventLogEntity>>

    @Insert
    fun insert(entity: EventLogEntity)

    @Query("DELETE FROM event_logs")
    fun clear()
}
