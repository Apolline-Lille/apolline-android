package science.apolline.view.Activity


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
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
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.config.Configuration
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.*
import permissions.dispatcher.*

import science.apolline.R
import science.apolline.service.sensor.IOIOService
import science.apolline.service.synchronisation.SyncInfluxDBJob
import science.apolline.view.Fragment.IOIOFragment

@RuntimePermissions
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, AnkoLogger {

    private lateinit var jobManager: JobManager
    private lateinit var fragmentIOIO: IOIOFragment

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

        fragmentIOIO = IOIOFragment()

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            val enableBlueTooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH)
        }

        // Setup JobManager
        val builder = Configuration.Builder(this)
        jobManager = JobManager(builder.build())

        // AutoGenerated
        checkFineLocationWithPermissionCheck()
        replaceFragmentWithPermissionCheck(fragmentIOIO)
    }


    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // NOTE: delegate the permission handling to generated method
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        info("Synchronisation button clicked")
        jobManager.addJobInBackground(SyncInfluxDBJob())
        toast("Synchronisation launched")
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val itemId = item.itemId
        val groupId = item.groupId

        //

        when (groupId) {
            R.id.grp_capteur -> if (itemId == R.id.nav_ioio) {
                val IOIOFragment = IOIOFragment()
                replaceFragment(IOIOFragment)
            }
            R.id.grp_fonction -> if (itemId == R.id.nav_setting) {
                val intent = Intent(this, OptionsActivity::class.java)
                startActivity(intent)
            } else if (itemId == R.id.nav_info) {
                val intent = Intent(this, InformationActivity::class.java)
                startActivity(intent)
                return true
            } else if (itemId == R.id.nav_contact) {
                val intent = Intent(this, ContactActivity::class.java)
                startActivity(intent)
            }
            else -> {
            }
        }//              if (itemId == R.id.nav_share) {
        //
        //              } else if (itemId == R.id.nav_send) {
        //
        //              }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun checkFineLocation(){
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun replaceFragment(fragment: Fragment) {
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

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun AlertWriteExternalStorage(request: PermissionRequest) {
        val alertdialog = AlertDialog.Builder(this).create()
        alertdialog.setTitle("Permission Write_External_Storage")
        alertdialog.setMessage("Cette permission est nécessaire pour exporter les données dans un fichier")
        alertdialog.setButton(AlertDialog.BUTTON_NEGATIVE, "non") { dialog, which ->
            request.cancel()
            finish()
        }
        alertdialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok ") { dialog, which ->
            request.proceed()
        }
        alertdialog.show()
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun AlertFine(request: PermissionRequest) {
        val alertdialog = AlertDialog.Builder(this).create()
        alertdialog.setTitle("Permission Access_Fine_Location")
        alertdialog.setMessage("Cette permission est nécessaire pour prendre la géolocalisation et au bon fonctionnement de l'application")
        alertdialog.setButton(AlertDialog.BUTTON_NEGATIVE, "non") { dialog, which ->
            request.cancel()
            finish()
        }
        alertdialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok ") { dialog, which ->
            request.proceed()
        }
        alertdialog.show()

    }


    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, IOIOService::class.java))
    }

    companion object {

        const val MY_PREFS_NAME = "MyPrefsFile"
        private const val REQUEST_CODE_ENABLE_BLUETOOTH = 100
        private const val REQUEST_CODE_FINE_LOCATION = 101
        private const val REQUEST_CODE_COARSE_LOCATION = 102
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 103
    }
}
