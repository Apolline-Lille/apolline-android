package science.apolline

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert
import org.junit.Test
import junit.framework.Assert.assertTrue
import science.apolline.service.networks.*
import science.apolline.models.Position
import science.apolline.models.Device
import science.apolline.utils.RequestParser
import java.io.IOException

/**
 * Created by sparow on 10/13/17.
 */

class InfluxDBServiceTest {

    private val testUrl = "http://localhost:8086/"
    private var parser = JsonParser()
    private var JSONTOSEND = "{" +
            "\"CO2\":[100,\"PPM\"]," +
            "\"SMOKE\":[200,\"PPM\"]," +
            "\"CH4\":[300,\"PPM\"]," +
            "\"O3\":[400,\"PPM\"]" +
            "}"

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
     * Test for writing points to remote InfluxDB server (New).
     */
    @Test
    @Throws(IOException::class)
    fun testWriteToInfluxDBNWithGsonModel() {

        //given
        val dataList = "{" +
                "\"CO2\":[100,\"PPM\"]," +
                "\"SMOKE\":[200,\"PPM\"]," +
                "\"CH4\":[300,\"PPM\"]," +
                "\"O3\":[400,\"PPM\"]" +
                "}"

        val gson = Gson()
        val dataListObject = gson.fromJson(dataList, JsonObject::class.java)
        val positionInitObject = Position("GPS", 152.36, 142.36, "Train")
        val sensorInitObject = Device("Arduino", "WedSep2614:23:28EST2017", positionInitObject, dataListObject)

        //when
        val dataTosend = RequestParser.createRequestBody(sensorInitObject)

        print(dataTosend)
        ApiUtils.setUrl(testUrl)
        val api = ApiUtils.apiService
        val call = api.savePost("test", "toto", "root", dataTosend)
        val response = call.execute()

        //then
        Assert.assertNotNull(api)
        Assert.assertNotNull(call)
        assertTrue(response.isSuccessful)

    }

}









