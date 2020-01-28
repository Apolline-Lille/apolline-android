package science.apolline.service.synchronisation

import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.widget.Toast
import com.birbit.android.jobqueue.*
import es.dmoral.toasty.Toasty
import org.jetbrains.anko.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import science.apolline.utils.RequestParser
import science.apolline.BuildConfig
import science.apolline.models.InfluxBody
import science.apolline.models.TimestampSync
import science.apolline.service.database.AppDatabase
import science.apolline.service.database.SensorDao
import science.apolline.service.database.TimestampSyncDao
import science.apolline.service.networks.ApiUtils
import science.apolline.utils.CheckUtility
import science.apolline.utils.CheckUtility.isNetworkConnected

/**
 * Created by sparow on 19/01/2018.
 */

class SyncInfluxDBJob : Job(Params(PRIORITY)
        .requireNetwork()
        .groupBy("influxDBGroupID")
        .singleInstanceBy("influxDBJobId")
        .persist()), AnkoLogger {

    private lateinit var sensorModel: SensorDao
    private lateinit var timestampModel: TimestampSyncDao
    private lateinit var mPrefs: SharedPreferences
    private var TO_MILLISECONDS: Int = 1000000

    private val SYNC_MODE_DENIED: Int = 0 //NO SYNCHRO
    private val SYNC_MODE_4G: Int = 1 //DATA ONLY (3g,4g,5g)
    private val SYNC_MODE_WIFI = 2 // Wi-Fi only

    override fun onAdded() {
        info("onAdded: ")
    }

    @Throws(Throwable::class)
    override fun onRun() {


        // Preferences.
        mPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        //Retrive user choice, if not defined, DATA (3g,4g,5g) will be used by default
        var SYNC_MODE_USER = (mPrefs.getString("sync_type", SYNC_MODE_4G.toString())).toInt()

        info("SYNC_MODE_USER: $SYNC_MODE_USER")

        when (SYNC_MODE_USER) {
            SYNC_MODE_DENIED -> {
                info("User denied sync job")
            }
            SYNC_MODE_4G -> {
                syncData()
            }
            SYNC_MODE_WIFI -> {
                if (CheckUtility.isWifiNetworkConnected(applicationContext))
                    syncData()
                else
                    info("No Wi-Fi connection detected, sync job denied")
            }
        }
        info("onRun: ")
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int,
                                        maxRunCount: Int): RetryConstraint {
        return RetryConstraint.createExponentialBackoff(runCount, 1000)
    }

    override fun onCancel(@CancelReason cancelReason: Int, throwable: Throwable?) {
        info("onCancel: ")
    }


    private fun syncData() {

        ApiUtils.setUrl(BuildConfig.INFLUXDB_URL)
        val api = ApiUtils.apiService

        if (super.getApplicationContext() != null && isNetworkConnected(super.getApplicationContext())) {
            sensorModel = AppDatabase.getInstance(super.getApplicationContext()).sensorDao()
            timestampModel = AppDatabase.getInstance(super.getApplicationContext()).timestampSyncDao()

            var t = TimestampSync(System.currentTimeMillis())

            val lastSyncDate: Long = timestampModel.getLastSync()
            var nbUnSynced: Long = sensorModel.getSensorNotSyncCountByDate(t.date  * TO_MILLISECONDS, lastSyncDate  * TO_MILLISECONDS)

            info("actualDate ${t.date}")
            info("last sync = $lastSyncDate")
            info("number of initial unsynced is : $nbUnSynced")

            var attempt: Long = nbUnSynced / MAX_LENGTH
            if (nbUnSynced % MAX_LENGTH != 0L)
                attempt++
            info("Attempts $attempt")

            for (i in 1..attempt) {
                //val dataNotSync = sensorModel.getUnSync(MAX_LENGTH)
                val dataNotSync = sensorModel.getUnSyncByDate(t.date  * TO_MILLISECONDS ,lastSyncDate * TO_MILLISECONDS ,MAX_LENGTH)

                if (dataNotSync.isNotEmpty()) {
                    info("UnSync to sync is :" + dataNotSync.size)

                    val dataToSend = RequestParser.createRequestBody(dataNotSync)
                    info(dataToSend)
                    val call = api.savePost(BuildConfig.INFLUXDB_DBNAME, BuildConfig.INFLUXDB_USR, BuildConfig.INFLUXDB_PWD, dataToSend)

                    call.enqueue(object : Callback<InfluxBody> {
                        override fun onResponse(call: Call<InfluxBody>?, response: Response<InfluxBody>?) {
                            if (response != null && response.isSuccessful) {
                                info("response success$response")
                                doAsync {

                                    uiThread {
                                        doAsync {
                                            sensorModel.update(*dataNotSync.toTypedArray())
                                            uiThread {
                                                nbUnSynced -= MAX_LENGTH
                                                info("number of pending unsyncked is : $nbUnSynced")
                                                Toasty.success(applicationContext, "Synchronized !", Toast.LENGTH_LONG, true).show()
                                            }
                                        }
                                    }
                                    timestampModel.insert(t)
                                }
                            } else {
                                info("response failed $response")
                            }
                        }

                        override fun onFailure(call: Call<InfluxBody>?, t: Throwable?) {

                        }
                    })
                }
            }
        }
    }

    companion object {
        private const val PRIORITY = 1
        private const val MAX_LENGTH = 8000L //Hardcoded in SensorDao
    }
}