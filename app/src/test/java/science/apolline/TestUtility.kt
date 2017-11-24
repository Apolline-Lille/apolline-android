package science.apolline

/**
 * Created by sparow on 10/9/17.
 */
object TestUtility {

    val influxDBIP: String
        get() {
            var ip = "localhost"

            val getenv = System.getenv()
            if (getenv.containsKey("INFLUXDB_IP")) {
                ip = getenv["INFLUXDB_IP"].toString()
            }

            return ip
        }


    fun getInfluxDBPORT(apiPort: Boolean): String {
        var port = "8086"

        val getenv = System.getenv()
        if (apiPort) {
            if (getenv.containsKey("INFLUXDB_PORT_API"))
                port = getenv["INFLUXDB_PORT_API"].toString()
        }
        return port
    }
}

