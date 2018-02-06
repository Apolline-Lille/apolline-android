package science.apolline

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import science.apolline.service.synchronisation.SyncJobManager
import com.google.firebase.analytics.FirebaseAnalytics


/**
 * Created by sparow on 05/02/18.
 */


class ApollineApplication : Application() {

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        Fabric.with(this, Crashlytics())
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        LeakCanary.install(this)
        SyncJobManager.getJobManager(this)
    }
}


