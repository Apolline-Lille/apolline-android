package science.apolline.di

import android.content.Context
import android.content.SharedPreferences
import android.net.wifi.WifiManager.WifiLock
import android.os.PowerManager.WakeLock
import android.preference.PreferenceManager
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
import science.apolline.view.fragment.ChartFragment
import science.apolline.view.fragment.IOIOFragment
import science.apolline.view.fragment.MapFragment
import science.apolline.view.fragment.ViewPagerFragment

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


        bind<SharedPreferences>() with singleton { PreferenceManager.getDefaultSharedPreferences(context) }


        bind<JobManager>() with singleton {
            val builder = Configuration.Builder(context)
            JobManager(builder.build())
            SyncJobService().jobManager
        }

        bind<IOIOFragment>() with provider { IOIOFragment() }
        bind<ChartFragment>() with provider { ChartFragment() }
        bind<MapFragment>() with provider { MapFragment() }

        bind<ViewPagerFragment>() with provider {ViewPagerFragment() }


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