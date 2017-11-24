package science.apolline.utils

import org.json.JSONException
import science.apolline.models.Device

/**
 * Created by sparow on 10/20/17.
 */

object RequestParser {

    val DEVICE = "device"
    val SENSOR = "sensor"
    val PROVIDER = "provider"
    val LONGITUDE = "longitude"
    val LATITUDE = "latitude"
    val LOCATION = "location"
    val UNIT = "unit"
    val VALUE = "value"
    val DATE = "date"

    fun createRequestBody(device: Device): String {

        val sb = StringBuilder()

        try {

            val json = device.data
            val temp = json!!.entrySet().iterator()

            while (temp.hasNext()) {

                val it = temp.next()
                val key = it.key
                val value = it.value.asJsonArray

                sb.append(key).append(",")

                sb.append(DEVICE).append("=").append(device.device).append(",")
                sb.append(PROVIDER).append("=").append(device.position!!.provider).append(",")
                sb.append(LOCATION).append("=").append(device.position!!.location).append(",")
                sb.append(LONGITUDE).append("=").append(device.position!!.longitude).append(",")
                sb.append(LATITUDE).append("=").append(device.position!!.latitude).append(",")
                sb.append(DATE).append("=").append(device.date).append(",")

                sb.append(UNIT).append("=").append(value[0]).append(" ")
                sb.append(VALUE).append("=").append(value[1]).append(" ")

                sb.append(System.currentTimeMillis()).append("\n")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return sb.toString()
    }
}




