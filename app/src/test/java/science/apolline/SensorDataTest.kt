package science.apolline

import org.junit.Test
import science.apolline.influxdb.Data
import java.io.IOException
import com.google.gson.Gson
import org.junit.Assert.*
import science.apolline.influxdb.Position
import science.apolline.influxdb.Sensor

/**
 * Created by sparow on 10/21/17.
 */

class SensorDataTest {


    /**
     * Test Data class
     */
    @Test
    @Throws(IOException::class)
    fun testHardcodedData() {

        //given
        val jsonInit = "{" +
                "\"name\":\"CO2\"," +
                "\"value\":100.0," +
                "\"unit\":\"PPM\"" +
                "}"
        val dataInitObject = Data("CO2",100.0,"PPM")

        //when
        val gson = Gson()
        val data = gson.fromJson(jsonInit, Data::class.java)
        val jsonDataFromObject = gson.toJson(dataInitObject)

        //then
        assertNotNull(data)
        assertEquals(data.name,"CO2")
        assertNotEquals(data.name,"toto")
        assertNotEquals(data.value,200.0)
        assertEquals(data.unit,"PPM")
        assertNotEquals(data.unit,"toto")
        assertEquals(data.toString(),dataInitObject.toString())
        assertEquals(jsonDataFromObject,jsonInit)
    }

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
                "\"date\":\"WedSep2614:23:28EST2017\"," +
                "\"position\":{" +
                    "\"provider\":\"GPS\"," +
                    "\"longitude\":152.36," +
                    "\"latitude\":142.36," +
                    "\"location\":\"Train\"" +
                    "},"+
                "\"data\":[" +
                    "{" +
                    "\"name\":\"CO2\"," +
                    "\"value\":100.0," +
                    "\"unit\":\"PPM\"" +
                    "}," +
                    "{" +
                    "\"name\":\"SMOKE\"," +
                    "\"value\":200.0," +
                    "\"unit\":\"PPM\"" +
                    "},"+
                    "{" +
                    "\"name\":\"O3\"," +
                    "\"value\":300.0," +
                    "\"unit\":\"PPM\" " +
                    "}"+
                    "]"+
            "}"

        val positionInitObject = Position("GPS",152.36,142.36,"Train")
        val dataInitObjectItem1 = Data("CO2",100.0,"PPM")
        val dataInitObjectItem2 = Data("SMOKE",200.0,"PPM")
        val dataInitObjectItem3 = Data("O3",300.0,"PPM")
        val datalist = listOf(dataInitObjectItem1,dataInitObjectItem2,dataInitObjectItem3)
        val sensorInitObject = Sensor("Arduino","MQ135","WedSep2614:23:28EST2017", positionInitObject, datalist)

        //when
        val gson = Gson()
        val sensor = gson.fromJson(jsonInit, Sensor::class.java)
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
        assertEquals(sensor.data.size,3)
        assertEquals(sensor.toString(),sensorInitObject.toString())
        val removedSpace = jsonSensorFromObject.toString().replace("\\s+".toRegex(), " ")
        assertEquals(removedSpace,removedSpace)

    }

}










