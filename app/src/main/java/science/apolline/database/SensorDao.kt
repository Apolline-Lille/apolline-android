package science.apolline.database

import android.arch.lifecycle.LiveData
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

    @Query("SELECT count(*) FROM sensor")
    fun getSensorCount(): Int

    @Query("SELECT * FROM sensor WHERE sensorId=:arg0")
    fun getSensorById(sensor_id: Int?): LiveData<Sensor>

    @Insert(onConflict = REPLACE)
    fun insertOne(sensor: Sensor)

    @Update(onConflict = REPLACE)
    fun update(sensor: Sensor)

    @Query("DELETE FROM sensor")
    fun flushSensorData()

    @Delete
    fun delete(sensor: Sensor)
}