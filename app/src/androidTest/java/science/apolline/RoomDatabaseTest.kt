package science.apolline

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import science.apolline.database.AppDatabase
import science.apolline.database.SensorDao
import science.apolline.models.Position
import science.apolline.models.Sensor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by sparow on 11/17/17.
 */


@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private var sensorDao: SensorDao? = null

    @Before
    fun setup() {
        AppDatabase.TEST_MODE = true
        sensorDao = AppDatabase.getInstance(InstrumentationRegistry.getTargetContext())
    }


    @Test
    fun InsertSensorTest() {

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
        val sensor = Sensor("Arduino", "MQ135", "WedSep2614:23:28EST2017", positionInitObject, dataListObject )

        sensorDao?.insertOne(sensor)
        val sensorTest = getValue(sensorDao?.getSensorById(sensor.sensorId)!!)
        Assert.assertEquals(sensor.sensor, sensorTest.sensor)
    }

    @Test
    fun FlushAllSensorDataTest(){
        sensorDao?.flushSensorData()
        Assert.assertEquals(sensorDao?.getSensorCount(), 0)
    }


    @Throws(InterruptedException::class)
    fun <T> getValue(liveData: LiveData<T>): T {
        val data = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(t: T?) {
                data[0] = t
                latch.countDown()
                liveData.removeObserver(this)
            }

        }
        liveData.observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)

        return data[0] as T
    }
}