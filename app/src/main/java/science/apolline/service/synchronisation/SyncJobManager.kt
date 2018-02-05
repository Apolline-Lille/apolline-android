@file:Suppress("INACCESSIBLE_TYPE")

package science.apolline.service.synchronisation

import android.content.Context
import android.os.Build
import android.util.Log

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.config.Configuration
import com.birbit.android.jobqueue.log.CustomLogger
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService

/**
 * Created by sparow on 05/02/18.
 */

object SyncJobManager {

    @get:Synchronized
    var jobManager: JobManager? = null
        private set

    @Synchronized
    fun getJobManager(context: Context): JobManager? {
        if (jobManager == null) {
            configureJobManager(context)
        }
        return jobManager
    }

    @Synchronized
    private fun configureJobManager(context: Context) {
        if (jobManager == null) {
            val builder = Configuration.Builder(context)
                    .minConsumerCount(1) // always keep at least one consumer alive
                    .maxConsumerCount(1) // up to 3 consumers at a time
                    .loadFactor(1) // 3 jobs per consumer
                    .consumerKeepAlive(120) // wait 2 minute
                    .customLogger(object : CustomLogger {
                        val TAG = "JOBS"
                        override fun isDebugEnabled(): Boolean {
                            return true
                        }

                        override fun d(text: String, vararg args: Any) {
                            Log.d(TAG, String.format(text, *args))
                        }

                        override fun e(t: Throwable, text: String, vararg args: Any) {
                            Log.e(TAG, String.format(text, *args), t)
                        }

                        override fun e(text: String, vararg args: Any) {
                            Log.e(TAG, String.format(text, *args))
                        }

                        override fun v(text: String, vararg args: Any) {

                        }
                    })

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(context,
                        SyncJobService::class.java), true)
            }
            jobManager = JobManager(builder.build())
        }
    }
}