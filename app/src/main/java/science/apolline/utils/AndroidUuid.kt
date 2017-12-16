package science.apolline.utils

import java.util.*
import android.os.Build


/**
 * Created by sparow on 12/16/17.
 */


object AndroidUuid {

    fun getAndroidUuid(): String {

        val mszDevIDShort = """35${Build.BOARD.length % 10}${Build.BRAND.length % 10}${Build.TYPE.length % 10}${Build.DEVICE.length % 10}${Build.MANUFACTURER.length % 10}${Build.MODEL.length % 10}${Build.PRODUCT.length % 10}"""

        var serial: String?
        try {
            serial = android.os.Build::class.java.getField("SERIAL").get(null).toString()
            // Go ahead and return the serial for api => 9
            return UUID(mszDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
        } catch (exception: Exception) {
            // String needs to be initialized
            serial = "serial" // some value
        }

        return UUID(mszDevIDShort.hashCode().toLong(), serial!!.hashCode().toLong()).toString()

    }
}