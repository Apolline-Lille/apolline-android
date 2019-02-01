package science.apolline.view.activity

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.preference.PreferenceManager
import android.support.annotation.IdRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import science.apolline.R
import kotlinx.android.synthetic.main.content_splash_screen.*
import com.szugyi.circlemenu.view.CircleImageView
import android.widget.Toast
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.EditText
import com.fondesa.kpermissions.extension.listeners
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.github.salomonbrys.kodein.instance
import es.dmoral.toasty.Toasty
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import science.apolline.root.RootActivity
import com.github.ivbaranov.rxbluetooth.predicates.BtPredicate
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.PermissionRequest
import ioio.lib.util.android.IOIOService
import org.jetbrains.anko.*
import science.apolline.service.sensor.IOIOService.Companion.getServiceStatus
import science.apolline.utils.CheckUtility
import java.util.ArrayList


class SplashScreen : RootActivity(), AnkoLogger {


    private val mRxBluetoothClient by instance<RxBluetooth>()
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var mDisposable: CompositeDisposable
    private var mDetectedDevices = hashMapOf<String, BluetoothDevice?>()
    private lateinit var mPrefs: SharedPreferences
    private var mIsLocationPermissionGranted = false
    private lateinit var mRequestLocationAlert: AlertDialog

    private var EXTRA_DEVICE_ADDRESS: String = "fffffff-ffff-ffff-ffff-ffffffffffff"
    private var SENSOR_MAC_ADDRESS: String = "ff-ff-ff-ff-ff-ff"

    private val mRequestLocationPermission by lazy {
        permissionsBuilder(Manifest.permission.ACCESS_FINE_LOCATION)
                .build()
    }

    private val mRequestPermissions by lazy {
        permissionsBuilder(Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getServiceStatus()){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        mPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        EXTRA_DEVICE_ADDRESS = mPrefs.getString("device_uuid", "ffffffff-ffff-ffff-ffff-ffffffffffff")
        SENSOR_MAC_ADDRESS = mPrefs.getString("sensor_mac_address", "ff-ff-ff-ff-ff-ff")
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mDisposable = CompositeDisposable()

        mIsLocationPermissionGranted = CheckUtility.isWifiNetworkConnected(this)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()



        setContentView(R.layout.activity_splash_screen)
        ripple_scan_view.clipToOutline = true
        circle_layout.setOnItemSelectedListener {
            setCurrentItemText()
        }
        circle_layout.setOnItemClickListener {
            pairCurrentDevice()
        }
        circle_layout.setOnCenterClickListener {
            if (!mIsLocationPermissionGranted) {
                checkFineLocationPermission(mRequestLocationPermission)
                // Request disable Doze Mode
                CheckUtility.requestDozeMode(this)
                // Request enable location
                mRequestLocationAlert = CheckUtility.requestLocation(this)
                // Check permissions
                checkPermissions(mRequestPermissions)
            } else {
                checkBlueToothState()
            }
        }
        circle_layout.setOnRotationFinishedListener {
            val animation = RotateAnimation(0f, 360f, it.width.toFloat() / 2, it.height.toFloat() / 2)
            animation.duration = 250
            it.startAnimation(animation)
            setCurrentItemText()
        }

        setCurrentItemText()
        checkFineLocationPermission(mRequestLocationPermission)
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

                    addDeviceToCircleView(device, isBounded = isBoundedDevice(device))
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

                            if (event.bluetoothDevice.name.toString().toLowerCase().contains(regex = "^ioio.".toRegex()) ||
                                    event.bluetoothDevice.name.toString().toLowerCase().contains(regex = "^appa.".toRegex())) {

                                val deviceMacAddress = event.bluetoothDevice!!.address.toString()
                                val intent = Intent(this, MainActivity::class.java)

                                if (deviceMacAddress != SENSOR_MAC_ADDRESS) {

                                    mPrefs.edit().putString("sensor_mac_address", deviceMacAddress)
                                            .putString("sensor_name", event.bluetoothDevice.name.toString().toLowerCase())
                                            .apply()
                                    startActivity(intent)
                                    finish()


                                }
                            }

                        }
                        else -> {
                        }

                    }
                })


    }


    private fun checkPermissions(request: PermissionRequest) {
        request.detachAllListeners()
        request.send()
        request.listeners {

            onAccepted {
                Toasty.success(applicationContext, "READ_PHONE_STATE and ACCESS_FINE_LOCATION permissions granted.", Toast.LENGTH_SHORT, true).show()
                stopService(Intent(applicationContext, science.apolline.service.sensor.IOIOService::class.java))
                startService(Intent(applicationContext, science.apolline.service.sensor.IOIOService::class.java))
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

//            if (boundedDevices.size > 0) {
            if (boundedDevices.contains(device)) {

                val deviceMacAddress = device!!.address.toString()
                val intent = Intent(this, MainActivity::class.java)

                if (deviceMacAddress != SENSOR_MAC_ADDRESS) {

                    mPrefs.edit().putString("sensor_mac_address", deviceMacAddress)
                            .putString("sensor_name", device!!.name.toString())
                            .apply()

                    startActivity(intent)
                    finish()

                } else {

                    startActivity(intent)
                    finish()
                }


            } else {
                Toasty.info(applicationContext, "Default PIN code for IOIO sensor is: 4545", Toast.LENGTH_LONG, true).show()
                device!!.createBond()
            }
            //           }
        }

    }


    private fun addDeviceToCircleView(device: BluetoothDevice?, isBounded: Boolean) {

        var isCompatible = false
        if (device != null) {
            info("device: $device")
            val nameOrcode: String
            if (device.name == null) {
                nameOrcode = device.address.toString()
            } else {
                nameOrcode = device.name.toString()
                if (nameOrcode.toLowerCase().contains(regex = "^ioio.".toRegex()) ||
                        nameOrcode.toLowerCase().contains(regex = "^appa.".toRegex())) {
                    isCompatible = true
                }
            }

            mDetectedDevices[nameOrcode] = device

            when (device.bluetoothClass.majorDeviceClass) {

                BluetoothClass.Device.Major.AUDIO_VIDEO -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_audio_video, isCompatible, isBounded)
                }
                BluetoothClass.Device.Major.COMPUTER -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_computer, isCompatible, isBounded)
                }
                BluetoothClass.Device.Major.HEALTH -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_health, isCompatible, isBounded)
                }

                BluetoothClass.Device.Major.IMAGING -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_imaging, isCompatible, isBounded)
                }

                BluetoothClass.Device.Major.MISC -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_misc, isCompatible, isBounded)
                }

                BluetoothClass.Device.Major.NETWORKING -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_netwoking, isCompatible, isBounded)
                }

                BluetoothClass.Device.Major.PERIPHERAL -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_peripheral, isCompatible, isBounded)
                }

                BluetoothClass.Device.Major.TOY -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_toy, isCompatible, isBounded)
                }

                BluetoothClass.Device.Major.PHONE -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_phone, isCompatible, isBounded)
                }

                BluetoothClass.Device.Major.WEARABLE -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_wear, isCompatible, isBounded)
                }

                else -> {
                    onAddClick(circle_layout, nameOrcode, R.drawable.ic_device_bluetooth_uncategorized, isCompatible, isBounded)
                }

            }

        }

    }

    private fun isBoundedDevice(device: BluetoothDevice?): Boolean {
        val boundedDevices = mBluetoothAdapter!!.bondedDevices
        return boundedDevices.contains(device)
    }

    private fun initBoundedDevices() {

        val boundedDevices = mBluetoothAdapter!!.bondedDevices
        var sensorCounter = 0
        var currentCompatibleSensor: BluetoothDevice? = null

        if (boundedDevices.size > 0) {

            boundedDevices.forEach { device ->
                addDeviceToCircleView(device, isBounded = isBoundedDevice(device))

                if (device.name.toString().toLowerCase().contains(regex = "^ioio.".toRegex()) ||
                        device.name.toString().toLowerCase().contains(regex = "^appa.".toRegex()) ) {
                    currentCompatibleSensor = device
                    sensorCounter++
                }
            }

//            if (sensorCounter == 1) {
//
//                mDisposable.add(mRxBluetoothClient.observeConnectDevice(currentCompatibleSensor, currentCompatibleSensor!!.uuids[0].uuid)
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribeOn(Schedulers.computation())
//                        .subscribe { event ->
//                            if (event.isConnected) {
//                                val intent = Intent(this, MainActivity::class.java)
//                                startActivity(intent)
//                                finish()
//                            }
//                        })
//
//            } else {
//                info("There is multiple IOIO Paired sensors, please choose one")
//            }

        } else {
            info("No bounded devices")
        }

    }

    private fun onAddClick(view: View, name: String, drawableId: Int, compatible: Boolean, bounded: Boolean) {
        if (compatible) {
            val newMenu = CircleImageView(this)
            val currentDrawable = ContextCompat.getDrawable(this, drawableId)
            val willBeWhite = currentDrawable!!.constantState.newDrawable()

            if (bounded) {
                newMenu.setBackgroundResource(R.drawable.circle_menu_shape_item_bounded)
            } else {
                newMenu.setBackgroundResource(R.drawable.circle_menu_shape_item)
            }

            willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
            newMenu.setPadding(20, 20, 20, 20)
            newMenu.setImageDrawable(willBeWhite)
            newMenu.name = name
            //newMenu.setBackgroundResource(R.color.colorPrimary)
            circle_layout.addView(newMenu)
        }
    }


    private fun onRemoveClick(view: View) {
        if (circle_layout.childCount > 0) {
            circle_layout.removeViewAt(circle_layout.childCount - 1)
        }
    }


    private fun checkFineLocationPermission(request: PermissionRequest) {
        request.detachAllListeners()
        request.send()
        request.listeners {

            onAccepted {
                mIsLocationPermissionGranted = true
                Toasty.success(applicationContext, "ACCESS_FINE_LOCATION granted.", Toast.LENGTH_SHORT, true).show()
//                checkBlueToothState()
            }

            onDenied {
                mIsLocationPermissionGranted = false
                Toasty.warning(applicationContext, "ACCESS_FINE_LOCATION denied.", Toast.LENGTH_SHORT, true).show()
            }

            onPermanentlyDenied {
                mIsLocationPermissionGranted = false
                Toasty.error(applicationContext, "ACCESS_FINE_LOCATION permission permanently denied, please grant it manually, Apolline will close in 10 seconds", Toast.LENGTH_LONG, true).show()
                object : CountDownTimer(10000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {

                    }

                    override fun onFinish() {
                        finish()
                    }
                }.start()
            }

            onShouldShowRationale { _, _ ->
                mIsLocationPermissionGranted = false
                alert("Apolline will not work, please grant ACCESS_FINE_LOCATION permission.", "Request location permission") {
                    yesButton {
                        checkFineLocationPermission(mRequestLocationPermission)
                    }
                    noButton {
                        request.detachAllListeners()
                    }

                }.show()

            }
        }
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
        private const val REQUEST_CODE_ENABLE_BLUETOOTH = 101
    }

    object Id {
        @IdRes
        val alert_new_sensor = View.generateViewId()
    }


}
