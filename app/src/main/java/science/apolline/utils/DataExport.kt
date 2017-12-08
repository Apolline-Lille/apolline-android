package science.apolline.utils

import android.content.Context
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import com.google.gson.Gson
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.onComplete
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import science.apolline.service.database.AppDatabase
import java.io.File
import java.io.FileWriter
import com.google.gson.GsonBuilder




/**
 * Created by damien on 08/12/2017.
 */

class DataExport {
    fun toJson(context:Context) {

        val folder = File(getExternalStorageDirectory().toString()+"/Folder")

        if (!folder.exists())
           folder.mkdir()

        val filename = folder.toString() + "/" + "Test.json"
        
        val sensorDao = AppDatabase.getInstance(context)
        doAsync {
            val fw = FileWriter(filename)
            val allData = sensorDao.all
            val dataList = allData.blockingGet()
            Log.e("export",dataList.size.toString())
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonFile = gson.toJson(dataList)
            fw.write(jsonFile)
            uiThread {
                context.toast("data exported")
            }
        }
    }
}
