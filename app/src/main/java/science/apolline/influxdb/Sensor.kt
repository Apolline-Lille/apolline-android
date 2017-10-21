package science.apolline.influxdb

/**
 * Created by sparow on 10/20/17.
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Sensor (
        @SerializedName("device")
        @Expose
        val device: String,
        @SerializedName("sensor")
        @Expose
        val sensor: String,
        @SerializedName("date")
        @Expose
        val date: String,
        @SerializedName("position")
        @Expose
        val position: Position,
        @SerializedName("data")
        @Expose
        val data: List<Data>
){

    override fun toString(): String {
        return """
        |Device = $device
        |Sensor = $sensor
        |Date = $date
        |Position = $position
        |Data = $data
        """.trimMargin()
    }

}