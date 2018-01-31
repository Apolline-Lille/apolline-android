package science.apolline.utils

import org.jetbrains.anko.AnkoLogger
import org.json.JSONException
import science.apolline.models.Device

/**
 * Created by sparow on 10/20/17.
 */

enum class Tags constructor(val value: String) {
    ANDROID("uuid"),
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

    fun createRequestBody(devices: List<Device>): String {
        val requestBody = StringBuilder()
        if (devices.isNotEmpty()) {
            devices.forEach {
                requestBody.append(createSingleRequestBody(it))
            }
        }
        return requestBody.toString()
    }


    fun createSingleRequestBody(device: Device): String {
        val tmpAndroidUuid = device.uuid.replace("\\s".toRegex(), "_").toLowerCase()
        val tmpDevice = device.device.replace("\\s".toRegex(), "_").toLowerCase()

        val tmpProvider = device.position?.provider?.replace("\\s".toRegex(), "_")?.toLowerCase()
        val tmpTransport = device.position?.transport?.replace("\\s".toRegex(), "_")?.toLowerCase()
        val tmpLongitude = device.position?.longitude
        val tmpLatitude = device.position?.latitude

        val tmpDate = device.date
        val sb = StringBuilder()

        try {

            device.data!!.entrySet().iterator().forEach {

                val key = it.key.toString()
                val value = it.value.asJsonArray
                val tmpUnit = value[1].asString.replace("\\s".toRegex(), "")
                val tmpValue = value[0].asDouble.toString()

                sb.append(key).append(",")
                sb.append(Tags.ANDROID.value).append("=").append(tmpAndroidUuid).append(",")
                sb.append(Tags.DEVICE.value).append("=").append(tmpDevice).append(",")
                sb.append(Tags.PROVIDER.value).append("=").append(tmpProvider).append(",")
                sb.append(Tags.LOCATION.value).append("=").append(tmpTransport).append(",")
                sb.append(Tags.UNIT.value).append("=").append(tmpUnit).append(" ")

                sb.append(Tags.LONGITUDE.value).append("=").append(tmpLongitude).append(",")
                sb.append(Tags.LATITUDE.value).append("=").append(tmpLatitude).append(",")
                sb.append(Tags.VALUE.value).append("=").append(tmpValue).append(" ")

                sb.append(tmpDate).append("\n")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return sb.toString()
    }
}




