package science.apolline;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.influxdb.InfluxDB;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;


/**
 * Created by sparow on 10/1/17.
 */

public class InfluxDBTest {

    public static String JSONTOSEND = "{\"device\":\"Arduino\",\"sensorId\":\"MQ135\",\"gasId\":{\"CO2\":1,\"SMOKE\":2,\"CH4\":3,\"O3\":4},\"unit\":\"PPM\",\"time\":\"WedSep2614:23:28EST2017\"}";
    JsonParser parser = new JsonParser();
    private InfluxDBManager influxDBManager = new InfluxDBManager();

    @Rule
    public final ExpectedException exception = ExpectedException.none();



    /**
     * Test for a ping.
     */
    @Test
    public void setupInfluxDBTest() throws Exception {
        InfluxDB influxDB = this.influxDBManager.setup("http://" + TestUtility.getInfluxDBIP() + ":" + TestUtility.getInfluxDBPORT(true),"toto", "root");
        Assert.assertNotNull(influxDB);
    }

    /**
     * Test for input JSON parsing.
     */
    @Test
    public void testJsonParsing() throws Exception {

        JsonElement jsonElement = this.parser.parse(JSONTOSEND);
        Assert.assertNotNull(jsonElement);
        Assert.assertEquals(this.parser.parse(JSONTOSEND).toString(),JSONTOSEND);

    }

    /**
     * Test for writing points to remote InfluxDB server.
     */
    @Test
    public void testWriteToInfluxDB() throws Exception {
        InfluxDB influxDB = this.influxDBManager.setup("http://" + TestUtility.getInfluxDBIP() + ":" + TestUtility.getInfluxDBPORT(true), "toto", "root");
        Assert.assertNotNull(influxDB);
        Assert.assertTrue(this.influxDBManager.write(influxDB, "test0", JSONTOSEND));
    }


}








