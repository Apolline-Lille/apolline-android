package science.apolline.view.Activity


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

import science.apolline.R
import science.apolline.service.sensor.IOIOService
import science.apolline.view.Fragment.IOIOFragment

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            val enableBlueTooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH)
        }

        permissionCheck()

        val IOIOFragment = IOIOFragment()
        replaceFragment(IOIOFragment)
    }

    private fun permissionCheck() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertFine()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_FINE_LOCATION)

            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                AlertCoarse()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_CODE_COARSE_LOCATION)

            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertWriteExternalStorage()
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)

            }
        }
    }

    private fun AlertCoarse() {
        val alertdialog = AlertDialog.Builder(this).create()
        alertdialog.setTitle("Permission Access_Coarse_Location")
        alertdialog.setMessage("Cette permission est nécessaire pour prendre la géolocalisation et au bon fonctionnement de l'application")
        alertdialog.setButton(AlertDialog.BUTTON_NEGATIVE, "non") { dialog, which ->
            dialog.dismiss()
            finish()
        }
        alertdialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok ") { dialog, which ->
            dialog.dismiss()
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), MainActivity.REQUEST_CODE_COARSE_LOCATION)
        }
        alertdialog.show()
    }

    private fun AlertWriteExternalStorage() {
        val alertdialog = AlertDialog.Builder(this).create()
        alertdialog.setTitle("Permission Write_External_Storage")
        alertdialog.setMessage("Cette permission est nécessaire pour exporter les données dans un fichier")
        alertdialog.setButton(AlertDialog.BUTTON_NEGATIVE, "non") { dialog, which ->
            dialog.dismiss()
            finish()
        }
        alertdialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok ") { dialog, which ->
            dialog.dismiss()
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MainActivity.REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
        }
        alertdialog.show()
    }

    private fun AlertFine() {
        val alertdialog = AlertDialog.Builder(this).create()
        alertdialog.setTitle("Permission Access_Fine_Location")
        alertdialog.setMessage("Cette permission est nécessaire pour prendre la géolocalisation et au bon fonctionnement de l'application")
        alertdialog.setButton(AlertDialog.BUTTON_NEGATIVE, "non") { dialog, which ->
            dialog.dismiss()
            finish()
        }
        alertdialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok ") { dialog, which ->
            dialog.dismiss()
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MainActivity.REQUEST_CODE_FINE_LOCATION)
        }
        alertdialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_FINE_LOCATION -> {
                run {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                        if (showRationale) {
                            AlertFine()
                        } else {
                            finish()
                        }
                    }
                }
                run {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                        if (showRationale) {
                            AlertCoarse()
                        } else {
                            finish()
                        }
                    }
                }
                run {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                        if (showRationale) {
                            AlertWriteExternalStorage()
                        } else {
                            finish()
                        }
                    }
                }
            }
            REQUEST_CODE_COARSE_LOCATION -> {
                run {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                        if (showRationale) {
                            AlertCoarse()
                        } else {
                            finish()
                        }
                    }
                }
                run {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                        if (showRationale) {
                            AlertWriteExternalStorage()
                        } else {
                            finish()
                        }
                    }
                }
            }
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
                    if (showRationale) {
                        AlertWriteExternalStorage()
                    } else {
                        finish()
                    }
                }
            }
        }
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
        //        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, IOIOService::class.java))
    }

    companion object {

        val MY_PREFS_NAME = "MyPrefsFile"
        private val REQUEST_CODE_ENABLE_BLUETOOTH = 100
        private val REQUEST_CODE_FINE_LOCATION = 101
        private val REQUEST_CODE_COARSE_LOCATION = 102
        private val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 103
    }
}
