package science.apolline.service.synchronisation

import com.birbit.android.jobqueue.*

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import science.apolline.service.database.AppDatabase
import science.apolline.service.database.SensorDao
import science.apolline.service.networks.ApiUtils
import science.apolline.utils.RequestParser
import science.apolline.BuildConfig
import science.apolline.models.InfluxBody

/**
 * Created by sparow on 19/01/2018.
 */

class SyncInfluxDBJob : Job(Params(PRIORITY).requireNetwork().persist()), AnkoLogger {

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
                val nbUnSyncked: Int = sensorModel.getSensorNotSyncCount()

                info("number of unsyncked is : $nbUnSyncked")

                var attempt: Int = nbUnSyncked/MAXLENGH

                if (nbUnSyncked % MAXLENGH != 0) {
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
        private const val MAXLENGH = 80000 //Hardcoded in SensorDao
    }
}