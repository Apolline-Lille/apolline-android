package science.apolline.models

/**
 * Created by sparow on 10/20/17.
 */

import android.arch.persistence.room.*
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity
data class Device(
        @SerializedName("device")
        @Expose
        var device: String,
        @SerializedName("date")
        @Expose
        var date: String,
        @SerializedName("position")
        @Expose
        @Embedded
        var position: Position?,
        @SerializedName("data")
        @Expose
        var data: JsonObject?
) {
    @SerializedName("sensorId")
    @Expose
    @PrimaryKey(autoGenerate = true)
    var sensorId: Int=0
    constructor() : this("", "", null, null)

    override fun toString(): String {
        return """
        |Device = $device
        |Date = $date
        |Position = $position
        |Data = $data
        """.trimMargin()
    }


    fun toHeader(): Array<String> {
        val headerArray = mutableListOf<String>()
        headerArray.add("SensorID")
        headerArray.add("Device")
        headerArray.add( "Date")
        headerArray.add( "Latitude")
        headerArray.add( "Longitude")
        headerArray.add( "Provider")
        headerArray.add( "Transport")
        val temp = data!!.entrySet().iterator()
        while (temp.hasNext()) {
            val it = temp.next()
            headerArray.add(it.key)
        }
        return headerArray.toTypedArray()
    }

    fun toArray(): Array<String> {
        val objectArray = mutableListOf<String>()
        objectArray.add(sensorId.toString())
        objectArray.add(device)
        objectArray.add(date)
        objectArray.add(position?.latitude.toString())
        objectArray.add( position?.longitude.toString())
        objectArray.add(position?.provider.orEmpty())
        objectArray.add(position?.transport.orEmpty())
        val temp = data!!.entrySet().iterator()
        while (temp.hasNext()) {
            val it = temp.next()
            val value = it.value.asJsonArray[0].toString()
            objectArray.add(value)
        }
        return objectArray.toTypedArray()
    }





}