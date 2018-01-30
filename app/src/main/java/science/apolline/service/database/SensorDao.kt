package science.apolline.service.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import io.reactivex.Flowable
import science.apolline.models.Device
import io.reactivex.Single

/**
 * Created by sparow on 11/5/17.
 */

@Dao
interface SensorDao {

    @Transaction
    @Query("SELECT * FROM Device ORDER BY date asc")
    fun all(): Flowable<List<Device>>

    @Transaction
    @Query("SELECT * FROM Device WHERE id IN (:sensorIds)")
    fun loadAllByIds(sensorIds: IntArray): List<Device>

    @Transaction
    @Query("SELECT * FROM Device WHERE isSync=0")
    fun getUnSync(): List<Device>

    @Transaction
    @Query("SELECT count(*) FROM Device")
    fun getSensorCount(): Int

    @Transaction
    @Query("SELECT * FROM Device WHERE id=:idDevice")
    fun getSensorById(idDevice: Long): LiveData<Device>

    @Insert(onConflict = REPLACE)
    fun insertOne(device: Device)

    @Update(onConflict = REPLACE)
    fun update(device: Device)

    @Transaction
    @Query("DELETE FROM Device")
    fun flushSensorData()

    @Transaction
    @Query("SELECT * FROM Device")
    fun dumpSensor(): List<Device>

    @Delete
    fun delete(device: Device)
}