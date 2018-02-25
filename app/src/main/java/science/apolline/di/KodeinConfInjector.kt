package science.apolline.di

import android.Manifest
import android.content.Context
import android.net.wifi.WifiManager.WifiLock
import android.os.PowerManager.WakeLock
import android.support.v7.app.AlertDialog
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.config.Configuration
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidActivityScope
import science.apolline.BuildConfig
import science.apolline.service.database.AppDatabase
import science.apolline.service.database.SensorDao
import science.apolline.service.networks.ApiService
import science.apolline.service.networks.ApiUtils
import science.apolline.service.synchronisation.SyncJobService
import science.apolline.utils.CheckUtility.requestPartialWakeUp
import science.apolline.utils.CheckUtility.requestWifiFullMode
import science.apolline.view.Fragment.IOIOFragment

/**
 * Created by sparow on 2/25/2018.
 */

class KodeinConfInjector(context: Context) {

    val kodein = Kodein {

        //import(autoAndroidModule(this@ApollineApplication))

        val database: AppDatabase = AppDatabase.getInstance(context)
        bind<SensorDao>() with singleton { database.sensorDao() }


        ApiUtils.setUrl(BuildConfig.INFLUXDB_URL)
        bind<ApiService>() with singleton { ApiUtils.apiService }


        bind<JobManager>() with singleton {
            val builder = Configuration.Builder(context)
            JobManager(builder.build())
            SyncJobService().jobManager
        }

        bind<IOIOFragment>() with provider { IOIOFragment() }

        bind<WakeLock>() with scopedSingleton(androidActivityScope) {
            requestPartialWakeUp(it.applicationContext, REQUEST_WAKE_UP_TIMEOUT )
        }


        bind<WifiLock>() with scopedSingleton(androidActivityScope) {
            requestWifiFullMode(it.applicationContext)
        }

    }


    companion object {
        private const val REQUEST_WAKE_UP_TIMEOUT: Long = 86400 // 24h
    }
}