package science.apolline.models


import android.os.Parcel
import android.os.Parcelable

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class APPAData : IntfSensorData {
    var count: Int = 0
    val buff = IntArray(64)

    var pm01Value = 0
    var pm2_5Value = 0
    var pm10Value = 0

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



    var dateGPS: String = ""
    var AltiGPS: Double = 0.toDouble()
    var SpeedGPS: Double = 0.toDouble()
    var pression: Double = 0.toDouble()
    var tempe: Double = 0.toDouble()
    var humi: Double = 0.toDouble()
    var bat_volt: Double = 0.toDouble()
    var SatGPS: Int = 0

    constructor()

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

        for (i in 0 until LENG - 2)
            receiveSum = receiveSum + buff[i]
        receiveSum = receiveSum + 0x42

        if (receiveSum == (buff[LENG - 2] shl 8) + buff[LENG - 1])
            receiveflag = true
        return receiveflag
    }

    fun parse() {
        pm01Value = extract(buff, 3, 4)
        pm2_5Value = extract(buff, 5, 6)
        pm10Value = extract(buff, 7, 8)
        pm0_3Above = extract(buff, 15, 16)
        pm0_5Above = extract(buff, 17, 18)
        pm1Above = extract(buff, 19, 20)
        pm2_5Above = extract(buff, 21, 22)
        pm5Above = extract(buff, 23, 24)
        pm10Above = extract(buff, 25, 26)
    }

    private fun extract(buffer: IntArray, lhs: Int, rhs: Int) = (buffer[lhs] shl 8) + buffer[rhs]


    enum class Units constructor(val value: String) {
        CONCENTRATION_UG_M3("µg/m3"),
        CONCENTRATION_ABOVE("#/0.1L"),
        PERCENTAGE("%"),
        TEMPERATURE_CELSIUS("°C"),
        TEMPERATURE_KELVIN("°K");
    }


    private fun addNestedJsonArray(obj: JsonObject, property: String, value: Number, unit: Units) {
        val array = JsonArray()
        array.add(value.toDouble())
        array.add(unit.value)
        obj.add(property, array)
    }

    override fun toJson(): JsonObject {
        val obj = JsonObject()

        addNestedJsonArray(obj, "pm.01.value", pm01Value, Units.CONCENTRATION_UG_M3)
        addNestedJsonArray(obj, "pm.2_5.value", pm2_5Value, Units.CONCENTRATION_UG_M3)
        addNestedJsonArray(obj, "pm.10.value", pm10Value, Units.CONCENTRATION_UG_M3)

        addNestedJsonArray(obj, "pm.0_3.above", pm0_3Above, Units.CONCENTRATION_ABOVE)
        addNestedJsonArray(obj, "pm.0_5.above", pm0_5Above, Units.CONCENTRATION_ABOVE)
        addNestedJsonArray(obj, "pm.1.above", pm1Above, Units.CONCENTRATION_ABOVE)
        addNestedJsonArray(obj, "pm.2_5.above", pm2_5Above, Units.CONCENTRATION_ABOVE)
        addNestedJsonArray(obj, "pm.5.above", pm5Above, Units.CONCENTRATION_ABOVE)
        addNestedJsonArray(obj, "pm.10.above", pm10Above, Units.CONCENTRATION_ABOVE)

        addNestedJsonArray(obj, "temperature.c", tempCelcius, Units.TEMPERATURE_CELSIUS)
        addNestedJsonArray(obj, "temperature.k", tempKelvin, Units.TEMPERATURE_KELVIN)
        addNestedJsonArray(obj, "humidity", rh, Units.PERCENTAGE)
        addNestedJsonArray(obj, "humidity.compensated", rht, Units.PERCENTAGE)

        return obj
    }

    fun setBuff(position: Int, value: Int) {
        this.buff[position] = value
    }

    companion object CREATOR : Parcelable.Creator<APPAData> {
        override fun createFromParcel(parcel: Parcel): APPAData? {
            return APPAData(parcel)
        }

        override fun newArray(size: Int): Array<APPAData?> {
            return arrayOfNulls(size)
        }
    }
}
