package science.apolline

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.testcontainers.containers.wait.strategy.Wait
import science.apolline.models.Device
import science.apolline.models.Position
import science.apolline.service.networks.ApiUtils
import science.apolline.utils.GeoHashHelper
import science.apolline.utils.InfluxDBContainer
import science.apolline.utils.RequestParser
import java.io.IOException


/**
 * Created by sparow on 10/13/17.
 */

class InfluxDBServiceTest {
    companion object {
        val INFLUXDB_DB = "test_db"
        val INFLUXDB_USER = "test_user"
        val INFLUXDB_USER_PASSWORD = "test_password"
    }

    private var testUrl = "http://localhost:8086/"
    private var parser = JsonParser()
    private var JSONTOSEND = "{" +
            "\"CO2\":[100,\"PPM\"]," +
            "\"SMOKE\":[200,\"PPM\"]," +
            "\"CH4\":[300,\"PPM\"]," +
            "\"O3\":[400,\"PPM\"]" +
            "}"

    @Rule
    @JvmField
    val influxDBcontainer: InfluxDBContainer = InfluxDBContainer("influxdb:1.5.4")
            .withEnv("INFLUXDB_DB",  INFLUXDB_DB)
            .withEnv("INFLUXDB_USER", INFLUXDB_USER)
            .withEnv("INFLUXDB_USER_PASSWORD", INFLUXDB_USER_PASSWORD)
            .withExposedPorts(8086)
            .waitingFor(Wait.forHttp("/ping").forStatusCode(204))

    @Before
    fun setUp() {
        val containerIpAddress = influxDBcontainer.containerIpAddress
        val firstMappedPort = influxDBcontainer.firstMappedPort

        testUrl = String.format("http://%s:%s", containerIpAddress, firstMappedPort)
    }


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
        val geohash = GeoHashHelper.encode(80.36, 142.36)
        val positionInitObject = Position("GPS", geohash, "Train")
        val sensorInitObject = Device("ffffffff-c9cf-31db-0000-00006c125b14", "Arduino", 1422568543702900257, positionInitObject, dataListObject, 0)

        //when
        val dataTosend = RequestParser.createSingleRequestBody(sensorInitObject)
        println(dataTosend)
        ApiUtils.setUrl(testUrl)
        val api = ApiUtils.apiService
        val call = api.savePost(INFLUXDB_DB, INFLUXDB_USER, INFLUXDB_USER_PASSWORD, dataTosend)
        val response = call.execute()

        //then
        Assert.assertNotNull(api)
        Assert.assertNotNull(call)
        assertTrue(response.isSuccessful)

    }

}






