package science.apolline.sensor.common

import android.os.Parcelable
import com.google.gson.JsonObject

/**
 * Created by damien-lenovo on 17/11/2017.
 */
interface sensorData : Parcelable{

    fun toJson(): JsonObject

}