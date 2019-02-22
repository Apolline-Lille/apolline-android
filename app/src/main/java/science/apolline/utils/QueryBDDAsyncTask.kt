package science.apolline.utils

import android.app.Activity
import android.os.AsyncTask
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import science.apolline.service.database.AppDatabase
import science.apolline.view.activity.SettingsActivity
import java.lang.ref.WeakReference

class QueryBDDAsyncTask(activity: SettingsActivity.DataErasePreferenceFragment) : AsyncTask<String, Void, Int>() {
    //Prevent leak
    private val weakActivity: WeakReference<Activity>
    private var mActivity : SettingsActivity.DataErasePreferenceFragment
    private var TO_MILLISECONDS : Int = 1000000

    init{
        weakActivity = WeakReference(activity.activity)
        mActivity = activity
    }

    @RequiresApi(Build.VERSION_CODES.M)
    protected override fun doInBackground(vararg params:String):Int {
        val sensorModel = AppDatabase.getInstance(mActivity.context).sensorDao()
        val timestampSyncDao = AppDatabase.getInstance(mActivity.context).timestampSyncDao()
        var arg0 = params[0]
        Log.d("dtp", "last sync" + timestampSyncDao.getLastSync())
        when(arg0)
        {
            "getSensorCount" -> return sensorModel.getSensorCount().toInt()
            "getSensorSyncCount" -> return sensorModel.getSensorSyncCountByDate(timestampSyncDao.getLastSync() * TO_MILLISECONDS).toInt()
            "getSensorNotSyncCount" -> return sensorModel.getSensorNotSyncCountByDate(timestampSyncDao.getLastSync() * TO_MILLISECONDS).toInt()
            "deleteDataSync" -> sensorModel.deleteDataSyncByDate(timestampSyncDao.getLastSync() * TO_MILLISECONDS)

            else -> return 0
        }
        return sensorModel.getSensorSyncCountByDate(timestampSyncDao.getLastSync() * 1000000).toInt()
    }

    protected override fun onPostExecute(countSyncData:Int) {
        val activity = weakActivity.get() ?: return
    }
}