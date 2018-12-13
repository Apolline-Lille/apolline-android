package science.apolline.service.database

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.Database
import android.content.Context
import science.apolline.models.Device
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration
import science.apolline.models.TimestampSync


/**
 * Created by sparow on 11/5/17.
 */
@Database(entities = [(Device::class), (TimestampSync::class)], version = 3, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sensorDao(): SensorDao
    abstract fun timestampSyncDao() : TimestampSyncDao

    companion object {
        var TEST_MODE = false
        private const val databaseName = "sensors-database"

        private var db: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (db == null)
                db = if (TEST_MODE) {
                    Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build()
                } else {
                    Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build()
                }
            return db!!
        }

        private fun close() {
            db?.close()
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Since we didn't alter the table, there's nothing else to do here.
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE TimestampSync (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, date INTEGER NOT NULL);")
            }
        }
    }
}