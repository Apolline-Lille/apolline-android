package science.apolline.service.synchronisation;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;

/**
 * Created by sparow on 05/02/18.
 */

public class SyncJobManager {

    private static JobManager mJobManager;

    public static synchronized JobManager getJobManager() {
        return mJobManager;
    }

    public static synchronized JobManager getJobManager(Context context) {
        if (mJobManager == null) {
            configureJobManager(context);
        }
        return mJobManager;
    }

    private static synchronized void configureJobManager(Context context) {
        if (mJobManager == null) {
            Configuration.Builder builder = new Configuration.Builder(context)
                    .minConsumerCount(1) // always keep at least one consumer alive
                    .maxConsumerCount(3) // up to 3 consumers at a time
                    .loadFactor(3) // 3 jobs per consumer
                    .consumerKeepAlive(120) // wait 2 minute
                    .customLogger(new CustomLogger() {
                        private static final String TAG = "JOBS";
                        @Override
                        public boolean isDebugEnabled() {
                            return true;
                        }

                        @Override
                        public void d(String text, Object... args) {
                            Log.d(TAG, String.format(text, args));
                        }

                        @Override
                        public void e(Throwable t, String text, Object... args) {
                            Log.e(TAG, String.format(text, args), t);
                        }

                        @Override
                        public void e(String text, Object... args) {
                            Log.e(TAG, String.format(text, args));
                        }

                        @Override
                        public void v(String text, Object... args) {

                        }
                    });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(context,
                        SyncJobService.class), true);
            }
            mJobManager = new JobManager(builder.build());
        }
    }
}