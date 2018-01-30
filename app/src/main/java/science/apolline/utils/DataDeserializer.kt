package science.apolline.utils

import com.google.gson.*

import java.lang.reflect.Type

import science.apolline.models.IOIOData

/**
 * Created by aoudia on 30/01/18.
 */

class DataDeserializer : JsonDeserializer<IOIOData> {

    @Throws(JsonParseException::class)
    override fun deserialize(jsonParam: JsonElement, typeOfT: Type, context: JsonDeserializationContext): IOIOData? {
        val ioio = IOIOData()
        val gson = Gson()
        val json = gson.fromJson(jsonParam,JsonObject::class.java)

        ioio.pm01Value = json.getAsJsonArray("pm.01.value").get(0).asInt
        ioio.pm0_3Above = json.getAsJsonArray("pm.0_3.above").get(0).asInt
        ioio.pm0_5Above = json.getAsJsonArray("pm.0_5.above").get(0).asInt
        ioio.pm10Above = json.getAsJsonArray("pm.10.above").get(0).asInt
        ioio.pm10Value = json.getAsJsonArray("pm.10.value").get(0).asInt
        ioio.pm1Above = json.getAsJsonArray("pm.1.above").get(0).asInt
        ioio.pm2_5Above = json.getAsJsonArray("pm.2_5.above").get(0).asInt
        ioio.pm2_5Value = json.getAsJsonArray("pm.2_5.value").get(0).asInt
        ioio.pm5Above = json.getAsJsonArray("pm.5.above").get(0).asInt
        ioio.tempKelvin = json.getAsJsonArray("temperature").get(0).asFloat
        ioio.rh = json.getAsJsonArray("humidity").get(0).asFloat
        ioio.rht = json.getAsJsonArray("humidity.compensated").get(0).asDouble

        return ioio
    }
}
