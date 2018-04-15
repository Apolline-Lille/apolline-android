package science.apolline.view.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import science.apolline.R
import kotlinx.android.synthetic.main.content_splash_screen.*
import org.jetbrains.anko.AnkoLogger
import com.szugyi.circlemenu.view.CircleImageView
import android.widget.Toast
import android.view.View
import android.view.animation.RotateAnimation
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.github.salomonbrys.kodein.instance
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import science.apolline.root.RootActivity
import pub.devrel.easypermissions.EasyPermissions
import com.github.ivbaranov.rxbluetooth.predicates.BtPredicate


class SplashScreen : RootActivity(), EasyPermissions.PermissionCallbacks, AnkoLogger {

    private val mRxBluetoothClient by instance<RxBluetooth>()
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var mDisposable: CompositeDisposable
    private var mDetectedDevices = hashMapOf<String, BluetoothDevice?>()
    private lateinit var mPrefs: SharedPreferences
    private var EXTRA_DEVICE_ADDRESS: String = "fffffff-ffff-ffff-ffff-ffffffffffff"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        EXTRA_DEVICE_ADDRESS = mPrefs.getString("device_uuid", "ffffffff-ffff-ffff-ffff-ffffffffffff")
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mDisposable = CompositeDisposable()

        setContentView(R.layout.activity_splash_screen)
        ripple_scan_view.clipToOutline = true
        circle_layout.setOnItemSelectedListener {
            setCurrentItemText()
        }
        circle_layout.setOnItemClickListener {
            pairCurrentDevice()
        }
        circle_layout.setOnCenterClickListener {
            checkBlueToothState()
        }
        circle_layout.setOnRotationFinishedListener {
            val animation = RotateAnimation(0f, 360f, it.width.toFloat() / 2, it.height.toFloat() / 2)
            animation.duration = 250
            it.startAnimation(animation)
            setCurrentItemText()
        }

        setCurrentItemText()
        checkPermissions()
        initBoundedDevices()
    }

    override fun onStart() {
        super.onStart()

        mDisposable.add(mRxBluetoothClient.observeDiscovery()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(BtPredicate.`in`(BluetoothAdapter.ACTION_DISCOVERY_STARTED, BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
                .subscribe { state ->

                    when (state) {
                        BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                            circle_layout.removeAllViews()
                            selected_device_name_textview.text = getString(R.string.splash_bluetooth_scan)
                            ripple_scan_view.startRippleAnimation()
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            ripple_scan_view.stopRippleAnimation()
                            //setCurrentItemText()
                            //selected_textView.text = getString(R.string.splash_bluetooth_scan_finished)

                        }
                        else -> {

                        }
                    }
                })

        mDisposable.add(mRxBluetoothClient.observeDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe { device ->
                    addDeviceToCircleView(device)
                }
        )

        mDisposable.add(mRxBluetoothClient.observeBondState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe { event ->
                    when (event.state) {
                        BluetoothDevice.BOND_NONE -> {
                            info("device bound state BOND_NONE: " + event.bluetoothDevice.name)
                        }
                        BluetoothDevice.BOND_BONDING -> {
                            info("device bound state BOND_BONDING: " + event.bluetoothDevice.name)
                        }
                        BluetoothDevice.BOND_BONDED -> {
                            info("device bound state BOND_BONDED: " + event.bluetoothDevice.name)

                            if (event.bluetoothDevice.name.toString().toLowerCase().contains(regex = "^ioio.".toRegex())) {
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }

                        }
                        else -> {

                        }

                    }
                })

    }

    override fun onStop() {
        if (!mDisposable.isDisposed)
            mDisposable.clear()
        super.onStop()
    }

    override fun onDestroy() {
        if (!mDisposable.isDisposed)
            mDisposable.dispose()
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
    }


    private fun setCurrentItemText() {
//        if (circle_layout.tag != null) {
        val view = circle_layout.selectedItem
        if (view is CircleImageView) {
            selected_device_name_textview.text = view.name
        }
//        }
    }


    private fun pairCurrentDevice() {
//        if (circle_layout.tag != null) {
        val view = circle_layout.selectedItem
        if (view is CircleImageView) {

            mBluetoothAdapter!!.cancelDiscovery()

            selected_device_name_textview.text = view.name
            val deviceName = view.name
            val device = mDetectedDevices[deviceName]
            val boundedDevices = mBluetoothAdapter!!.bondedDevices

            if (boundedDevices.size > 0) {
                if (boundedDevices.contains(device)) {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                device!!.createBond()
            }
        }

    }


    private fun addDeviceToCircleView(device: BluetoothDevice?) {


        if (device != null) {
            info("device: $device")

            val nameOrcode = if (device.name == null) {
                device.address.toString()
            } else {
                device.name.toString()
            }

            mDetectedDevices[nameOrcode] = device

            when (device.bluetoothClass.majorDeviceClass) {

                BluetoothClass.Device.Major.AUDIO_VIDEO -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_audio_video)
                }
                BluetoothClass.Device.Major.COMPUTER -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_computer)
                }
                BluetoothClass.Device.Major.HEALTH -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_health)
                }

                BluetoothClass.Device.Major.IMAGING -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_imaging)
                }

                BluetoothClass.Device.Major.MISC -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_misc)
                }

                BluetoothClass.Device.Major.NETWORKING -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_netwoking)
                }

                BluetoothClass.Device.Major.PERIPHERAL -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_peripheral)
                }

                BluetoothClass.Device.Major.TOY -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_toy)
                }

                BluetoothClass.Device.Major.PHONE -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_phone)
                }

                BluetoothClass.Device.Major.WEARABLE -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_wear)
                }

                else -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_uncategorized)
                }

            }

        }

    }

    private fun initBoundedDevices() {

        val boundedDevices = mBluetoothAdapter!!.bondedDevices
        var sensorCounter = 0

        if (boundedDevices.size > 0) {

            boundedDevices.forEach { device ->

                addDeviceToCircleView(device)

                if (device.name.toString().toLowerCase().contains(regex = "^ioio.".toRegex())) {
                    sensorCounter++
                }
            }

            if (sensorCounter == 1) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                info("There is multiple IOIO Paired sensors, please choose one")
            }

        } else {
            info("No bounded devices")
        }

    }

    private fun onAddClick(view: View, name: String, drawableId: Int) {
        val newMenu = CircleImageView(this)
        val currentDrawable = ContextCompat.getDrawable(this, drawableId)
        val willBeWhite = currentDrawable!!.constantState.newDrawable()
        newMenu.setBackgroundResource(R.drawable.circle_menu_shape_item)
        willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

        newMenu.setPadding(20, 20, 20, 20)
        newMenu.setImageDrawable(willBeWhite)

        newMenu.name = name
        circle_layout.addView(newMenu)
    }


    private fun onRemoveClick(view: View) {
        if (circle_layout.childCount > 0) {
            circle_layout.removeViewAt(circle_layout.childCount - 1)
        }
    }


    private fun checkPermissions(): Boolean {
        if (!EasyPermissions.hasPermissions(this, *PERMISSIONS_ARRAY)) {
            EasyPermissions.requestPermissions(this, "Location permission is necessary for the proper working of Apolline", REQUEST_CODE_PERMISSIONS_ARRAY,
                    Manifest.permission.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {
    }


    private fun checkBlueToothState() {

        if (mBluetoothAdapter == null) {
            Toasty.error(applicationContext, "Bluetooth NOT supported.", Toast.LENGTH_SHORT, true).show()
        } else {
            if (mBluetoothAdapter!!.isEnabled) {
                if (mBluetoothAdapter!!.isDiscovering) {
                    mBluetoothAdapter!!.cancelDiscovery()
                    Toasty.info(applicationContext, "Bluetooth discovering stopped.", Toast.LENGTH_SHORT, true).show()
                } else {
                    mBluetoothAdapter!!.startDiscovery()
                    info("Bluetooth is Enabled, starting discovery...")
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

                if (mBluetoothAdapter!!.isEnabled) {
                    initBoundedDevices()
                    mBluetoothAdapter!!.startDiscovery()
                }

                Toasty.success(applicationContext, "Bluetooth is Enabled, starting discovery...", Toast.LENGTH_SHORT, true).show()
            } else {
                //checkBlueToothState()
                Toasty.error(applicationContext, "Bluetooth NOT enabled.", Toast.LENGTH_LONG, true).show()
            }

        }
    }


    companion object {
        private val PERMISSIONS_ARRAY = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        private const val REQUEST_CODE_PERMISSIONS_ARRAY = 100
        private const val REQUEST_CODE_ENABLE_BLUETOOTH = 101
    }


}
