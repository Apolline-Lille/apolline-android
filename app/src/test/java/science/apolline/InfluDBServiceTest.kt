package science.apolline

import com.google.gson.JsonParser
import org.junit.Assert
import org.junit.Test
import junit.framework.Assert.assertTrue
import science.apolline.influxdb.ApiUtils
import science.apolline.influxdb.DataParser
import java.io.IOException


/**
 * Created by sparow on 10/13/17.
 */

class InfluxDBServiceTest {

    private var parser = JsonParser()
    private var JSONTOSEND = "{\"deviceId\":\"Arduino\",\"sensorId\":\"MQ135\",\"gasId\":{\"CO2\":1,\"SMOKE\":2,\"CH4\":3,\"O3\":4},\"unit\":\"PPM\",\"date\":\"WedSep2614:23:28EST2017\"}"

    /**
     * Test for input JSON parsing.
     */
    @Test
    @Throws(Exception::class)
    fun testJsonParsing() {

        val jsonElement = this.parser.parse(JSONTOSEND)
        Assert.assertNotNull(jsonElement)
        Assert.assertEquals(this.parser.parse(JSONTOSEND).toString(), JSONTOSEND)

    }

    /**
     * Test for writing points to remote InfluxDB server.
     */
    @Test
    @Throws(IOException::class)
    fun testWriteToInfluxDB() {

        val dataTosend = DataParser.createData(JSONTOSEND)
        val api = ApiUtils.apiService
        val call = api.savePost("test","toto","root", dataTosend )
        val response = call.execute()

        Assert.assertNotNull(api)
        Assert.assertNotNull(call)
        assertTrue(response.isSuccessful)

    }



}









