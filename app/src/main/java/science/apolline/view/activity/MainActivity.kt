package science.apolline.view.activity


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.net.wifi.WifiManager.WifiLock
import android.os.Bundle
import android.os.CountDownTimer
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
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.birbit.android.jobqueue.JobManager
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.PermissionRequest
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import science.apolline.BuildConfig
import science.apolline.R
import science.apolline.root.RootActivity
import science.apolline.service.sensor.IOIOService
import science.apolline.service.synchronisation.SyncInfluxDBJob
import science.apolline.utils.CheckUtility
import science.apolline.utils.SyncJobScheduler
import science.apolline.utils.SyncJobScheduler.cancelAutoSync
import science.apolline.view.fragment.ViewPagerFragment


class MainActivity : RootActivity(), NavigationView.OnNavigationItemSelectedListener, AnkoLogger {

    private val mJobManager by instance<JobManager>()
    private val mFragmentViewPager by instance<ViewPagerFragment>()
    private val mWakeLock: WakeLock by with(this as AppCompatActivity).instance()
    private val mWifiLock: WifiLock by with(this as AppCompatActivity).instance()
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var mRequestLocationAlert: AlertDialog
    private lateinit var mPrefs: SharedPreferences
    private var SYNC_MOD = 2 // Wi-Fi only
    private var INFLUXDB_SYNC_FREQ: Long = -1

    private val mRequestPermissions by lazy {
        permissionsBuilder(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // Request enable Bluetooth
        checkBlueToothState()
        // Check permissions
        checkPermissions(mRequestPermissions)
        // Request disable Doze Mode
        CheckUtility.requestDozeMode(this)
        // Request enable location
        mRequestLocationAlert = CheckUtility.requestLocation(this)

        // Launch AutoSync
        SyncJobScheduler.setAutoSync(SYNC_MOD, INFLUXDB_SYNC_FREQ, this)

        replaceFragment(mFragmentViewPager)
    }

    override fun onStart() {
        super.onStart()
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
                stopService(Intent(applicationContext, IOIOService::class.java))
                startService(Intent(applicationContext, IOIOService::class.java))
                return true
            }
            R.id.pause -> {
                stopService(Intent(applicationContext, IOIOService::class.java))
                return true
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

    private fun checkPermissions(request: PermissionRequest) {
        request.detachAllListeners()
        request.send()
        request.listeners {

            onAccepted {
                Toasty.success(applicationContext, "READ_PHONE_STATE and ACCESS_FINE_LOCATION permissions granted.", Toast.LENGTH_SHORT, true).show()
                stopService(Intent(applicationContext, IOIOService::class.java))
                startService(Intent(applicationContext, IOIOService::class.java))
            }

            onDenied {
                Toasty.error(applicationContext, "READ_PHONE_STATE and ACCESS_FINE_LOCATION permissions denied.", Toast.LENGTH_LONG, true).show()
            }

            onPermanentlyDenied {
                Toasty.error(applicationContext, "READ_PHONE_STATE and ACCESS_FINE_LOCATION permissions permanently denied, please grant it manually, Apolline will close in 10 seconds", Toast.LENGTH_LONG, true).show()
                object : CountDownTimer(10000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {

                    }

                    override fun onFinish() {
                        finish()
                    }
                }.start()
            }

            onShouldShowRationale { _, _ ->

                alert("Apolline will not work, please grant READ_PHONE_STATE and ACCESS_FINE_LOCATION permissions.", "Request read phone state and location permissions") {
                    yesButton {
                        checkPermissions(mRequestPermissions)
                    }
                    noButton {
                        checkPermissions(mRequestPermissions)
                    }
                }.show()

            }
        }
    }


    override fun onDestroy() {
        if (mWakeLock.isHeld) {
            mWakeLock.release()
            info("WakeLock released")
        } else if (mWifiLock.isHeld) {
            mWifiLock.release()
            info("WifiLock released")
        }
        super.onDestroy()
        cancelAutoSync(false)
        stopService(Intent(this, IOIOService::class.java))
        if (mRequestLocationAlert.isShowing) {
            mRequestLocationAlert.cancel()
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
                Toasty.success(applicationContext, "Bluetooth is Enabled.", Toast.LENGTH_SHORT, true).show()
            } else {
                //checkBlueToothState()
                Toasty.error(applicationContext, "Bluetooth NOT enabled", Toast.LENGTH_LONG, true).show()
            }

        }
    }

    companion object {
        private const val REQUEST_CODE_ENABLE_BLUETOOTH = 101
    }
}
