package science.apolline.service.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import io.reactivex.Flowable
import science.apolline.models.Device

/**
 * Created by sparow on 11/5/17.
 */
@Dao
interface SensorDao {

    @Transaction
    @Query("SELECT * FROM Device ORDER BY date desc LIMIT :nbDevice")
    fun getLastEntries(nbDevice: Long): Flowable<List<Device>>



    @Transaction
    @Query("SELECT * FROM Device WHERE date <= :actualSyncDate AND date > :lastSyncDate ORDER BY date ASC LIMIT :nbDevice")
    fun getUnSyncByDate(actualSyncDate: Long, lastSyncDate: Long,nbDevice: Long): List<Device>



    @Query("SELECT count(*) FROM Device WHERE date > :lastSyncDate")
    fun getSensorNotSyncCountByDate(lastSyncDate: Long): Long

    @Query("SELECT count(*) FROM Device WHERE date <= :actualSyncDate AND date > :lastSyncDate")
    fun getSensorNotSyncCountByDate(actualSyncDate: Long, lastSyncDate: Long): Long



    @Query("SELECT count(*) FROM Device WHERE date <= :lastSyncDate")
    fun getSensorSyncCountByDate(lastSyncDate: Long): Long


    @Query("SELECT count(*) FROM Device")
    fun getSensorCount(): Long

    @Query("SELECT count(*) FROM Device")
    fun getCount(): Flowable<Long>

    @Transaction
    @Query("SELECT * FROM Device WHERE id=:idDevice")
    fun getSensorById(idDevice: Long): LiveData<Device>



    @Query("DELETE FROM Device WHERE date <= :lastSyncDate")
    fun deleteDataSyncByDate(lastSyncDate: Long)

    @Transaction
    @Query("SELECT * FROM Device")
    fun dumpSensor(): List<Device>

    @Query("DELETE FROM Device")
    fun flushSensorData()

    @Insert(onConflict = REPLACE)
    fun insert(vararg device: Device)

    @Update(onConflict = REPLACE)
    fun update(vararg device: Device)
}