package science.apolline.utils

import android.content.Context
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import science.apolline.service.database.AppDatabase
import java.io.File
import java.io.FileWriter
import com.google.gson.GsonBuilder
import com.opencsv.CSVWriter
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import com.google.gson.JsonObject
import science.apolline.R
import org.jetbrains.anko.*


/**
 * Created by damien on 08/12/2017.
 */

class DataExport : AnkoLogger {

    private fun toHeader(data: JsonObject?): Array<String> {
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

    fun exportToJson(context: Context) {

        val folder = File(getExternalStorageDirectory().toString() + "/Apolline")

        if (!folder.exists())
            folder.mkdir()

        val filename = folder.toString() + "/" + "data.json"

        doAsync {

            val sensorDao = AppDatabase.getInstance(context).sensorDao()
            val fw = FileWriter(filename)
            val dataList = sensorDao.dumpSensor()
            Log.e("exportToCsv", dataList.size.toString())
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonFile = gson.toJson(dataList)
            fw.write(jsonFile)
            uiThread {
                context.toast("data exported to JSON")
            }
        }
    }


    fun exportToCsv(context: Context) {

        val folder = File(getExternalStorageDirectory().toString() + "/Apolline")

        if (!folder.exists())
            folder.mkdir()

        val filenameCSV = folder.toString() + "/" + "data.csv"

        doAsync {

            val sensorDao = AppDatabase.getInstance(context).sensorDao()
            val dataList = sensorDao.dumpSensor()
            Log.e("exportToCsv", dataList.size.toString())

            val entries: MutableList<Array<String>> = mutableListOf()
            entries.add(toHeader(dataList[0].data))
            dataList.forEach {
                entries.add(it.toArray())
            }

            CSVWriter(FileWriter(filenameCSV)).use { writer -> writer.writeAll(entries) }


            uiThread {

                val file = File(folder,"data.csv")
                val uri :Uri
                uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    Uri.fromFile(file)
                }else{
                    FileProvider.getUriForFile(context, "science.apolline.fileprovider", file)
                }

                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "text/plain"
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.toast("Data exported with success")
                context.startActivity(Intent.createChooser(shareIntent, context.resources.getText(R.string.send_to)))
            }
        }
    }


}
