package science.apolline.influxdb

import com.google.gson.JsonParser

/**
 * Created by sparow on 13/10/2017.
 */

object DataParser {

    val DEVICE = "deviceId"
    val SENSOR = "sensorId"
    val DATA = "gasId"
    val UNIT = "unit"
    val VALUE = "value"
    val DATE = "date"
    val TIMESTAMP = "timestamp"

    fun createData(jsonArg: String): String {

        val sb = StringBuilder()

        try {

            val parser = JsonParser()
            val jsonElement = parser.parse(jsonArg)

            if (jsonElement.isJsonObject) {

                val json = jsonElement.asJsonObject

                if (json.has(DATA)) {

                    val tmp = json.get(DATA).asJsonObject
                    val entries = tmp.entrySet()

                    for ((key, value) in entries) {
                        //cpu_load_short,host=server01,region=us-west value=0.64 1434055562000000000
                        sb.append(key).append(",")
                        sb.append(DEVICE).append("=").append(json.get(DEVICE).asString).append(",")
                        sb.append(SENSOR).append("=").append(json.get(SENSOR).asString).append(",")
                        sb.append(DATE).append("=").append(json.get(DATE).asString).append(",")
                        sb.append(UNIT).append("=").append(json.get(UNIT).asString).append(" ")
                        sb.append(VALUE).append("=").append(value.toString()).append(" ")
                        sb.append(System.currentTimeMillis()).append("\n")
                    }
                }


            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

        return sb.toString()
    }


}

