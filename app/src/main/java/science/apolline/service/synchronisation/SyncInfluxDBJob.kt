package science.apolline.service.synchronisation

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
import science.apolline.service.database.AppDatabase
import science.apolline.service.database.SensorDao
import science.apolline.service.networks.ApiUtils
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


    override fun onAdded() {
        info("onAdded: ")
    }

    @Throws(Throwable::class)
    override fun onRun() {

        ApiUtils.setUrl(BuildConfig.INFLUXDB_URL)
        val api = ApiUtils.apiService


        if (super.getApplicationContext() != null && isNetworkConnected(super.getApplicationContext())) {

            sensorModel = AppDatabase.getInstance(super.getApplicationContext()).sensorDao()

            var nbUnSynced: Long = sensorModel.getSensorNotSyncCount()

            info("number of initial unsyncked is : $nbUnSynced")

            var attempt: Long = nbUnSynced / MAX_LENGTH

            if (nbUnSynced % MAX_LENGTH != 0L) {
                attempt++
            }

            info("Attempts " + attempt)

            for (i in 1..attempt) {

                val dataNotSync = sensorModel.getUnSync(MAX_LENGTH)

                if (dataNotSync.isNotEmpty()) {
                    info("UnSync to sync is :" + dataNotSync.size)

                    val dataToSend = RequestParser.createRequestBody(dataNotSync)
                    info(dataToSend)
                    val call = api.savePost(BuildConfig.INFLUXDB_DBNAME, BuildConfig.INFLUXDB_USR, BuildConfig.INFLUXDB_PWD, dataToSend)

                    call.enqueue(object : Callback<InfluxBody> {

                        override fun onResponse(call: Call<InfluxBody>?, response: Response<InfluxBody>?) {

                            if (response != null && response.isSuccessful) {

                                info("response success" + response)

                                doAsync {

                                    dataNotSync.forEach {
                                        it.isSync = 1
                                        //sensorModel.update(it)
                                    }
//                                            uiThread {
//                                                nbUnSynced -= MAX_LENGTH
//                                                info("number of pending unsyncked is : $nbUnSynced")
//                                                applicationContext.longToast("Synced")
//                                            }
//
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
                                }

                            } else {
                                info("response failed " + response)
                            }
                        }

                        override fun onFailure(call: Call<InfluxBody>?, t: Throwable?) {

                        }
                    })
                }
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

    companion object {
        private const val PRIORITY = 1
        private const val MAX_LENGTH = 8000L //Hardcoded in SensorDao
    }
}