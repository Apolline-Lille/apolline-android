package science.apolline.utils

import org.json.JSONException
import science.apolline.models.Device

/**
 * Created by sparow on 10/20/17.
 */

object RequestParser {

    val DEVICE = "device"
    val PROVIDER = "provider"
    val LONGITUDE = "longitude"
    val LATITUDE = "latitude"
    val LOCATION = "transport"
    val UNIT = "unit"
    val VALUE = "value"
    val DATE = "date"

    fun createRequestBody(device: Device): String {

        val tmpDevice = device.device.replace("\\s".toRegex(), "")
        val tmpProvider = device.position!!.provider.replace("\\s".toRegex(), "")
        val tmpTransport = device.position!!.transport.replace("\\s".toRegex(), "")
        val tmpLongitude = device.position!!.longitude.toString()
        val tmpLatitude = device.position!!.latitude.toString()
        val tmpDate = device.date.replace("\\s".toRegex(), "")

        val sb = StringBuilder()

        try {

            device.data!!.entrySet().iterator().forEach {

                val key = it.key.toString()
                val value = it.value.asJsonArray
                val tmpUnit = value[1].asString.replace("\\s".toRegex(), "")
                val tmpValue = value[0].asDouble.toString()

                sb.append(key).append(",")

                sb.append(DEVICE).append("=").append(tmpDevice).append(",")
                sb.append(PROVIDER).append("=").append(tmpProvider).append(",")
                sb.append(LOCATION).append("=").append(tmpTransport).append(",")
                sb.append(LONGITUDE).append("=").append(tmpLongitude).append(",")
                sb.append(LATITUDE).append("=").append(tmpLatitude).append(",")

                sb.append(DATE).append("=").append(tmpDate).append(",")
                sb.append(UNIT).append("=").append(tmpUnit).append(" ")
                sb.append(VALUE).append("=").append(tmpValue).append(" ")

                sb.append(System.currentTimeMillis()).append("\n")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return sb.toString()
    }
}




