package science.apolline

import com.google.gson.JsonParser
import org.junit.Assert
import org.junit.Test
import junit.framework.Assert.assertTrue
import science.apolline.influxdb.*
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



    /**
     * Test for writing points to remote InfluxDB server (New).
     */
    @Test
    @Throws(IOException::class)
    fun testWriteToInfluxDBNWithGsonModel() {

        //given
        val positionInitObject = Position("GPS",152.36,142.36,"Train")
        val dataInitObjectItem1 = Data("CO2",100.0,"PPM")
        val dataInitObjectItem2 = Data("SMOKE",200.0,"PPM")
        val dataInitObjectItem3 = Data("O3",300.0,"PPM")
        val datalist = listOf(dataInitObjectItem1,dataInitObjectItem2,dataInitObjectItem3)
        val sensorInitObject = Sensor("Arduino","MQ135","WedSep2614:23:28EST2017", positionInitObject, datalist)

        //when
        val dataTosend = RequestParser.createRequestBody(sensorInitObject)
        val api = ApiUtils.apiService
        val call = api.savePost("test","toto","root", dataTosend )
        val response = call.execute()

        //then
        Assert.assertNotNull(api)
        Assert.assertNotNull(call)
        assertTrue(response.isSuccessful)

    }

}









