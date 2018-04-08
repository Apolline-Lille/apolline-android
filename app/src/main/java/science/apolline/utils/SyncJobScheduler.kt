package science.apolline.utils

import android.content.Context
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import science.apolline.service.synchronisation.SyncInfluxDBJob
import science.apolline.service.synchronisation.SyncJobManager.jobManager
import science.apolline.utils.CheckUtility.isWifiNetworkConnected
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


/**
 * Created by sparow on 05/02/18.
 */
object SyncJobScheduler : AnkoLogger {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private lateinit var executor: ScheduledFuture<*>

    fun setAutoSync(mode: Int, time: Long, context: Context) {
        if (time > 0L && mode != 0) {
            executor = scheduler.scheduleAtFixedRate(
                    { syncTask(context) },
                    1,
                    time,
                    TimeUnit.MINUTES)
            info("setAutoSync: " + executor.isDone)
        }else{
            cancelAutoSync(true)
            info("cancelAutoSync disabled by user")
        }
    }

    fun cancelAutoSync(interrupt: Boolean): Boolean {
        if (!(executor.isDone || executor.isCancelled)) {
            executor.cancel(interrupt)
            return true
        }
        info("cancelAutoSync: " + executor.isCancelled)
        return false
    }

    private fun syncTask(context: Context) {
        if (CheckUtility.isNetworkConnected(context) && isWifiNetworkConnected(context)) {
            jobManager!!.addJobInBackground(SyncInfluxDBJob())
            info("Auto synchronisation...")
        } else {
            jobManager!!.addJobInBackground(SyncInfluxDBJob())
            info("No internet connection ! Job added to queue")
        }
    }
}