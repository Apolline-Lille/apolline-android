package science.apolline.view.activity

import android.annotation.SuppressLint



import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import science.apolline.R
import science.apolline.service.sensor.BluetoothLeService
import science.apolline.utils.SampleGattAttributes
import java.util.ArrayList
import java.util.HashMap

class Main2Activity : AppCompatActivity() {



    private var mDeviceAddress: String? = null
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? = ArrayList()
    private var mConnected = false


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true
                invalidateOptionsMenu()
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false
                invalidateOptionsMenu()
                // clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(Main2Activity.mBluetoothLeService!!.getSupportedGattServices())

                if (mGattCharacteristics != null) {
                    val characteristic = mGattCharacteristics!![3][0]
                    val charaProp = characteristic.properties
                    if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (Main2Activity.mNotifyCharacteristic != null) {
                            Main2Activity.mBluetoothLeService!!.setCharacteristicNotification(
                                    Main2Activity.mNotifyCharacteristic!!, false)
                            Main2Activity.mNotifyCharacteristic = null
                        }
                        Main2Activity.mBluetoothLeService!!.readCharacteristic(characteristic)
                    }
                    if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        Main2Activity.mNotifyCharacteristic = characteristic
                        Main2Activity.mBluetoothLeService!!.setCharacteristicNotification(
                                characteristic, true)
                    }
                }
            }
        }
    }


    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Main2Activity.mBluetoothLeService = (service as BluetoothLeService.LocalBinder).getService()
            if (!Main2Activity.mBluetoothLeService!!.initialize()) {
                Log.e(Main2Activity.TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.

            Main2Activity.mBluetoothLeService!!.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Main2Activity.mBluetoothLeService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        val intent = getIntent()
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME)
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS)

        println("haaaaa 5 " + mDeviceName + " " + mDeviceAddress)



        val gattServiceIntent = Intent(this@Main2Activity, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)



        PM1 = findViewById(R.id.textView11)
        PM2_5 = findViewById(R.id.textView12)
        PM10 = findViewById(R.id.textView13)


        println("haaaaa 6 " + PM10!!.text)

    }


    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver, Main2Activity.makeGattUpdateIntentFilter())
        if (Main2Activity.mBluetoothLeService != null) {
            val result = Main2Activity.mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d(Main2Activity.TAG, "Connect request result=$result")
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiver)
    }

    override fun onDestroy() {
        // Send command to BLE
        if (Main2Activity.mNotifyCharacteristic != null) {
            Main2Activity.mNotifyCharacteristic!!.setValue("d")
            Main2Activity.mBluetoothLeService!!.writeCharacteristic(Main2Activity.mNotifyCharacteristic!!)
        }
        super.onDestroy()
        unbindService(mServiceConnection)
        Main2Activity.mBluetoothLeService = null
    }



    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String
        val unknownServiceString = resources.getString(R.string.unknown_service)
        val unknownCharaString = resources.getString(R.string.unknown_characteristic)
        val gattServiceData = ArrayList<HashMap<String, String>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String>>>()
        mGattCharacteristics = ArrayList()

        // Loops through available GATT Services.
        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            val LIST_NAME = "NAME"
            currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownServiceString)
            val LIST_UUID = "UUID"
            currentServiceData[LIST_UUID] = uuid
            gattServiceData.add(currentServiceData)

            val gattCharacteristicGroupData = ArrayList<HashMap<String, String>>()
            val gattCharacteristics = gattService.characteristics
            val charas = ArrayList<BluetoothGattCharacteristic>()

            // Loops through available Characteristics.
            for (gattCharacteristic in gattCharacteristics) {
                charas.add(gattCharacteristic)
                val currentCharaData = HashMap<String, String>()
                uuid = gattCharacteristic.uuid.toString()
                currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(uuid, unknownCharaString)
                currentCharaData[LIST_UUID] = uuid
                gattCharacteristicGroupData.add(currentCharaData)
            }
            mGattCharacteristics!!.add(charas)
            gattCharacteristicData.add(gattCharacteristicGroupData)
        }
    }

    companion object {
        internal val TAG = Main2Activity::class.java!!.getSimpleName()

        internal val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
        internal val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"

        internal var mDeviceName: String = ""
        internal var mBluetoothLeService: BluetoothLeService? = null
        internal var mNotifyCharacteristic: BluetoothGattCharacteristic? = null

        @SuppressLint("StaticFieldLeak")
        internal var PM1: TextView? = null
        @SuppressLint("StaticFieldLeak")
        internal var PM2_5: TextView? = null
        @SuppressLint("StaticFieldLeak")
        internal var PM10: TextView? = null
        @SuppressLint("StaticFieldLeak")
        internal var Temp_sensor: TextView? = null
        @SuppressLint("StaticFieldLeak")
        internal var Kmh_sensor: TextView? = null
        @SuppressLint("StaticFieldLeak")
        internal var Pressure_sensor: TextView? = null
        @SuppressLint("StaticFieldLeak")
        internal var Map: GoogleMap? = null
        @SuppressLint("StaticFieldLeak")
        internal var mapFragment: MapFragment? = null
        @SuppressLint("StaticFieldLeak")
        internal var img: ImageView? = null

        private fun makeGattUpdateIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
            return intentFilter
        }
    }
}
