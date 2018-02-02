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
    @Query("SELECT * FROM Device WHERE isSync=0 ORDER BY date ASC LIMIT 8000")
    fun getUnSync(): List<Device>

    @Transaction
    @Query("SELECT count(*) FROM Device WHERE isSync=0")
    fun getSensorNotSyncCount(): Int

    @Transaction
    @Query("SELECT count(*) FROM Device")
    fun getSensorCount(): Int

    @Transaction
    @Query("SELECT * FROM Device WHERE id=:idDevice")
    fun getSensorById(idDevice: Long): LiveData<Device>

    @Transaction
    @Query("SELECT * FROM Device")
    fun dumpSensor(): List<Device>

    @Query("DELETE FROM Device")
    fun flushSensorData()

    @Insert(onConflict = REPLACE)
    fun insert(vararg device: Device)

    @Update(onConflict = REPLACE)
    fun update(vararg device: Device)

    @Delete
    fun delete(vararg device: Device)
}