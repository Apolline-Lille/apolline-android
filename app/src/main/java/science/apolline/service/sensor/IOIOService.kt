package science.apolline.service.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.google.android.gms.location.LocationRequest
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import ioio.lib.api.AnalogInput
import ioio.lib.api.DigitalOutput
import ioio.lib.api.Uart
import ioio.lib.api.exception.ConnectionLostException
import ioio.lib.util.BaseIOIOLooper
import ioio.lib.util.IOIOLooper
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import science.apolline.R
import science.apolline.models.Device
import science.apolline.models.IOIOData
import science.apolline.models.Position
import science.apolline.service.database.SensorDao
import science.apolline.utils.CheckUtility
import science.apolline.utils.DetectedActivityToString
import java.io.IOException
import java.io.InputStream
import science.apolline.utils.GeoHashHelper
import science.apolline.utils.ToMostProbableActivity
import science.apolline.view.activity.MainActivity


class IOIOService : ioio.lib.util.android.IOIOService(), AnkoLogger {

    private val injector: KodeinInjector = KodeinInjector()
    private val sensorModel by injector.instance<SensorDao>()
    private val locationProvider = ReactiveLocationProvider(this)
    private val disposable = CompositeDisposable()
    private var location: Location? = null
    internal var led = true

    private val mPrefs by injector.instance<SharedPreferences>()

    private var DEVICE_NAME = "Apolline00"
    private var DEVICE_UUID = "ffffffff-ffff-ffff-ffff-ffffffffffff"
    private var COLLECT_DATA_FREQ: Int = 1
    private var TO_MILLISECONDS: Int = 1000


    override fun createIOIOLooper(): IOIOLooper {
        injector.inject(appKodein())

        DEVICE_NAME = mPrefs.getString("device_name", "Apolline00")
        DEVICE_UUID = mPrefs.getString("device_uuid", "ffffffff-ffff-ffff-ffff-ffffffffffff")

        return object : BaseIOIOLooper() {
            private val data = IOIOData()
            private var led: DigitalOutput? = null
            private var uart: Uart? = null
            private var inputStream: InputStream? = null
            private var inputTemp: AnalogInput? = null
            private var inputHum: AnalogInput? = null
            private val request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(5)
                    .setInterval(750)
            private var position = Position()

            @Throws(ConnectionLostException::class, InterruptedException::class)
            override fun setup() {
                led = ioio_.openDigitalOutput(0, true)
                uart = ioio_.openUart(7, 6, 9600, Uart.Parity.NONE, Uart.StopBits.ONE)
                inputStream = uart!!.inputStream
                inputTemp = ioio_.openAnalogInput(44)
                inputHum = ioio_.openAnalogInput(42)
                launchForegroundServiceNotification(applicationContext)
            }


            @Throws(ConnectionLostException::class, InterruptedException::class)
            override fun loop() {
                COLLECT_DATA_FREQ = (mPrefs.getString("collect_data_frequency", "1")).toInt()

                try {
                    if (CheckUtility.checkFineLocationPermission(applicationContext) && CheckUtility.canGetLocation(applicationContext)) {
                        disposable.add(

                                Observable.zip(
                                        locationProvider.getUpdatedLocation(request).subscribeOn(Schedulers.io()),
                                        locationProvider.getDetectedActivity(0)
                                                .map(ToMostProbableActivity())
                                                .map(DetectedActivityToString())
                                                .subscribeOn(Schedulers.io()),
                                        BiFunction<Location, String, Pair<Location, String>> { currentLocation, currentActivity ->
                                            Pair(currentLocation, currentActivity)
                                        }

                                )
                                        .onExceptionResumeNext(Observable.empty())
                                        .onErrorReturn {
                                            error("Error location pair not found $it")
                                        }
                                        .observeOn(Schedulers.io())
                                        .subscribe { t ->
                                            if (t == null)
                                                info("Get location error")
                                            else
                                                position = Position(t.first.provider, GeoHashHelper.encode(t.first.latitude, t.first.longitude), t.second)
                                        }
                        )

                    } else
                        position = Position()

                    val dataAvailability = this.inputStream!!.available()
                    if (dataAvailability > 0) {
                        val buffer = ByteArray(dataAvailability)
                        this.inputStream!!.read(buffer)
                        for (b in buffer)
                            if (b.compareTo(0x42) == 0)
                                data.count = 0
                            else {
                                data.setBuff(data.count, b.toInt().and(0xff))
                                data.count = data.count + 1
                                if (data.count == data.LENG.toInt()) {
                                    data.count = 0
                                    if (data.checkValue()) {
                                        this@IOIOService.led = !this@IOIOService.led
                                        led!!.write(this@IOIOService.led)

                                        //parse PM value
                                        data.parse()

                                        val tensionTemp = inputTemp!!.voltage
                                        data.tempKelvin = tensionTemp * 100

                                        val tensionHum = inputHum!!.voltage
                                        val RH = ((tensionHum / 3.3 - 0.1515) / 0.0636).toFloat() //pour avoir l'humidité à 25°C
                                        val RHT = RH / (1.0546 - 0.00216 * (tensionTemp * 100 - 273.15)) * 10 //compensé en t°
                                        data.rh = RH
                                        data.rht = RHT
                                    }
                                }
                            }
                    }
                } catch (e: IOException) {
                    setServiceStatus(false)
                    error("Unable to start IOIOService: " + e.printStackTrace())
                }
                Thread.sleep((COLLECT_DATA_FREQ * TO_MILLISECONDS).toLong())
                info("Position Hash :" + position.geohash)
                persistData(data, position)
                setServiceStatus(true)
            }
        }
    }

    private fun persistData(data: IOIOData, pos: Position?) {
        val d1 = System.currentTimeMillis() * 1000000
        val device = Device(DEVICE_UUID, DEVICE_NAME, d1, pos, data.toJson(),0)
        doAsync {
            sensorModel.insert(device)
            location = null
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onUnbind(intent: Intent?): Boolean {
        if (!disposable.isDisposed)
            disposable.clear()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!disposable.isDisposed)
            disposable.dispose()
        setServiceStatus(false)
        info("onDestroy")
    }

    fun launchForegroundServiceNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val n: Notification
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            } else {
                null
            }
            if (channel != null) {
                channel.description = "Apolline notification channel"
                notificationManager.createNotificationChannel(channel)
            }
        }

        n = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentTitle("IOIO service is running")
                .setTicker("IOIO service is running")
                .setContentText("collecting particles measurements")
                .setSmallIcon(R.drawable.logo_apolline)
                .setLargeIcon(BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.logo_apolline))
                .setContentIntent(pendingIntent)
                .build()

        n.flags = n.flags or (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)

        startForeground(SERVICE_ID, n)
    }

    companion object {

        private var mServiceStatus: Boolean = false

        fun getServiceStatus(): Boolean {
            return mServiceStatus
        }

        fun setServiceStatus(status: Boolean) {
            mServiceStatus = status
        }

        private const val SERVICE_ID: Int = 101
        private const val CHANNEL_ID = "science.apolline"
        private const val CHANNEL_NAME = "Apolline"
    }
}
