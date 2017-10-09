package science.apolline;

/**
 * Created by sparow on 10/9/17.
 */

import java.util.Map;

public class TestUtility {


    public static String getInfluxDBIP() {
        String ip = "127.0.0.1";

        Map<String, String> getenv = System.getenv();
        if (getenv.containsKey("INFLUXDB_IP")) {
            ip = getenv.get("INFLUXDB_IP");
        }

        return ip;
    }



    public static String getInfluxDBPORT(boolean apiPort) {
        String port = "8086";

        Map<String, String> getenv = System.getenv();
        if(apiPort) {
            if (getenv.containsKey("INFLUXDB_PORT_API"))
                port = getenv.get("INFLUXDB_PORT_API");
        }
        return port;
    }
}
