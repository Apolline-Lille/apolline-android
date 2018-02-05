package science.apolline

import android.app.Application
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService
import android.os.Build
import android.util.Log
import com.birbit.android.jobqueue.config.Configuration
import com.birbit.android.jobqueue.log.CustomLogger
import science.apolline.service.synchronisation.SyncJobManager
import science.apolline.service.synchronisation.SyncJobService


/**
 * Created by sparow on 05/02/18.
 */


class ApollineApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SyncJobManager.getJobManager(this)
    }
}


