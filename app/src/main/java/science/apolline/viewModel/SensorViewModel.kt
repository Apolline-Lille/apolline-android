package science.apolline.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import retrofit2.Call
import retrofit2.Response
import science.apolline.service.database.AppDatabase
import science.apolline.service.database.SensorDao
import science.apolline.models.InfluxBody
import science.apolline.models.Device
import science.apolline.service.networks.ApiService
import science.apolline.service.networks.ApiUtils
import science.apolline.utils.RequestParser
import org.jetbrains.anko.*
import science.apolline.BuildConfig
import android.net.ConnectivityManager
import io.reactivex.Flowable


class SensorViewModel(application: Application) : AndroidViewModel(application), AnkoLogger {


    val sensorModel: SensorDao = AppDatabase.getInstance(getApplication()).sensorDao()
    var deviceListObserver: Flowable<List<Device>> = sensorModel.all()


    private fun sendData(device: Device) {

        doAsync {
            if (isConnectingToInternet(getApplication())) {
                val requestBody: String = RequestParser.createSingleRequestBody(device)
//                info(requestBody)
                val api: ApiService = ApiUtils.apiService
                val postCall: Call<InfluxBody> = api.savePost(BuildConfig.INFLUXDB_DBNAME, BuildConfig.INFLUXDB_USR, BuildConfig.INFLUXDB_PWD, requestBody)
                val postResponse: Response<InfluxBody>
                postResponse = postCall.execute()

                if (postResponse.isSuccessful()) {
                    info("Data send: success")
                } else {
                    info("Data send: Failure, message = " + postResponse.message())
                }
            } else {
                info("Data send: can't establish internet connection")
            }

        }

    }

    private fun setPersistant(device: Device) {
        doAsync {
            info(device.data?.toString())
            sensorModel.insertOne(device)
        }
    }

    fun isConnectingToInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }


}