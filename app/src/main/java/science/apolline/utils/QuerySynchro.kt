package science.apolline.utils

import android.app.Activity
import android.os.AsyncTask
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import science.apolline.service.database.AppDatabase
import science.apolline.view.activity.SettingsActivity
import java.lang.ref.WeakReference

class QuerySynchro(activity: SettingsActivity.DataErasePreferenceFragment) : AsyncTask<String, Void, Long>() {
    //Prevent leak
    private val weakActivity: WeakReference<Activity>
    private var mActivity : SettingsActivity.DataErasePreferenceFragment

    init{
        weakActivity = WeakReference(activity.activity)
        mActivity = activity
    }

    @RequiresApi(Build.VERSION_CODES.M)
    protected override fun doInBackground(vararg params:String):Long {
        val timestampModel = AppDatabase.getInstance(mActivity.context).timestampSyncDao()
        when(params[0])
        {
            "getLastSync" -> return timestampModel.getLastSync()
            else -> return 0
        }
        return timestampModel.getLastSync()
    }

    protected override fun onPostExecute(countSyncData:Long) {
        val activity = weakActivity.get() ?: return
    }
}
