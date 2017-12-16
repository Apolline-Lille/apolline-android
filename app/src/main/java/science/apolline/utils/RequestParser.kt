package science.apolline.utils

import org.json.JSONException
import science.apolline.models.Device

/**
 * Created by sparow on 10/20/17.
 */

enum class Tags constructor(val value: String) {
    DEVICE("device"),
    PROVIDER("provider"),
    LONGITUDE("longitude"),
    LATITUDE("latitude"),
    LOCATION("transport"),
    UNIT("unit"),
    VALUE("value"),
    DATE("date")
}

object RequestParser {

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

                sb.append(Tags.DEVICE).append("=").append(tmpDevice).append(",")
                sb.append(Tags.PROVIDER).append("=").append(tmpProvider).append(",")
                sb.append(Tags.LOCATION).append("=").append(tmpTransport).append(",")
                sb.append(Tags.LONGITUDE).append("=").append(tmpLongitude).append(",")
                sb.append(Tags.LATITUDE).append("=").append(tmpLatitude).append(",")

                sb.append(Tags.DATE).append("=").append(tmpDate).append(",")
                sb.append(Tags.UNIT).append("=").append(tmpUnit).append(" ")
                sb.append(Tags.VALUE).append("=").append(tmpValue).append(" ")

                val tmpTimestamp = System.currentTimeMillis() / 1000

                sb.append(tmpTimestamp.toString()).append("\n")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return sb.toString()
    }
}




