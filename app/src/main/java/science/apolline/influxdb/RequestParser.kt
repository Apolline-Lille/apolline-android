package science.apolline.influxdb

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

    fun createRequestBody(SensorData: Sensor): String {

        val sb = StringBuilder()

        SensorData.data.forEach {
            //co2,device=ard001,sensor=mq135,provider=gps,location=car longitude=45.521 latitude=15.256
            // date=WedSep2614:23:28EST2017 unit=PPM value=1452 1434055562000000000
            sb.append(it.name).append(",")
            sb.append(DEVICE).append("=").append(SensorData.device).append(",")
            sb.append(SENSOR).append("=").append(SensorData.sensor).append(",")
            sb.append(PROVIDER).append("=").append(SensorData.position.provider).append(",")
            sb.append(LOCATION).append("=").append(SensorData.position.location).append(" ")
            sb.append(LONGITUDE).append("=").append(SensorData.position.longitude).append(" ")
            sb.append(LATITUDE).append("=").append(SensorData.position.latitude).append(" ")
            sb.append(DATE).append("=").append(SensorData.date).append(" ")
            sb.append(UNIT).append("=").append(it.unit).append(" ")
            sb.append(VALUE).append("=").append(it.value).append(" ")
            sb.append(System.currentTimeMillis()).append("\n")
        }

        return sb.toString()
    }


}

