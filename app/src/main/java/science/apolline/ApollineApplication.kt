package science.apolline

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.github.salomonbrys.kodein.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import science.apolline.di.KodeinConfInjector
import science.apolline.service.synchronisation.SyncJobManager

/**
 * Created by sparow on 05/02/18.
 */
class ApollineApplication : Application(), KodeinAware {

    override val kodein: Kodein = KodeinConfInjector(this).kodein

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate() {
        super.onCreate()
        Logger.addLogAdapter(AndroidLogAdapter())

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