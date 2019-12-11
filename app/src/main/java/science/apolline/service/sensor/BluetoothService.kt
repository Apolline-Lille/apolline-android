package science.apolline.service.sensor

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import science.apolline.service.bluetooth.BluetoothGateway
import science.apolline.utils.SampleGattAttributes
import java.util.*

class BluetoothService : Service() {

    companion object{
        val TAG = "apoline";
        val UUID_DATA_DUST_SENSOR = UUID.fromString(SampleGattAttributes.DATA_DUST_SENSOR);
    }

    override fun onBind( intent : Intent ): IBinder? {
        return binder
    }

    private var binder = BluetoothServiceBinder()

    internal inner class BluetoothServiceBinder : Binder(){

        /**
         * Get service instance
         */
        fun getService() = this@BluetoothService;
    }

    /**
     * @brief After using a given device, you should make sure that BluetoothGatt.close() is called
     * such that resources are cleaned up properly.  In this particular example, close() is
     * invoked when the UI is disconnected from the Service.
     */
    override fun onUnbind(intent: Intent): Boolean {
        close()
        return super.onUnbind(intent)
    }

    /**
     * @brief It's the bluetooth adaptater
     */
    private var mBluetoothManager : BluetoothManager? = null;

    /**
     * @brief It's the bluetooth adaptater
     */
    private var mBluetoothAdapter : BluetoothAdapter? = null;

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService( Context.BLUETOOTH_SERVICE ) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e( TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }

        mBluetoothAdapter = mBluetoothManager!!.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mGattCallback = BluetoothGateway();
    fun getGateway() = mGattCallback

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            return mBluetoothGatt!!.connect()
        }

        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.

        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)

        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address

        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.disconnect()
    }



    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.readCharacteristic(characteristic)

    }

    /**
     * Write information to the device on a given `BluetoothGattCharacteristic`. The content string and characteristic is
     * only pushed into a ring buffer. All the transmission is based on the `onCharacteristicWrite` call back function,
     * which is called directly in this function
     *
     * @param characteristic The characteristic to write to.
     */
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.writeCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic,
                                      enabled: Boolean) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)

        // This is specific to DUST SENSOR. D.C 20180112
        if (UUID_DATA_DUST_SENSOR == characteristic.uuid) {
            val desc = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBluetoothGatt!!.writeDescriptor(desc)
        }
    }



    internal fun getSupportedGattServices(): List<BluetoothGattService>? {
        if (mBluetoothGatt == null) {
            return null
        }
        return mBluetoothGatt!!.services
    }

}