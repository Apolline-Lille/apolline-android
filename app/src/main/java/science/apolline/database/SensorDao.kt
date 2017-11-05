package science.apolline.database

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import science.apolline.models.Sensor
import io.reactivex.Single

/**
 * Created by sparow on 11/5/17.
 */

@Dao
interface SensorDao {
    @get:Query("SELECT * FROM sensor")
    val all: Single<List<Sensor>>

    @Query("SELECT * FROM sensor WHERE sensorId IN (:arg0)")
    fun loadAllByIds(sensorIds: IntArray): List<Sensor>

    @Insert(onConflict = REPLACE)
    fun insertOne(sensor: Sensor)

    @Update(onConflict = REPLACE)
    fun update(sensor: Sensor)

    @Delete
    fun delete(sensor: Sensor)
}