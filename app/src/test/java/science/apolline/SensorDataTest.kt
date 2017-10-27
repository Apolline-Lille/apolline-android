package science.apolline

import org.junit.Test
import java.io.IOException
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.*
import science.apolline.influxdb.Position
import science.apolline.influxdb.Sensor



/**
 * Created by sparow on 10/21/17.
 */

class SensorDataTest {

    /**
     * Test Position class
     */
    @Test
    @Throws(IOException::class)
    fun testHardcodedPosition() {

        //given
        val jsonInit = "{" +
                "\"provider\":\"GPS\"," +
                "\"longitude\":152.36," +
                "\"latitude\":142.36," +
                "\"location\":\"Train\"" +
                "}"
        val positionInitObject = Position("GPS",152.36,142.36,"Train")

        //when
        val gson = Gson()
        val position = gson.fromJson(jsonInit, Position::class.java)
        val jsonPositionFromObject = gson.toJson(positionInitObject)

        //then
        assertNotNull(position)
        assertEquals(position.provider,"GPS")
        assertNotEquals(position.provider,"toto")
        assertNotEquals(position.longitude,100.0)
        assertNotEquals(position.latitude,100.0)
        assertEquals(position.location,"Train")
        assertNotEquals(position.location,"toto")
        assertEquals(position.toString(),positionInitObject.toString())
        assertEquals(jsonPositionFromObject,jsonInit)

    }


    /**
     * Test Sensor class
     */
    @Test
    @Throws(IOException::class)
    fun testHardcodedSensor() {

        //given
        val jsonInit = "{" +
                "\"device\":\"Arduino\"," +
                "\"sensor\":\"MQ135\"," +
                "\"date\": \"WedSep2614:23:28EST2017\"," +
                "\"position\": {" +
                "\"provider\":\"GPS\"," +
                "\"longitude\":152.36," +
                "\"latitude\":142.36," +
                "\"location\":\"Train\"" +
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

        val positionInitObject = Position("GPS",152.36,142.36,"Train")

        //when
        val gson = Gson()
        val dataListObject = gson.fromJson(dataList, JsonObject::class.java)
        val sensor = gson.fromJson(jsonInit, Sensor::class.java)
        val sensorInitObject = Sensor("Arduino","MQ135","WedSep2614:23:28EST2017", positionInitObject, dataListObject)
        val jsonSensorFromObject = gson.toJson(sensorInitObject)

        //then
        assertNotNull(sensor)
        assertEquals(sensor.device,"Arduino")
        assertNotEquals(sensor.device,"toto")
        assertEquals(sensor.sensor,"MQ135")
        assertNotEquals(sensor.sensor,"toto")
        assertEquals(sensor.date,"WedSep2614:23:28EST2017")
        assertNotEquals(sensor.date,"toto")
        assertNotNull(sensor.position)
        assertNotNull(sensor.data)
        // assertEquals(sensor.data.size,3)
        assertEquals(sensor.toString(),sensorInitObject.toString())
        val removedSpace = jsonSensorFromObject.toString().replace("\\s+".toRegex(), " ")
        assertEquals(removedSpace,removedSpace)

    }

}










