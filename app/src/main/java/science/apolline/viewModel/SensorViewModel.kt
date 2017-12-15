package science.apolline.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import retrofit2.Call
import retrofit2.Response
import science.apolline.R
import science.apolline.service.database.AppDatabase
import science.apolline.service.database.SensorDao
import science.apolline.models.InfluxBody
import science.apolline.models.Device
import science.apolline.service.networks.ApiService
import science.apolline.service.networks.ApiUtils
import science.apolline.models.IntfSensorData
import science.apolline.utils.RequestParser
import java.util.*
import android.os.Bundle
import org.jetbrains.anko.*
import science.apolline.models.Position
import science.apolline.service.geolocalisation.SingleShotLocationProvider
import science.apolline.BuildConfig
import android.net.ConnectivityManager




class SensorViewModel(application: Application) : AndroidViewModel(application), AnkoLogger {
    init {

        val BReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val data : IntfSensorData = intent.getParcelableExtra(
                        application.getString(R.string.serviceBroadCastDataSet)
                )
                dataLive.postValue(data)

                Log.e("viewModel","${dataLive.hasActiveObservers()}");

                val d1 = intent.getSerializableExtra(application.getString(R.string.serviceBroadCastDate)) as Date;

                var position: Position
                mLocationListener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        position=Position(location.provider,location.longitude,location.latitude,"null")

                        val device: Device = Device(
                                intent.getStringExtra(application.getString(R.string.serviceBroadCastSensorName))
                                ,d1.toString()
                                , position,dataLive.value?.toJson()
                        )
                        setPersistant(device)
//                        sendData(device)
                    }
                    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }


                val singleShotLocationProvider  = SingleShotLocationProvider
                singleShotLocationProvider.requestSingleUpdate(application,mLocationListener)
            }
        }
        LocalBroadcastManager.getInstance(application).registerReceiver(BReceiver, IntentFilter(application.getString(R.string.sensorBroadCast)))
    }

    var dataLive : MutableLiveData<IntfSensorData> = MutableLiveData()
    lateinit var mLocationListener: LocationListener


    private fun sendData(device: Device) {

        doAsync {
            if(isConnectingToInternet(getApplication())){
                val requestBody: String = RequestParser.createRequestBody(device)
                val api : ApiService = ApiUtils.apiService
                val postCall : Call<InfluxBody> = api.savePost(BuildConfig.INFLUXDB_DBNAME,BuildConfig.INFLUXDB_USR,BuildConfig.INFLUXDB_PWD,requestBody)
                val postResponse: Response<InfluxBody>
                postResponse = postCall.execute()

                if (postResponse.isSuccessful()) {
                    info("Data send: success")
                } else {
                    info("Data send: Failure, message = " + postResponse.message())
                }
            }else{
                info("Data send: can't establish internet connection")
            }

        }

    }

    private fun setPersistant(device: Device) {
        doAsync {
            val sensorModel: SensorDao = AppDatabase.getInstance(getApplication())
            info(device.data?.toString())
            sensorModel.insertOne(device)
        }



//        val list = sensorModel.all.blockingGet()
//        val count = sensorModel.all.blockingGet().size
//        Log.e(this.javaClass.name,"/////////")
//        Log.e(this.javaClass.name,"$count")
//        Log.e(this.javaClass.name,"${ list.last()}")
//        Log.e(this.javaClass.name,"/////////")
    }
//    BReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            IOIOData data = intent.getParcelableExtra("IOIOData");
//            int PM01Value = data.getPM01Value();
//            int PM2_5Value = data.getPM2_5Value();
//            int PM10Value = data.getPM10Value();
//
//            progressPM1.setProgress(PM01Value);
//            progressPM2.setProgress(PM2_5Value);
//            progressPM10.setProgress(PM10Value);
//
//            textViewPM1.setText(PM01Value+"");
//            textViewPM2.setText(PM2_5Value+"");
//            textViewPM10.setText(PM10Value+"");
//            Calendar c = Calendar.getInstance();
//            Date d1 = c.getTime();
//
//            int nbPoint = 10 * 60;
//
//            series.appendData(new DataPoint(d1,PM01Value),true,nbPoint);
//            series2.appendData(new DataPoint(d1,PM2_5Value),true,nbPoint);
//            series10.appendData(new DataPoint(d1,PM10Value),true,nbPoint);
//
//
////TODO localisation
//            Device sensor = new Device("IOIO","IOIO",d1.toString(),null,data.toJson());
//
//            AppDatabase appDatabase = AppDatabase.Companion.getAppDatabase(getActivity());
//            appDatabase.sensorDao().insertOne(sensor);
//
//            String requestBody = RequestParser.INSTANCE.createRequestBody(sensor);
//            ApiService api = ApiUtils.INSTANCE.getApiService();
//            Call<InfluxBody> postCall = api.savePost("test", "toto", "root", requestBody);
//            Response<InfluxBody> postResponse;
//            try {
//                postResponse= postCall.execute();
//                if (postResponse.isSuccessful()){
//                    Toast.makeText(getActivity(),"Data send: success",Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    Toast.makeText(getActivity(),"Data send: Failure, message = "+postResponse.message(),Toast.LENGTH_SHORT).show();
//                }
//            } catch (IOException e) {
//                Toast.makeText(getActivity(),"Unable to send data",Toast.LENGTH_SHORT).show();
//            }
//
//        }


    fun isConnectingToInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }


}