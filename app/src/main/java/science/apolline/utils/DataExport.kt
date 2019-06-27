package science.apolline.utils

import android.content.Context
import android.os.Environment.getExternalStorageDirectory
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
import science.apolline.models.Device
import science.apolline.service.database.SensorDao


/**
 * Created by damien on 08/12/2017.
 */

object DataExport : AnkoLogger {

    private fun toHeader(data: JsonObject?): Array<String> {
        val headerArray = mutableListOf<String>()
        headerArray.apply {
            add("SensorID")
            add("Device")
            add("Date")
//            add("Geohash")
            add("Longitude")
            add("Latitude")
            add("Provider")
            add("Transport_:_Confidence")
        }

        data!!.entrySet().iterator().forEach {

            var tmp = it.key + "_" + it.value.asJsonArray[1]

            if (tmp.contains(("#/0.1L").toRegex())) {
                tmp = tmp.substringBefore("#/0.1L").replace(("\\.").toRegex(), "_")
                tmp += "#/0.1L"
            } else {
                tmp = tmp.replace(("\\.").toRegex(), "_")
            }
            info(tmp)
            tmp = tmp.replace(("\"|°").toRegex(), "")
            tmp = tmp.replace(("µ").toRegex(), "u")
            headerArray.add(tmp)

        }

        return headerArray.toTypedArray()
    }

    fun exportToJson(context: Context, sensorDao: SensorDao) {
        var isDone: Boolean
        doAsync {
            val dataList = sensorDao.dumpSensor()
            info("List size: " + dataList.size.toString())
            isDone = if (dataList.isNotEmpty()) {
                val fw = FileWriter(createFileName("json"))
                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonFile = gson.toJson(dataList)
                print(jsonFile.toString())
                fw.write(jsonFile)
                true
            } else {
                false
            }

            uiThread {
                if (isDone) {
                    Toasty.success(context, "Data exported to JSON with success!", Toast.LENGTH_SHORT, true).show()
                } else {
                    Toasty.error(context, "No data found, please collect some data before export", Toast.LENGTH_LONG, true).show()
                }
            }
        }
    }

    fun exportToCsv(context: Context, sensorDao: SensorDao, multiple: Boolean) {
        var isDone: Boolean
        doAsync {
            val dataList = sensorDao.dumpSensor()
            info("List size: " + dataList.size.toString())
            isDone = if (dataList.isNotEmpty()) {
                createCsv(dataList, multiple)
                true
            } else {
                false
            }

            uiThread {
                if (isDone) {
                    Toasty.success(context, "Data exported to CSV with success!", Toast.LENGTH_SHORT, true).show()
                } else {
                    Toasty.error(context, "No data found, please collect some data before export", Toast.LENGTH_LONG, true).show()
                }
            }
        }
    }


    fun exportShareCsv(context: Context, sensorDao: SensorDao, multiple: Boolean) {
        var isDone: Boolean
        doAsync {
            val dataList = sensorDao.dumpSensor()
            info("List size: " + dataList.size.toString())
            isDone = if (dataList.isNotEmpty()) {
                createCsv(dataList, multiple)
                true
            } else {
                false
            }

            uiThread {
                if (isDone) {
                    val file = File(localFolder(), "data_${CheckUtility.newDate()}.csv")
                    val uri: Uri
                    uri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        Uri.fromFile(file)
                    } else {
                        FileProvider.getUriForFile(context, "science.apolline.fileprovider", file)
                    }

                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    shareIntent.type = "text/plain"
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    Toasty.success(context, "Data exported with success!", Toast.LENGTH_SHORT, true).show()
                    context.startActivity(Intent.createChooser(shareIntent, context.resources.getText(R.string.send_to)))
                } else {
                    Toasty.error(context, "No data found, please collect some data before export", Toast.LENGTH_LONG, true).show()
                }


            }
        }
    }

    private fun createFileName(extension: String): String {
        return localFolder().toString() + "/" + "dump_data_${CheckUtility.newDate()}.$extension"
    }


    private fun createMultiFileName(date: String, extension: String): String {
        return localFolder().toString() + "/" + "data_$date.$extension"
    }


    private fun localFolder(): File {
        val folder = File(getExternalStorageDirectory().toString() + "/Apolline")
        if (!folder.exists())
            folder.mkdir()
        return folder
    }


    private fun createCsv(dataList: List<Device>, multiple: Boolean) {
        info("List size: " + dataList.size.toString())
        val entries: MutableList<Array<String>> = mutableListOf()
        entries.add(toHeader(dataList[0].data))

        if (multiple) {

            val previousDateWithTime = dataList[1].toArray()[2]
            var previousDate = previousDateWithTime.substring(0, previousDateWithTime.length - 9)

            dataList.forEach {

                val currentDateWithTime = it.toArray()[2]
                val currentDate = currentDateWithTime.substring(0, currentDateWithTime.length - 9)

                if (currentDate == previousDate) {
                    entries.add(it.toArray())
                } else {
                    info("write to csv")

                    CSVWriter(FileWriter(createMultiFileName(previousDate, "csv"))).use { writer ->
                        writer.writeAll(entries)
                    }
                    previousDate = currentDate
                    entries.clear()
                }
            }

            val lastDateWithTime = dataList.last().toArray()[2]
            val lastDate = lastDateWithTime.substring(0, lastDateWithTime.length - 9)

            CSVWriter(FileWriter(createMultiFileName(lastDate, "csv"))).use { writer ->
                writer.writeAll(entries)
            }


        } else {
            dataList.forEach {
                entries.add(it.toArray())
            }
            CSVWriter(FileWriter(createFileName("csv"))).use { writer -> writer.writeAll(entries) }
        }
    }

}

