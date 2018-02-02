package science.apolline

import org.junit.Test
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.*
import science.apolline.models.Position
import science.apolline.models.Device
import science.apolline.utils.GeoHashHelper


/**
 * Created by sparow on 10/21/17.
 */

class DeviceModelDataTest {

    /**
     * Test Position class
     */
    @Test
    @Throws(IOException::class)
    fun testHardcodedPosition() {

        val geohash = GeoHashHelper.encode(80.36,142.36)

        //given
        val jsonInit = "{" +
                "\"provider\":\"GPS\"," +
                "\"geohash\":\"$geohash\"," +
                "\"transport\":\"Train\"" +
                "}"
        val positionInitObject = Position("GPS", geohash, "Train")

        //when
        val gson = Gson()
        val position = gson.fromJson(jsonInit, Position::class.java)
        val jsonPositionFromObject = gson.toJson(positionInitObject)

        //then
        assertNotNull(position)
        assertNotEquals(position.provider, 0)
        assertEquals(position.provider, "GPS")
        assertNotEquals(position.provider, "toto")
        assertNotEquals(GeoHashHelper.getLongitude(geohash), 100.0)
        assertNotEquals(GeoHashHelper.getLatitude(geohash), 100.0)
        assertEquals(position.transport, "Train")
        assertNotEquals(position.transport, "toto")
        assertEquals(position.toString(), positionInitObject.toString())
        assertEquals(jsonPositionFromObject, jsonInit)

    }


    /**
     * Test Device class
     */
    @Test
    @Throws(IOException::class)
    fun testHardcodedSensor() {
        //given
        val geohash = GeoHashHelper.encode(80.36,142.36)

        val jsonInit = "{" +
                "\"id\":0," +
                "\"uuid\":ffffffff-c9cf-31db-0000-00006c125b14," +
                "\"device\":\"Arduino\"," +
                "\"sensor\":\"MQ135\"," +
                "\"date\": \"1422568543702900257\"," +
                "\"position\": {" +
                "\"provider\":\"GPS\"," +
                "\"geohash\":$geohash," +
                "\"transport\":\"Train\"" +
                "}," +
                "\"data\": {" +
                "\"CO2\": [100,\"PPM\"]," +
                "\"SMOKE\":[200,\"PPM\"]," +
                "\"CH4\":[300,\"PPM\"]," +
                "\"O3\":[400,\"PPM\"]" +
                "}" +
                "}"

        val dataList = "{" +
                "\"CO2\":[100,\"PPM\"]," +
                "\"SMOKE\":[200,\"PPM\"]," +
                "\"CH4\":[300,\"PPM\"]," +
                "\"O3\":[400,\"PPM\"]" +
                "}"

        val positionInitObject = Position("GPS", geohash, "Train")

        //when
        val gson = Gson()
        val dataListObject = gson.fromJson(dataList, JsonObject::class.java)
        val deviceModel = gson.fromJson(jsonInit, Device::class.java)
        val sensorInitObject = Device("ffffffff-c9cf-31db-0000-00006c125b14","Arduino",  1422568543702900257, positionInitObject, dataListObject,0)
        val jsonSensorFromObject = gson.toJson(sensorInitObject)

        //then
        assertNotNull(deviceModel)
        assertEquals(deviceModel.id, 0)
        assertNotEquals(deviceModel.id, 1)
        assertEquals(deviceModel.device, "Arduino")
        assertNotEquals(deviceModel.device, "toto")
        assertEquals(deviceModel.date, 1422568543702900257)
        assertNotEquals(deviceModel.date, "toto")
        assertNotNull(deviceModel.position)
        assertNotNull(deviceModel.data)

        assertEquals(deviceModel.toString(), sensorInitObject.toString())
        val removedSpace = jsonSensorFromObject.toString().replace("\\s+".toRegex(), " ")
        assertEquals(removedSpace, removedSpace)

    }

}










