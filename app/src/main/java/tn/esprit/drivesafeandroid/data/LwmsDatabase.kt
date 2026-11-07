package tn.esprit.drivesafeandroid.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [EventLogEntity::class], version = 1, exportSchema = false)
@TypeConverters(SeverityConverters::class)
abstract class LwmsDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: LwmsDatabase? = null

        fun getDatabase(context: Context): LwmsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LwmsDatabase::class.java,
                    "lwms_database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
