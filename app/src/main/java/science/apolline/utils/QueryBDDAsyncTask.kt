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

    init{
        weakActivity = WeakReference(activity.activity)
        mActivity = activity
    }

    @RequiresApi(Build.VERSION_CODES.M)
    protected override fun doInBackground(vararg params:String):Int {
        val sensorModel = AppDatabase.getInstance(mActivity.context).sensorDao()
        val timestampSyncDao = AppDatabase.getInstance(mActivity.context).timestampSyncDao()
        when(params[0])
        {
            "getSensorCount" -> return sensorModel.getSensorCount().toInt()
            "getSensorSyncCount" -> return sensorModel.getSensorSyncCountByDate(timestampSyncDao.getLastSync() * 1000000).toInt()
            "getSensorNotSyncCount" -> return sensorModel.getSensorNotSyncCountByDate(timestampSyncDao.getLastSync() * 1000000).toInt()
            "deleteDataSync" -> sensorModel.deleteDataSyncByDate(timestampSyncDao.getLastSync() * 1000000)

            else -> return 0
        }
        return sensorModel.getSensorSyncCount().toInt()
    }

    protected override fun onPostExecute(countSyncData:Int) {
        val activity = weakActivity.get() ?: return
        Log.i("","Count Data Sync : " + countSyncData.toString())
        //activity.onBackPressed()
    }
}