package science.apolline.service.bluetooth

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Debug
import android.util.Log
import science.apolline.models.SensorMessageModel
import science.apolline.models.deserializer.SensorMessageModelDeserializer
import java.util.*

class BluetoothGateway : BluetoothGattCallback() {

    /**
     * @brief observe the bluetooth change
     */
    private var status = MutableLiveData< Int >();

    /**
     * @return get the observer for bluetooth's status
     */
    fun getStatus() = status;

    /**
     * @brief refresh gateway status
     */
    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if( newState == BluetoothGatt.STATE_CONNECTED || newState == BluetoothGatt.STATE_DISCONNECTED )
            this.status.value = newState
    }

    /**
     * @brief refresh gateway status
     */
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            this.status.value = status
        }
    }

    /**
     * @brief refresh gateway status
     */
    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            this.status.value = status
        }
    }

    /**
     * @brief It's a buffer from bluetooth input
     */
    private var buffer : String = ""

    /**
     * @brief It's the live data container
     */
    private var data = MutableLiveData< SensorMessageModel >()

    /**
     * @brief This object convert raw input into SensorMessageModel object
     */
    private var deserializer = SensorMessageModelDeserializer()

    /**
     * @return the data listener
     */
    fun getData() : LiveData< SensorMessageModel > = data

    /**
     * @brief read the bluetooth input
     */
    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val frequence = 1
        val tempBuff = characteristic.getStringValue(0)
        buffer += tempBuff

        if( !buffer.contains( "\n" ) )
            return;

        var model = SensorMessageModel()
        var success = deserializer.fromString( buffer, model )

        if( !success )
            Log.e( "Apolline", "Bluetooth have been partially parsed. Maybe throw an error here ? Data: $buffer" )

        data.value = model
        buffer = "" //reset buffer
    }

}