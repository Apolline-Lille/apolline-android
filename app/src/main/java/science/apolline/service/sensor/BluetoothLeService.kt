package science.apolline.service.sensor

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

import android.graphics.Color

import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.*


import android.util.Log

import android.widget.Toast


import science.apolline.models.APPAData
import science.apolline.utils.SampleGattAttributes


import science.apolline.view.activity.MainActivity

import android.support.v4.app.ActivityCompat.checkSelfPermission


import kotlinx.android.synthetic.main.fragment_ioio_content.*

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar

import java.util.UUID

import science.apolline.R


/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
class BluetoothLeService : Service() {

    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    internal var mBluetoothGatt: BluetoothGatt? = null

    internal var buff = ""

    internal var color: Int = 0
    internal var col: Int = 0



    internal var appaData = APPAData()



    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val mGattCallback: BluetoothGattCallback

    private val mBinder = LocalBinder()

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     *
     * @return A `List` of supported services.
     */
    val supportedGattServices: List<BluetoothGattService>?
        get() = if (mBluetoothGatt == null) null else mBluetoothGatt!!.services

    init {
        mGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                val intentAction: String
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED
                    broadcastUpdate(intentAction)
                    Log.i(TAG, "Connected to GATT server.")
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt!!.discoverServices())

                    val UIHandl = Handler(Looper.getMainLooper())
                    UIHandl.postDelayed({
                        //Toast.makeText(this@BluetoothLeService, "Start", Toast.LENGTH_SHORT).show()
                        if (MainActivity.mNotifyCharacteristic != null) {
                            MainActivity.mNotifyCharacteristic!!.setValue("c")
                            MainActivity.mBluetoothLeService!!.writeCharacteristic(MainActivity.mNotifyCharacteristic!!)
                        }
                    }, 5000)

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED
                    Log.i(TAG, "Disconnected from GATT server.")
                    broadcastUpdate(intentAction)
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)

                } else {
                    Log.w(TAG, "onServicesDiscovered received: $status")
                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)

                }
            }

            @SuppressLint("SetTextI18n")
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {


                val tempBuff = characteristic.getStringValue(0)
                buff += tempBuff





                if (buff.contains("\n")) {
                    if (!buff.contains("#") && buff.indexOf("2") == 0) {
                        //Decode rawdata
                        //date
                        appaData.dateGPS = buff.substring(0, buff.indexOf(";"))
                        //PM1
                        var debut = buff.indexOf(";")
                        var fin = buff.indexOf(";", debut + 1)
                        var Value = buff.substring(debut + 1, fin)
                        try {
                            appaData.pm01Value = Integer.parseInt(Value)
                        } catch (e: NumberFormatException) {
                            appaData.pm01Value = 0
                        }

                        //PM2.5
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        Value = buff.substring(debut + 1, fin)
                        try {
                            appaData.pm2_5Value = Integer.parseInt(Value)
                        } catch (e: NumberFormatException) {
                            appaData.pm2_5Value = 0
                        }

                        //PM10
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        Value = buff.substring(debut + 1, fin)
                        try {
                            appaData.pm10Value = Integer.parseInt(Value)
                        } catch (e: NumberFormatException) {
                            appaData.pm10Value = 0
                        }

                        if (Integer.parseInt(Value) <= 25) {
                            col = Integer.parseInt(Value) * 10
                            color = Color.rgb(col, 0xFF, 0)
                        } else if (Integer.parseInt(Value) <= 50) {
                            col = 0xFF - (Integer.parseInt(Value) - 25) * 10
                            color = Color.rgb(0xFF, col, 0)
                        } else
                            color = Color.rgb(0xFF, 0, 0)
                        //Position GPS
                        //Lat
                        for (i in 0..6) {
                            debut = fin
                            fin = buff.indexOf(";", debut + 1)
                        }
                        try {
                            LatGPS = java.lang.Double.parseDouble(buff.substring(debut + 1, fin))
                        } catch (e: NumberFormatException) {
                            LatGPS = 0.0
                        }

                        //Long
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        try {
                            LongGPS = java.lang.Double.parseDouble(buff.substring(debut + 1, fin))
                        } catch (e: NumberFormatException) {
                            LongGPS = 0.0
                        }

                        //Altitude
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        try {
                            appaData.AltiGPS = java.lang.Double.parseDouble(buff.substring(debut + 1, fin))
                        } catch (e: NumberFormatException) {
                            appaData.AltiGPS = 0.0
                        }

                        //Vitesse(km/h)
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        try {
                            appaData.SpeedGPS = java.lang.Double.parseDouble(buff.substring(debut + 1, fin))
                        } catch (e: NumberFormatException) {
                            appaData.SpeedGPS = 0.0
                        }

                        //nb satellites
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        try {
                            appaData.SatGPS = Integer.parseInt(buff.substring(debut + 1, fin))
                        } catch (e: NumberFormatException) {
                            appaData.SatGPS = 0
                        }

                        //T° Dps310
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        //Pression
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        try {
                            appaData.pression = java.lang.Double.parseDouble(buff.substring(debut + 1, fin)) / 100
                        } catch (e: NumberFormatException) {
                            appaData.pression = 0.0
                        }

                        //T° HDC1080
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        try {
                            appaData.tempe = java.lang.Double.parseDouble(buff.substring(debut + 1, fin))
                        } catch (e: NumberFormatException) {
                            appaData.tempe = 0.0
                        }

                        // Humidité
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        try {
                            appaData.humi = java.lang.Double.parseDouble(buff.substring(debut + 1, fin))
                        } catch (e: NumberFormatException) {
                            appaData.humi = 0.0
                        }

                        // batterie level
                        debut = fin
                        fin = buff.indexOf(";", debut + 1)
                        try {
                            appaData.bat_volt = java.lang.Double.parseDouble(buff.substring(debut + 1, fin))
                        } catch (e: NumberFormatException) {
                            appaData.bat_volt = 0.0
                        }

                    }
                    // generate Dates
                    val calendar = Calendar.getInstance()
                    val d1 = calendar.time

                    @SuppressLint("SimpleDateFormat") val DateFormat = SimpleDateFormat("yy_MM_dd_")
                    val date = DateFormat.format(d1)
                    val myFile = File(Environment.getExternalStorageDirectory().toString() +
                            File.separator + "data", date + "Dust_sensor_" + MainActivity.mDeviceName + ".csv")

                    val myDir = File(Environment.getExternalStorageDirectory().toString() +
                            File.separator + "data")
                    var success: Boolean? = true
                    if (!myDir.exists()) {
                        success = myDir.mkdir()
                    }

                    if (success!!) {
                        var data = ""
                        if (!myFile.exists()) {
                            data += "#Fichier des données brutes issues du capteur de poussières SEN0177\r\n#Format:\r\n#AA-MM-JJ hh:mm:ss;PM1.0;PM2.5;PM10(ug/m3);Above PM0.3;PM0.5;PM1;PM2.5;PM5;PM10(ug/m3);Latitude;Longitude;Altitude;vitesse(km/h);nb de satellites;Température(°C);Pression(Pascal);Température ambiante(°C);Humidité ambiante(%);Batterie(V);Température interne(°C);Humidité interne(%)\r\n"
                        }
                        data += buff

                        var output: FileOutputStream? = null
                        try {
                            output = FileOutputStream(myFile, true)
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }

                        try {
                            output?.write(data.toByteArray())
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        try {
                            output?.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                    } else {
                        Log.e("écriture fichier", "ERROR DE CREATION DE DOSSIER")
                    }

                    // récupérer info GPS android
                    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val criteria = Criteria()
                    if (checkSelfPermission(this@BluetoothLeService, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED && checkSelfPermission(this@BluetoothLeService, Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    var location: Location? = null
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false))
                    }
                    if (location != null) {
                        Lat = location.latitude
                        Long = location.longitude
                    }







                    var ioiofm : android.support.v4.app.Fragment? = MainActivity.mFragment
                    ioiofm!!.fragment_ioio_progress_pm1.stopSpinning()
                    ioiofm.fragment_ioio_progress_pm2_5.stopSpinning()
                    ioiofm.fragment_ioio_progress_pm10.stopSpinning()
                    ioiofm.fragment_ioio_progress_rht.stopSpinning()
                    ioiofm.fragment_ioio_progress_tmpk.stopSpinning()
                    ioiofm.fragment_ioio_progress_tmpc.stopSpinning()
                    ioiofm.fragment_ioio_progress_tmpk.stopSpinning()
                    ioiofm!!.fragment_ioio_progress_pm1.setValueAnimated(MainActivity.mBluetoothLeService!!.appaData.pm01Value.toFloat())
                    ioiofm.fragment_ioio_progress_pm2_5.setValueAnimated(MainActivity.mBluetoothLeService!!.appaData.pm2_5Value.toFloat())
                    ioiofm.fragment_ioio_progress_pm10.setValueAnimated(MainActivity.mBluetoothLeService!!.appaData.pm10Value.toFloat())
                    ioiofm.fragment_ioio_progress_rht.setValueAnimated(MainActivity.mBluetoothLeService!!.appaData.humi.toFloat())
                    ioiofm.fragment_ioio_progress_tmpk.setValueAnimated(MainActivity.mBluetoothLeService!!.appaData.tempKelvin)
                    ioiofm.fragment_ioio_progress_tmpc.setValueAnimated(MainActivity.mBluetoothLeService!!.appaData.tempe.toFloat())
                    ioiofm.fragment_ioio_progress_tmpk.setValueAnimated(MainActivity.mBluetoothLeService!!.appaData.tempe.toFloat()+273.15.toFloat())



                    if (appaData.bat_volt >=3.97) {//80-100
                        ioiofm!!.imageView3.setBackgroundResource(R.drawable.bat_0)
                    }
                    else if (appaData.bat_volt >=3.87) {//60-80
                        ioiofm!!.imageView3.setBackgroundResource(R.drawable.bat_1)
                    }
                    else if (appaData.bat_volt >=3.79) {//40-60
                        ioiofm!!.imageView3.setBackgroundResource(R.drawable.bat_2)
                    }
                    else if (appaData.bat_volt >=3.70) {//20-40
                        ioiofm!!.imageView3.setBackgroundResource(R.drawable.bat_3)
                    }
                    else{//0-20
                        ioiofm!!.imageView3.setBackgroundResource(R.drawable.bat_4)
                    }









                    buff = ""//reset buffer
                }
            }
        }
    }




    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        println("sending broadcast ... " + action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
        // writes the data formatted in HEX.
        val data = characteristic.value
        if (data != null && data.size > 0) {
            val stringBuilder = StringBuilder(data.size)
            for (byteChar in data)
                stringBuilder.append(String.format("%02X ", byteChar))
            intent.putExtra(EXTRA_DATA, String(data) + "\n" + stringBuilder.toString())
        }
        sendBroadcast(intent)
    }

    internal inner class LocalBinder : Binder() {

        fun getService(): BluetoothLeService {
            return this@BluetoothLeService
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
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

    companion object {
        private val TAG = BluetoothLeService::class.java.simpleName

        val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

        val UUID_DATA_DUST_SENSOR = UUID.fromString(SampleGattAttributes.DATA_DUST_SENSOR)

        var Lat: Double = 0.toDouble()
        var LatOld: Double = 0.toDouble()
        var LatGPS: Double = 0.toDouble()
        var Long: Double = 0.toDouble()
        var LongOld: Double = 0.toDouble()
        var LongGPS: Double = 0.toDouble()
    }



}
