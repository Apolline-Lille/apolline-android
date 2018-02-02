package science.apolline.service.synchronisation

import com.birbit.android.jobqueue.*
import org.jetbrains.anko.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import science.apolline.service.database.AppDatabase
import science.apolline.service.database.SensorDao
import science.apolline.service.networks.ApiUtils
import science.apolline.utils.RequestParser
import science.apolline.BuildConfig
import science.apolline.models.InfluxBody
import java.util.concurrent.TimeUnit

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


        if (super.getApplicationContext() != null) {

                sensorModel = AppDatabase.getInstance(super.getApplicationContext()).sensorDao()
                var nbUnSynced: Int = sensorModel.getSensorNotSyncCount()

                info("number of initial unsyncked is : $nbUnSynced")

                var attempt: Int = nbUnSynced/MAXLENGH

                if (nbUnSynced % MAXLENGH != 0) {
                    attempt++
                }

                info("Attempts " + attempt)

                for (i in 1..attempt) {

                    val dataNotSync = sensorModel.getUnSync()

                    if (dataNotSync.isNotEmpty()) {
                        val dataToSend = RequestParser.createRequestBody(dataNotSync)
                        info(dataToSend)
                        val call = api.savePost(BuildConfig.INFLUXDB_DBNAME, BuildConfig.INFLUXDB_USR, BuildConfig.INFLUXDB_PWD, dataToSend)

                        call.enqueue(object : Callback<InfluxBody> {

                                override fun onResponse(call: Call<InfluxBody>?, response: Response<InfluxBody>?) {

                                if (response != null && response.isSuccessful) {

                                    info("response success" + response)

                                    doAsync {

                                        dataNotSync.forEach{
                                            it.isSync = 1
                                            sensorModel.update(it)
                                        }

                                        // sensorModel.update(*dataNotSync.toTypedArray())

                                        uiThread {

                                            nbUnSynced -= MAXLENGH
                                            info("number of pending unsyncked is : $nbUnSynced")

                                            applicationContext.longToast("Synced")
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
        // An error occurred in onRun.
        // Return value determines whether this job should retry or cancel. You can further
        // specify a backoff strategy or change the job's priority. You can also apply the
        // delay to the whole group to preserve jobs' running order.
        return RetryConstraint.createExponentialBackoff(runCount, 1000)
    }

    override fun onCancel(@CancelReason cancelReason: Int, throwable: Throwable?) {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
        info("onCancel: ")
    }

    companion object {
        private const val PRIORITY = 1
        private const val MAXLENGH = 8000 //Hardcoded in SensorDao
    }
}