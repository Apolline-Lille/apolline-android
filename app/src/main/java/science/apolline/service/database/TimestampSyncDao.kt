package science.apolline.service.database

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import science.apolline.models.TimestampSync

@Dao
interface TimestampSyncDao{

    @Insert(onConflict = REPLACE)
    fun insert(timestampSync: TimestampSync)

    @Query("SELECT * FROM timestampSync ORDER BY date desc")
    fun getAllSyncDesc() : List<TimestampSync>

    @Query("SELECT date FROM timestampSync ORDER BY id desc LIMIT 1")
    fun getLastSync() : Long
}
