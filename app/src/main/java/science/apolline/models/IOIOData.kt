package science.apolline.models

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class IOIOData : IntfSensorData {
    ////////////////////////////////////////////////////////////////////////////////////////////////////


    var count: Int = 0
    val buff = IntArray(64)

    var pm01Value = 0          //define PM1.0 value of the air detector module
    var pm2_5Value = 0         //define PM2.5 value of the air detector module
    var pm10Value = 0         //define PM10 value of the air detector module

    var pm0_3Above = 0
    var pm0_5Above = 0
    var pm1Above = 0
    var pm2_5Above = 0
    var pm5Above = 0
    var pm10Above = 0

    var tempKelvin = 0f
    var rh = 0f
    var rht = 0.0

    val LENG: Byte = 31

    val tempCelcius: Float
        get() = tempKelvin - 273.15f

    constructor() {}

    protected constructor(`in`: Parcel) {
        pm01Value = `in`.readInt()
        pm2_5Value = `in`.readInt()
        pm10Value = `in`.readInt()
        pm0_3Above = `in`.readInt()
        pm0_5Above = `in`.readInt()
        pm1Above = `in`.readInt()
        pm2_5Above = `in`.readInt()
        pm5Above = `in`.readInt()
        pm10Above = `in`.readInt()
        tempKelvin = `in`.readFloat()
        rh = `in`.readFloat()
        rht = `in`.readDouble()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(pm01Value)
        parcel.writeInt(pm2_5Value)
        parcel.writeInt(pm10Value)

        parcel.writeInt(pm0_3Above)
        parcel.writeInt(pm0_5Above)
        parcel.writeInt(pm1Above)
        parcel.writeInt(pm2_5Above)
        parcel.writeInt(pm5Above)
        parcel.writeInt(pm10Above)

        parcel.writeFloat(tempKelvin)
        parcel.writeFloat(rh)
        parcel.writeDouble(rht)
    }


    fun checkValue(): Boolean {
        var receiveflag = false
        var receiveSum = 0

        for (i in 0 until LENG - 2) {
            receiveSum = receiveSum + buff[i]
        }
        receiveSum = receiveSum + 0x42

        if (receiveSum == (buff[LENG - 2] shl 8) + buff[LENG - 1])
        //check the serial data
        {
            receiveflag = true
        }
        return receiveflag
    }

    fun parse() {
        pm01Value = (buff[3] shl 8) + buff[4]
        pm2_5Value = (buff[5] shl 8) + buff[6]
        pm10Value = (buff[7] shl 8) + buff[8]
        pm0_3Above = (buff[15] shl 8) + buff[16]
        pm0_5Above = (buff[17] shl 8) + buff[18]
        pm1Above = (buff[19] shl 8) + buff[20]
        pm2_5Above = (buff[21] shl 8) + buff[22]
        pm5Above = (buff[23] shl 8) + buff[24]
        pm10Above = (buff[25] shl 8) + buff[26]
    }


    enum class Units constructor(val value: String) {
        CONCENTRATION_UG_M3("µg/m3"),
        PERCENTAGE("%"),
        TEMPERATURE_KELVIN("K");
    }


    private fun addNestedJsonArray(obj: JsonObject, property: String, value: Double, unit: String) {
        val array = JsonArray()
        array.add(value)
        array.add(unit)
        obj.add(property, array)
    }

    override fun toJson(): JsonObject {
        val obj = JsonObject()

        addNestedJsonArray(obj, "pm.01.value", pm01Value.toDouble(), Units.CONCENTRATION_UG_M3.value)
        addNestedJsonArray(obj, "pm.2_5.value", pm2_5Value.toDouble(), Units.CONCENTRATION_UG_M3.value)
        addNestedJsonArray(obj, "pm.10.value", pm10Value.toDouble(), Units.CONCENTRATION_UG_M3.value)

        addNestedJsonArray(obj, "pm.0_3.above", pm0_3Above.toDouble(), Units.CONCENTRATION_UG_M3.value)
        addNestedJsonArray(obj, "pm.0_5.above", pm0_5Above.toDouble(), Units.CONCENTRATION_UG_M3.value)
        addNestedJsonArray(obj, "pm.1.above", pm1Above.toDouble(), Units.CONCENTRATION_UG_M3.value)
        addNestedJsonArray(obj, "pm.2_5.above", pm2_5Above.toDouble(), Units.CONCENTRATION_UG_M3.value)
        addNestedJsonArray(obj, "pm.5.above", pm5Above.toDouble(), Units.CONCENTRATION_UG_M3.value)
        addNestedJsonArray(obj, "pm.10.above", pm10Above.toDouble(), Units.CONCENTRATION_UG_M3.value)

        addNestedJsonArray(obj, "temperature", tempKelvin.toDouble(), Units.TEMPERATURE_KELVIN.value)
        addNestedJsonArray(obj, "humidity", rh.toDouble(), Units.PERCENTAGE.value)
        addNestedJsonArray(obj, "humidity.compensated", rht, Units.PERCENTAGE.value)

        return obj
    }

    fun setBuff(position: Int, value: Int) {
        this.buff[position] = value
    }

    companion object CREATOR : Parcelable.Creator<IOIOData> {
        override fun createFromParcel(parcel: Parcel): IOIOData {
            return IOIOData(parcel)
        }

        override fun newArray(size: Int): Array<IOIOData?> {
            return arrayOfNulls(size)
        }
    }




}
