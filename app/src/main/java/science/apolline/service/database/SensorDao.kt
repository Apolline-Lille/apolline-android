package science.apolline.service.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import science.apolline.models.Device
import io.reactivex.Single

/**
 * Created by sparow on 11/5/17.
 */

@Dao
interface SensorDao {
    @get:Query("SELECT * FROM Device")
    val all: Single<List<Device>>

    @Query("SELECT * FROM Device WHERE sensorId IN (:arg0)")
    fun loadAllByIds(sensorIds: IntArray): List<Device>

    @Query("SELECT count(*) FROM Device")
    fun getSensorCount(): Int

    @Query("SELECT * FROM Device WHERE sensorId=:arg0")
    fun getSensorById(sensor_id: Int?): LiveData<Device>

    @Insert(onConflict = REPLACE)
    fun insertOne(device: Device)

    @Update(onConflict = REPLACE)
    fun update(device: Device)

    @Query("DELETE FROM Device")
    fun flushSensorData()

    @Delete
    fun delete(device: Device)
}