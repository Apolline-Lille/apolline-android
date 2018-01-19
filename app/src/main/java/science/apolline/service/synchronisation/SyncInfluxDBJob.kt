package science.apolline.service.synchronisation

import android.content.Context
import android.util.Log

import com.birbit.android.jobqueue.CancelReason
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint

import org.greenrobot.eventbus.EventBus.TAG

/**
 * Created by sparow on 19/01/2018.
 */

// A job to send a tweet
class SyncInfluxDBJob(context: Context)// This job requires network connectivity,
// and should be persisted in case the application exits before job is completed.
    : Job(Params(PRIORITY).requireNetwork().persist()) {
    override fun onAdded() {
        // Job has been saved to disk.
        // This is a good place to dispatch a UI event to indicate the job will eventually run.
        // In this example, it would be good to update the UI with the newly posted tweet.
        Log.d(TAG, "onAdded: ")
    }

    @Throws(Throwable::class)
    override fun onRun() {
        // Job logic goes here. In this example, the network call to post to Twitter is done here.
        // All work done here should be synchronous, a job is removed from the queue once
        // onRun() finishes.
        Log.d(TAG, "onRun: ")

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
        Log.d(TAG, "onCancel: ")
    }

    companion object {
        private const val PRIORITY = 1
    }
}