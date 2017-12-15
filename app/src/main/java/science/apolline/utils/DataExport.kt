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
import android.content.Intent
import android.net.Uri
import com.google.gson.JsonObject
import science.apolline.R


/**
 * Created by damien on 08/12/2017.
 */

class DataExport {

    fun toHeader(data: JsonObject?): Array<String> {
        val headerArray = mutableListOf<String>()
        headerArray.add("SensorID")
        headerArray.add("Device")
        headerArray.add("Date")
        headerArray.add("Latitude")
        headerArray.add("Longitude")
        headerArray.add("Provider")
        headerArray.add("Transport")

        data!!.entrySet().iterator().forEach {
            headerArray.add(it.key + "_" + it.value.asJsonArray[1].toString().replace("\"", ""))
        }

        return headerArray.toTypedArray()
    }

    fun toJson(context:Context) {

        val folder = File(getExternalStorageDirectory().toString()+"/Apolline")

        if (!folder.exists())
            folder.mkdir()

        val filename = folder.toString() + "/" + "data.json"

        doAsync {

            val sensorDao = AppDatabase.getInstance(context)
            val fw = FileWriter(filename)
            val dataList = sensorDao.dumpSensor()
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

        val folder = File(getExternalStorageDirectory().toString() + "/Apolline")
        if (!folder.exists())
            folder.mkdir()
        val filename = folder.toString() + "/" + "data.csv"

        doAsync {

            val sensorDao = AppDatabase.getInstance(context)
            val dataList = sensorDao.dumpSensor()
            val entries: MutableList<Array<String>> = mutableListOf()
            entries.add(toHeader(dataList[0].data))
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

        val folder = File(getExternalStorageDirectory().toString()+"/Apolline")
        if (!folder.exists())
            folder.mkdir()
        val filenameCSV = folder.toString() + "/" + "data.csv"
        val filenameJSON = folder.toString() + "/" + "data.json"

        doAsync {

            val sensorDao = AppDatabase.getInstance(context)
            val fw = FileWriter(filenameJSON)
            val dataList = sensorDao.dumpSensor()
            Log.e("export",dataList.size.toString())
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonFile = gson.toJson(dataList)
            fw.write(jsonFile)

            val entries: MutableList<Array<String>> = mutableListOf()
            entries.add(toHeader(dataList[0].data))
            dataList.forEach{
                entries.add(it.toArray())
            }
            CSVWriter(FileWriter(filenameCSV)).use { writer -> writer.writeAll(entries) }

            uiThread {

                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filenameCSV))
                shareIntent.type = "text/plain"
                context.toast("Data exported with success")
                context.startActivity(Intent.createChooser(shareIntent, context.resources.getText(R.string.send_to)))
            }
        }
    }





}
