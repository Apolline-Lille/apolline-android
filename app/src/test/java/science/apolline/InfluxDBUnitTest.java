package science.apolline;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.influxdb.InfluxDB;

import org.junit.Test;


/**
 * Created by sparow on 10/1/17.
 */

public class InfluxDBUnitTest {

    String json = "{" +
            "  \"device\": \"Arduino\"," +
            "  \"sensorId\": \"MQ135\"," +
            "  \"gasId\":" +
            "    {" +
            "      \"CO2\":1," +
            "      \"SMOKE\":2," +
            "      \"CH4\":3," +
            "      \"O3\":4" +
            "    }," +
            "  \"unit\": \"PPM\" ," +
            "  \"time\": \"Wed Sep 26 14:23:28 EST 2017\"" +
            "  " +
            "}";


    @Test
    public void setupInfluxDBTest() throws Exception {

        InfluxDBManager testManager = new InfluxDBManager();
        testManager.configInfluxDB("test0","http://localhost:8086","foo","root");

    }


    @Test
    public void jsonParseTest() throws Exception {

        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(json);

        if (jsonElement.isJsonObject()) {

            System.out.println(jsonElement.toString());

        }

    }

    @Test
    public void writePointTest() throws Exception {

        InfluxDBManager testManager = new InfluxDBManager();
        InfluxDB influxDB = testManager.configInfluxDB("test1", "http://localhost:8086", "foo", "root");
        testManager.write(influxDB, "test1", json);

    }
}
