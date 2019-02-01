package science.apolline.utils


import java.util.HashMap

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
internal object SampleGattAttributes {
    private val attributes = HashMap<String,String>()
    var DATA_DUST_SENSOR = "49535343-1E4D-4BD9-BA61-23C647249616"
    var CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

    init {
        // Sample Services.
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service")
        // Sample Characteristics.
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String")
    }

    fun lookup(uuid: String, defaultName: String): String {
        val name = attributes.get(uuid)
        return if (name == null) defaultName else name
    }
}
