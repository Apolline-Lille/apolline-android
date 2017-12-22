package science.apolline.models

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class IOIOData : IntfSensorData {
    ////////////////////////////////////////////////////////////////////////////////////////////////////


    var count: Int = 0
    val buff = IntArray(64)

    var pM01Value = 0          //define PM1.0 value of the air detector module
    var pM2_5Value = 0         //define PM2.5 value of the air detector module
    var pM10Value = 0         //define PM10 value of the air detector module

    var pM0_3Above = 0
    var pM0_5Above = 0
    var pM1Above = 0
    var pM2_5Above = 0
    var pM5Above = 0
    var pM10Above = 0

    var tempKelvin = 0f
    var rh = 0f
    var rht = 0.0

    val LENG: Byte = 31

    val tempCelcius: Float
        get() = tempKelvin - 273.15f

    constructor() {}

    protected constructor(`in`: Parcel) {
        pM01Value = `in`.readInt()
        pM2_5Value = `in`.readInt()
        pM10Value = `in`.readInt()
        pM0_3Above = `in`.readInt()
        pM0_5Above = `in`.readInt()
        pM1Above = `in`.readInt()
        pM2_5Above = `in`.readInt()
        pM5Above = `in`.readInt()
        pM10Above = `in`.readInt()
        tempKelvin = `in`.readFloat()
        rh = `in`.readFloat()
        rht = `in`.readDouble()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(pM01Value)
        parcel.writeInt(pM2_5Value)
        parcel.writeInt(pM10Value)

        parcel.writeInt(pM0_3Above)
        parcel.writeInt(pM0_5Above)
        parcel.writeInt(pM1Above)
        parcel.writeInt(pM2_5Above)
        parcel.writeInt(pM5Above)
        parcel.writeInt(pM10Above)

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
        pM01Value = (buff[3] shl 8) + buff[4]
        pM2_5Value = (buff[5] shl 8) + buff[6]
        pM10Value = (buff[7] shl 8) + buff[8]
        pM0_3Above = (buff[15] shl 8) + buff[16]
        pM0_5Above = (buff[17] shl 8) + buff[18]
        pM1Above = (buff[19] shl 8) + buff[20]
        pM2_5Above = (buff[21] shl 8) + buff[22]
        pM5Above = (buff[23] shl 8) + buff[24]
        pM10Above = (buff[25] shl 8) + buff[26]
    }


    enum class Units constructor(val value: String) {
        CONCENTRATION_UG_M3("ug/m3"),
        PERCENTAGE("%"),
        TEMPERATURE_KELVIN("K");
    }


    private fun addNestedJsonArray(obj: JsonObject, property: String, value: Double, unit: Units) {
        val array = JsonArray()
        array.add(value)
        array.add(unit.toString())
        obj.add(property, array)
    }

    override fun toJson(): JsonObject {
        val obj = JsonObject()

        addNestedJsonArray(obj, "pm.01.value", pM01Value.toDouble(), Units.CONCENTRATION_UG_M3)
        addNestedJsonArray(obj, "pm.2.5.value", pM2_5Value.toDouble(), Units.CONCENTRATION_UG_M3)
        addNestedJsonArray(obj, "pm.10.value", pM10Value.toDouble(), Units.CONCENTRATION_UG_M3)

        addNestedJsonArray(obj, "pm.0.3.above", pM0_3Above.toDouble(), Units.CONCENTRATION_UG_M3)
        addNestedJsonArray(obj, "pm.0.5.above", pM0_5Above.toDouble(), Units.CONCENTRATION_UG_M3)
        addNestedJsonArray(obj, "pm.1.above", pM1Above.toDouble(), Units.CONCENTRATION_UG_M3)
        addNestedJsonArray(obj, "pm.2.5.above", pM2_5Above.toDouble(), Units.CONCENTRATION_UG_M3)
        addNestedJsonArray(obj, "pm.5.above", pM5Above.toDouble(), Units.CONCENTRATION_UG_M3)
        addNestedJsonArray(obj, "pm.10.above", pM10Above.toDouble(), Units.CONCENTRATION_UG_M3)

        addNestedJsonArray(obj, "temperature", tempKelvin.toDouble(), Units.TEMPERATURE_KELVIN)
        addNestedJsonArray(obj, "humidity", rh.toDouble(), Units.PERCENTAGE)
        addNestedJsonArray(obj, "humidity.compensated", rht, Units.PERCENTAGE)

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
