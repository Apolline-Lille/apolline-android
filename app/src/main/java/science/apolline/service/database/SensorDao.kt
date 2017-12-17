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

    @Query("SELECT * FROM Device WHERE id IN (:sensorIds)")
    fun loadAllByIds(sensorIds: IntArray): List<Device>

    @Query("SELECT count(*) FROM Device")
    fun getSensorCount(): Int

    @Query("SELECT * FROM Device WHERE id=:idDevice")
    fun getSensorById(idDevice: Long): LiveData<Device>

    @Insert(onConflict = REPLACE)
    fun insertOne(device: Device)

    @Update(onConflict = REPLACE)
    fun update(device: Device)

    @Query("DELETE FROM Device")
    fun flushSensorData()

    @Query("SELECT * FROM Device")
    fun dumpSensor(): List<Device>

    @Delete
    fun delete(device: Device)
}