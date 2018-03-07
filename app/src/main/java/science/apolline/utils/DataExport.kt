package science.apolline.utils

import android.content.Context
import android.os.Environment.getExternalStorageDirectory
import science.apolline.service.database.AppDatabase
import java.io.File
import java.io.FileWriter
import com.google.gson.GsonBuilder
import com.opencsv.CSVWriter
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.widget.Toast
import com.google.gson.JsonObject
import es.dmoral.toasty.Toasty
import science.apolline.R
import org.jetbrains.anko.*


/**
 * Created by damien on 08/12/2017.
 */

object DataExport : AnkoLogger {

    private fun toHeader(data: JsonObject?): Array<String> {
        val headerArray = mutableListOf<String>()
        headerArray.add("SensorID")
        headerArray.add("Device")
        headerArray.add("Date")
        headerArray.add("Geohash")
        headerArray.add("Provider")
        headerArray.add("Transport")

        data!!.entrySet().iterator().forEach {
            headerArray.add(it.key + "_" + it.value.asJsonArray[1].toString().replace("\"", ""))
        }

        return headerArray.toTypedArray()
    }

    fun exportToJson(context: Context) {
        doAsync {
            val sensorDao = AppDatabase.getInstance(context).sensorDao()
            val dataList = sensorDao.dumpSensor()
            info("List size: "+dataList.size.toString())
            val fw = FileWriter(filename("json"))
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonFile = gson.toJson(dataList)
            fw.write(jsonFile)

            uiThread {
                Toasty.success(context, "Data exported to JSON with success!", Toast.LENGTH_SHORT, true).show()
            }
        }
    }

    fun exportToCsv(context: Context) {
        doAsync {
            val sensorDao = AppDatabase.getInstance(context).sensorDao()
            val dataList = sensorDao.dumpSensor()
            info("List size: "+dataList.size.toString())
            val entries: MutableList<Array<String>> = mutableListOf()
            entries.add(toHeader(dataList[0].data))
            dataList.forEach {
                entries.add(it.toArray())
            }
            CSVWriter(FileWriter(filename("csv"))).use { writer -> writer.writeAll(entries) }

            uiThread {
                Toasty.success(context, "Data exported to CSV with success!", Toast.LENGTH_SHORT, true).show()
            }
        }
    }

    fun exportShareCsv(context: Context) {
        doAsync {
            val sensorDao = AppDatabase.getInstance(context).sensorDao()
            val dataList = sensorDao.dumpSensor()
            info("List size: "+dataList.size.toString())
            val entries: MutableList<Array<String>> = mutableListOf()
            entries.add(toHeader(dataList[0].data))
            dataList.forEach {
                entries.add(it.toArray())
            }
            CSVWriter(FileWriter(filename("csv"))).use { writer -> writer.writeAll(entries) }

            uiThread {
                val file = File(localFolder(),"data.csv")
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
                Toasty.success(context, "Data exported with success!", Toast.LENGTH_SHORT, true).show()
                context.startActivity(Intent.createChooser(shareIntent, context.resources.getText(R.string.send_to)))
            }
        }
    }

    private fun filename(extension: String): String {
        val filenameCSV = localFolder().toString() + "/" + "data.$extension"
        return filenameCSV
    }

    private fun localFolder(): File {
        val folder = File(getExternalStorageDirectory().toString() + "/Apolline")
        if (!folder.exists())
            folder.mkdir()
        return folder
    }
}
