package science.apolline.service.sensor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log
import ioio.lib.api.AnalogInput
import ioio.lib.api.DigitalOutput
import ioio.lib.api.Uart
import ioio.lib.api.exception.ConnectionLostException
import ioio.lib.util.BaseIOIOLooper
import ioio.lib.util.IOIOLooper
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import science.apolline.models.Device
import science.apolline.models.IOIOData
import science.apolline.models.Position
import science.apolline.service.database.AppDatabase
import science.apolline.service.database.SensorDao
import science.apolline.utils.AndroidUuid
import science.apolline.utils.CheckPermission
import java.io.IOException
import java.io.InputStream


class IOIOService : ioio.lib.util.android.IOIOService(), AnkoLogger {

    internal var led = true
    private val sensorModel: SensorDao = AppDatabase.getInstance(this).sensorDao()
    private val locationProvider = ReactiveLocationProvider(this)

    override fun createIOIOLooper(): IOIOLooper {
        return object : BaseIOIOLooper() {
            private val data = IOIOData()
            private var led_: DigitalOutput? = null
            private var uart_: Uart? = null
            private var uartIn_: InputStream? = null
            private var inputTemp: AnalogInput? = null
            private var inputHum: AnalogInput? = null
            private val freq = 1000


            @Throws(ConnectionLostException::class, InterruptedException::class)
            override fun setup() {
                led_ = ioio_.openDigitalOutput(0, true)
                uart_ = ioio_.openUart(7, 6, 9600, Uart.Parity.NONE, Uart.StopBits.ONE)
                uartIn_ = uart_!!.inputStream
                inputTemp = ioio_.openAnalogInput(44)
                inputHum = ioio_.openAnalogInput(42)
                initChannels(applicationContext)
                val notification = NotificationCompat.Builder(applicationContext, "default")
                        .setContentTitle("IOIO service is running")
                        .setTicker("IOIO service is running")
                        .setContentText("collect of air quality is running")
                        .setOngoing(true)
                        .build()
                startForeground(101, notification)
            }

            @Throws(ConnectionLostException::class, InterruptedException::class)
            override fun loop() {
                Log.e("ioioService","loop");
                try {
                    val availableCount = this.uartIn_!!.available()
                    if (availableCount > 0) {
                        val buffer = ByteArray(availableCount)
                        this.uartIn_!!.read(buffer)
                        for (b in buffer) {
                            if (b.compareTo(0x42) == 0) {
                                data.count = 0
                            } else {
                                data.setBuff(data.count, b.toInt().and(0xff))
                                data.count = data.count + 1
                                if (data.count == data.LENG.toInt()) {
                                    data.count = 0
                                    if (data.checkValue()) {
                                        led = !led
                                        led_!!.write(led)

                                        //parse PM value
                                        data.parse()

                                        //Température
                                        val tensionTemp = inputTemp!!.voltage
                                        data.tempKelvin = tensionTemp * 100

                                        //humidity
                                        val tensionHum = inputHum!!.voltage
                                        val RH = ((tensionHum / 3.3 - 0.1515) / 0.0636).toFloat() //pour avoir l'humidité à 25°C
                                        val RHT = RH / (1.0546 - 0.00216 * (tensionTemp * 100 - 273.15)) * 10 //compensé en t°
                                        data.rh = RH
                                        data.rht = RHT


                                    }
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }


                Thread.sleep(freq.toLong())
                persistData(data)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(this.javaClass.name, "onDestroy")
    }

    private fun persistData(data: IOIOData) {
        val d1 = System.currentTimeMillis() * 1000000

        var position: Position? = null
        val device = Device(AndroidUuid.getAndroidUuid(), "LOA", d1, position, data.toJson(),0)

        doAsync {
            sensorModel.insertOne(device)
        }

        if (CheckPermission.checkCoarseLocationPermission(this)) {
            val location = locationProvider.lastKnownLocation.blockingLast()
            position = Position(location.provider, location.longitude, location.latitude, "")
            device.position = position
            doAsync {
                sensorModel.update(device)
            }

        }


    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "Channel description"
        notificationManager.createNotificationChannel(channel)
    }
}
