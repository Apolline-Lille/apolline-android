package science.apolline.utils

import android.content.Context
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import science.apolline.service.database.AppDatabase
import java.io.File
import java.io.FileWriter
import com.google.gson.GsonBuilder
import com.opencsv.CSVWriter



/**
 * Created by damien on 08/12/2017.
 */

class DataExport {
    fun toJson(context:Context) {

        val folder = File(getExternalStorageDirectory().toString()+"/Folder")

        if (!folder.exists())
            folder.mkdir()

        val filename = folder.toString() + "/" + "data.json"

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
                context.toast("data exported to JSON")
            }
        }
    }

    fun toCsv(context:Context) {

        val folder = File(getExternalStorageDirectory().toString() + "/Folder")
        if (!folder.exists())
            folder.mkdir()
        val filename = folder.toString() + "/" + "data.csv"
        val sensorDao = AppDatabase.getInstance(context)

        doAsync {

            val allData = sensorDao.all
            val dataList = allData.blockingGet()
            val entries: MutableList<Array<String>> = mutableListOf()
            entries.add(dataList[0].toHeader())
            dataList.forEach {
                entries.add(it.toArray())
            }

            CSVWriter(FileWriter(filename)).use { writer -> writer.writeAll(entries) }
            uiThread {
                context.toast("data exported to CSV")
            }
        }
    }

    fun export(context:Context) {

        val folder = File(getExternalStorageDirectory().toString()+"/Folder")
        if (!folder.exists())
            folder.mkdir()
        val filenameCSV = folder.toString() + "/" + "data.csv"
        val filenameJSON = folder.toString() + "/" + "data.json"
        val sensorDao = AppDatabase.getInstance(context)

        doAsync {
            val fw = FileWriter(filenameJSON)
            val allData = sensorDao.all
            val dataList = allData.blockingGet()
            Log.e("export",dataList.size.toString())
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonFile = gson.toJson(dataList)
            fw.write(jsonFile)

            val entries: MutableList<Array<String>> = mutableListOf()
            entries.add(dataList[0].toHeader())
            dataList.forEach{
                entries.add(it.toArray())
            }
            CSVWriter(FileWriter(filenameCSV)).use { writer -> writer.writeAll(entries) }
            uiThread {
                context.toast("Data exported to JSON and CSV")
            }
        }
    }





}
