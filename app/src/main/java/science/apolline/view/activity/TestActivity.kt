package science.apolline.view.activity

import science.apolline.R
import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic

import android.content.Context
import android.content.Intent

import android.content.pm.PackageManager

import android.os.Bundle


import android.util.Log

import android.widget.Toast

import java.util.ArrayList




import science.apolline.service.sensor.BluetoothLeService


import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager

import 	android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult





/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with `BluetoothLeService`, which in turn interacts with the
 * Bluetooth LE API.
 */
class TestActivity : Activity() {

    private var mBluetoothAdapter : BluetoothAdapter? = null
    private var mScanning : Boolean? = null
    private var mHandler : Handler? = null

    internal val mLeDevices : ArrayList<BluetoothDevice>? = ArrayList()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    private var mDeviceAddress: String? = null








    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gatt_services_characteristics)

        mHandler = Handler()

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this@TestActivity, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        val bluetoothManager : BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.getAdapter()


        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this@TestActivity, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish()
        }




        mLeDevices?.clear()
        scanLeDevice(true)





    }


    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed({
                mScanning = false
                mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mLeScanCallback)

                invalidateOptionsMenu()
            }, SCAN_PERIOD)

            mScanning = true

            mBluetoothAdapter!!.bluetoothLeScanner.startScan(mLeScanCallback)

        } else {
            mScanning = false
            mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mLeScanCallback)
        }
        invalidateOptionsMenu()
    }

    // Device scan callback.
    private val mLeScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d("ScanDeviceActivity", "onScanResult(): ${result?.device?.address} - ${result?.device?.name}")

            if(!mLeDevices!!.contains(result!!.device)) {
                if(result!!.device.name == ("APPA-09_v3.3-6699")){
                mLeDevices!!.add(result!!.device)

                    mDeviceAddress =  result!!.device.address
                mDeviceName =  result!!.device.name

                //scanLeDevice(false)
                    val intent = Intent(this@TestActivity, Main2Activity::class.java)
                    intent.putExtra(Main2Activity.EXTRAS_DEVICE_NAME, mLeDevices!!.get(0).getName());
                    intent.putExtra(Main2Activity.EXTRAS_DEVICE_ADDRESS, mLeDevices!!.get(0).getAddress());
                    startActivity(intent!!)

                }
            }










        }
    }





    companion object {




        internal var mDeviceName: String = ""
        internal var mBluetoothLeService: BluetoothLeService? = null
        internal var mNotifyCharacteristic: BluetoothGattCharacteristic? = null



    }
}
