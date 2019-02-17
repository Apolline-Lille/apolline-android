package science.apolline.view.activity


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.FragmentManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.WifiManager.WifiLock
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager.WakeLock
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.birbit.android.jobqueue.JobManager
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.PermissionRequest
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import es.dmoral.toasty.Toasty
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import science.apolline.BuildConfig
import science.apolline.R
import science.apolline.root.RootActivity
import science.apolline.root.RootFragment
import science.apolline.service.sensor.BluetoothLeService
import science.apolline.service.sensor.IOIOService
import science.apolline.service.synchronisation.SyncInfluxDBJob
import science.apolline.utils.CheckUtility
import science.apolline.utils.SampleGattAttributes
import science.apolline.utils.SyncJobScheduler
import science.apolline.utils.SyncJobScheduler.cancelAutoSync
import science.apolline.view.fragment.ViewPagerFragment
import java.util.ArrayList
import java.util.HashMap


class MainActivity : RootActivity(), NavigationView.OnNavigationItemSelectedListener, AnkoLogger {

    // IOIO
    private val mJobManager by instance<JobManager>()
     val mFragmentViewPager by instance<ViewPagerFragment>()
    private val mWakeLock: WakeLock by with(this as AppCompatActivity).instance()
    private val mWifiLock: WifiLock by with(this as AppCompatActivity).instance()
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var mRequestLocationAlert: AlertDialog
    private lateinit var mDisposable: CompositeDisposable

    private var SYNC_MOD = 2 // Wi-Fi only
    private var INFLUXDB_SYNC_FREQ: Long = -1


    // APPA
    private var mDeviceAddress: String? = null
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? = ArrayList()
    private var mConnected = false
    @SuppressLint("StaticFieldLeak")
    var img: ImageView? = null

    private var distroyed = false




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

                if(distroyed == false) {
                    val builder = AlertDialog.Builder(this@MainActivity)

                    builder.setTitle("capteur deconnecté")

                    builder.setMessage("Le capteur a été deconnecté. Vérifiez qu'il est allumé et qu'il est assez proche de votre de téléphone.")

                    builder.setPositiveButton("reconnecter"){dialog, which ->
                        registerReceiver(this, MainActivity.makeGattUpdateIntentFilter())
                        if (MainActivity.mBluetoothLeService != null) {
                            val result = MainActivity.mBluetoothLeService!!.connect(mPrefs.getString("sensor_mac_address","address not found"))
                            Log.d(MainActivity.TAG, "Connect request result=$result")
                        }
                    }


                    builder.setNegativeButton("Ok"){dialog,which ->
                    }



                    val dialog: AlertDialog = builder.create()

                    dialog.show()
                }


                invalidateOptionsMenu()


                // clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(MainActivity.mBluetoothLeService!!.getSupportedGattServices())

                if (mGattCharacteristics != null) {


                    val characteristic = mGattCharacteristics!![3][0]
                    val charaProp = characteristic.properties
                    if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (MainActivity.mNotifyCharacteristic != null) {

                            MainActivity.mBluetoothLeService!!.setCharacteristicNotification(
                                    MainActivity.mNotifyCharacteristic!!, false)
                            MainActivity.mNotifyCharacteristic = null
                        }
                        MainActivity.mBluetoothLeService!!.readCharacteristic(characteristic)
                    }
                    if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                        MainActivity.mNotifyCharacteristic = characteristic
                        MainActivity.mBluetoothLeService!!.setCharacteristicNotification(
                                characteristic, true)
                    }
                }
            }
        }
    }


    // Code to manage Service lifecycle.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {


            MainActivity.mBluetoothLeService = (service as BluetoothLeService.LocalBinder).getService()
            if (!MainActivity.mBluetoothLeService!!.initialize()) {
                Log.e(MainActivity.TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.

            var bundle = Bundle()
            bundle.putString("sensor_name" , mPrefs.getString("sensor_name" , "sensor_name does not exist"))
            mFragmentViewPager.setArguments(bundle)

            replaceFragment(mFragmentViewPager)
            registerReceiver(mGattUpdateReceiver, MainActivity.makeGattUpdateIntentFilter())
            MainActivity.mBluetoothLeService!!.connect(mPrefs.getString("sensor_mac_address","address not found"))



        }

        override fun onServiceDisconnected(componentName: ComponentName) {

            MainActivity.mBluetoothLeService = null
        }
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





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mDisposable = CompositeDisposable()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_drawer)
        navigationView.setNavigationItemSelectedListener(this)

        val version = "Version: " + BuildConfig.VERSION_NAME
        app_version.text = version

        // Preferences.
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        SYNC_MOD = (mPrefs.getString("sync_mod", "2")).toInt()
        INFLUXDB_SYNC_FREQ = (mPrefs.getString("sync_frequency", "60")).toLong()



        // Launch AutoSync
        SyncJobScheduler.setAutoSync(SYNC_MOD, INFLUXDB_SYNC_FREQ, this)



        if (mPrefs.getString("sensor_name" , "sensor_name does not exist").toLowerCase().contains(regex = "^ioio.".toRegex())) {
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffdc41")))
            var bundle = Bundle()
            bundle.putString("sensor_name" , mPrefs.getString("sensor_name" , "sensor_name does not exist"))
            mFragmentViewPager.setArguments(bundle)
             startService(Intent(applicationContext, IOIOService::class.java))
            replaceFragment(mFragmentViewPager)

        }

    }

    override fun onStart() {
        super.onStart()

        //APPA
        if (mPrefs.getString("sensor_name" , "sensor_name does not exist").toLowerCase().contains(regex = "^appa.".toRegex())) {

            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#428aff")))
            val gattServiceIntent = Intent(this@MainActivity, BluetoothLeService::class.java)

            bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)


        }
        SYNC_MOD = (mPrefs.getString("sync_mod", "2")).toInt()
        INFLUXDB_SYNC_FREQ = (mPrefs.getString("sync_frequency", "60")).toLong()
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            val backStackEntryCount = supportFragmentManager.backStackEntryCount
            if (backStackEntryCount == 1) {
                if (IOIOService.getServiceStatus()){
                    stopService(Intent(applicationContext, IOIOService::class.java))
                }
                if(mPrefs.getString("sensor_name" , "sensor_name does not exist").toLowerCase().contains(regex = "^appa.".toRegex())) {


                }
                val intent = Intent(this, SplashScreen::class.java)
                startActivity(intent)
                finish()
            } else {
                if (backStackEntryCount > 1) {
                    supportFragmentManager.popBackStack()
                } else {
                    super.onBackPressed()
                }
                super.onBackPressed()
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            R.id.sync -> {
                when (SYNC_MOD) {
                    0 -> {
                        info("User denied sync job")
                        Toasty.warning(applicationContext, "Please enable synchronization from settings", Toast.LENGTH_LONG, true).show()
                        return true
                    }
                    1 -> {
                        if (CheckUtility.isNetworkConnected(this)) {
                            mJobManager.addJobInBackground(SyncInfluxDBJob())
                            Toasty.info(applicationContext, "Synchronization in progress (Mobile)", Toast.LENGTH_SHORT, true).show()
                            return true
                        } else {
                            Toasty.warning(applicationContext, "Please enable your data mobile or change synchronization policy", Toast.LENGTH_SHORT, true).show()
                        }
                    }
                    2 -> {
                        if (CheckUtility.isWifiNetworkConnected(this)) {
                            mJobManager.addJobInBackground(SyncInfluxDBJob())
                            Toasty.info(applicationContext, "Synchronization in progress (Wi-Fi)", Toast.LENGTH_SHORT, true).show()
                            return true
                        } else {
                            Toasty.warning(applicationContext, "Please enable your Wi-Fi connection or change synchronization policy", Toast.LENGTH_SHORT, true).show()
                        }
                    }

                    else -> {
                        mJobManager.addJobInBackground(SyncInfluxDBJob())
                        Toasty.warning(applicationContext, "No internet connection ! Synchronization job added to queue", Toast.LENGTH_LONG, true).show()
                        return false
                    }

                }
            }

            R.id.start -> {
                if (mPrefs.getString("sensor_name" , "sensor_name does not exist").toLowerCase().contains(regex = "^appa.".toRegex())) {
                    registerReceiver(mGattUpdateReceiver, MainActivity.makeGattUpdateIntentFilter())
                    if (MainActivity.mBluetoothLeService != null) {
                        val result = MainActivity.mBluetoothLeService!!.connect(mPrefs.getString("sensor_mac_address","address not found"))
                        Log.d(MainActivity.TAG, "Connect request result=$result")
                    }

                }
                else {
                    stopService(Intent(applicationContext, IOIOService::class.java))
                    startService(Intent(applicationContext, IOIOService::class.java))
                    return true
                }
            }
            R.id.pause -> {
                if (mPrefs.getString("sensor_name" , "sensor_name does not exist").toLowerCase().contains(regex = "^appa.".toRegex())) {

                    MainActivity.mBluetoothLeService!!.disconnect()
                    return true

                }
                else {
                    stopService(Intent(applicationContext, IOIOService::class.java))
                    return true
                }
            }
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val itemId = item.itemId
        val groupId = item.groupId

        when (groupId) {
            R.id.grp_capteur -> if (itemId == R.id.nav_ioio) {
                val viewPagerFragment = ViewPagerFragment()
                replaceFragment(viewPagerFragment)
            }
            R.id.grp_fonction -> if (itemId == R.id.nav_setting) {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            else -> {

            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }


    private fun replaceFragment(fragment: Fragment) {
        val backStateName = fragment.javaClass.name

        val manager = supportFragmentManager
        val fragmentPopped = manager.popBackStackImmediate(backStateName, 0)

        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) { //fragment not in back stack, create it.
            val ft = manager.beginTransaction()
            ft.replace(R.id.fragment, fragment, backStateName)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.addToBackStack(backStateName)
            ft.commit()
        }
    }



    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()

    }


    override fun onDestroy() {
        distroyed = true
        if (mWakeLock.isHeld) {
            mWakeLock.release()
            info("WakeLock released")
        } else if (mWifiLock.isHeld) {
            mWifiLock.release()
            info("WifiLock released")
        }
        super.onDestroy()
        cancelAutoSync(false)
        if (mPrefs.getString("sensor_name" , "sensor_name does not exist").toLowerCase().contains(regex = "^ioio.".toRegex())) {
            stopService(Intent(this, IOIOService::class.java))

        }
        else {

            MainActivity.mBluetoothLeService!!.disconnect()
            unbindService(mServiceConnection)

        }

    }

    private fun checkBlueToothState() {

        if (mBluetoothAdapter == null) {
            Toasty.error(applicationContext, "Bluetooth NOT supported.", Toast.LENGTH_SHORT, true).show()
        } else {
            if (mBluetoothAdapter!!.isEnabled) {
                if (mBluetoothAdapter!!.isDiscovering) {
                    Toasty.info(applicationContext, "Bluetooth is currently in device discovery process.", Toast.LENGTH_SHORT, true).show()
                } else {
                    info("Bluetooth is Enabled.")
                }
            } else {
                Toasty.warning(applicationContext, "Bluetooth NOT enabled.", Toast.LENGTH_SHORT, true).show()
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_CODE_ENABLE_BLUETOOTH)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                if (!IOIOService.getServiceStatus()) {
                    stopService(Intent(applicationContext, IOIOService::class.java))
                    startService(Intent(applicationContext, IOIOService::class.java))
                }
                else {
                    registerReceiver(mGattUpdateReceiver, MainActivity.makeGattUpdateIntentFilter())
                    if (MainActivity.mBluetoothLeService != null) {
                        val result = MainActivity.mBluetoothLeService!!.connect(mPrefs.getString("sensor_mac_address","address not found"))
                        Log.d(MainActivity.TAG, "Connect request result=$result")
                    }
                }
                Toasty.success(applicationContext, "Bluetooth is Enabled.", Toast.LENGTH_SHORT, true).show()
            } else {
                //checkBlueToothState()
                Toasty.error(applicationContext, "Bluetooth NOT enabled", Toast.LENGTH_LONG, true).show()
            }

        }
    }

    companion object {
        private const val REQUEST_CODE_ENABLE_BLUETOOTH = 101

        // APPA
        internal val TAG = MainActivity::class.java!!.getSimpleName()
        internal lateinit var mPrefs: SharedPreferences
        internal val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
        internal val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"

        var mFragment : Fragment? = null

        internal var mDeviceName: String = ""
         var mBluetoothLeService: BluetoothLeService? = null
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
