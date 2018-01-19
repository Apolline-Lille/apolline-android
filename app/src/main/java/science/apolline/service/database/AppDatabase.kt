package science.apolline.service.database

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.Database
import android.content.Context
import science.apolline.models.Device


/**
 * Created by sparow on 11/5/17.
 */

@Database(entities = arrayOf(Device::class), version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sensorDao(): SensorDao

    companion object {

        var TEST_MODE = false
        private val databaseName = "sensors-database"

        private var db: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (db == null) {
                if (TEST_MODE) {
                    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build()
                } else {
                    db = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                            .build()
                }
            }
            return db!!
        }

        private fun close() {
            db?.close()
        }

    }
}