package science.apolline.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity(tableName = "timestampSync")
data class TimestampSync(
        @SerializedName("date")
        @Expose
        var date: Long
){
    @SerializedName("id")
    @Expose
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}