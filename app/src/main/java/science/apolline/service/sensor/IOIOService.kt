package science.apolline.service.sensor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import ioio.lib.api.AnalogInput
import ioio.lib.api.DigitalOutput
import ioio.lib.api.Uart
import ioio.lib.api.exception.ConnectionLostException
import ioio.lib.util.BaseIOIOLooper
import ioio.lib.util.IOIOLooper
import science.apolline.R
import science.apolline.models.IOIOData
import java.io.IOException
import java.io.InputStream

class IOIOService : ioio.lib.util.android.IOIOService() {

    internal var led = true


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
                //Log.e("ioioService","loop");
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
                sendBroadcast(data)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(this.javaClass.name, "onDestroy")
    }

    private fun sendBroadcast(data: IOIOData) {

        val intent = Intent(getString(R.string.sensorBroadCast))
        intent.putExtra(getString(R.string.serviceBroadCastDataSet), data)
        val d1 = System.currentTimeMillis() * 1000000
        intent.putExtra(getString(R.string.serviceBroadCastDate), d1)
        val sensorName = getString(R.string.loa_ioio_name)
        intent.putExtra(getString(R.string.serviceBroadCastSensorName), sensorName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

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
