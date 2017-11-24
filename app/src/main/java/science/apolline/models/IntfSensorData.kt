package science.apolline.models

import android.os.Parcelable
import com.google.gson.JsonObject

/**
 * Created by damien-lenovo on 17/11/2017.
 */
interface IntfSensorData : Parcelable{

    fun toJson(): JsonObject

}