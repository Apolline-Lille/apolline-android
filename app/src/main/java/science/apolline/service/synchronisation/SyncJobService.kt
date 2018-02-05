package science.apolline.service.synchronisation

import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService
import science.apolline.ApollineApplication

/**
 * Created by sparow on 05/02/18.
 */

class SyncJobService: FrameworkJobSchedulerService() {
    public override fun getJobManager(): JobManager{
        return SyncJobManager.jobManager!!
    }
}