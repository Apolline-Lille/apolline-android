package science.apolline.viewModel

import android.Manifest
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
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
import java.io.IOException
import java.util.*

class SensorViewModel(application: Application) : AndroidViewModel(application){
    init {
        val BReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Log.e("viewModel","receiver")
                val data : IntfSensorData = intent.getParcelableExtra("dataBundle")
                dataLive.postValue(data)

                Log.e("viewModel","${dataLive.hasActiveObservers()}");
                //TODO : move that to Device sendBroadcast as intent extra
                val calendar = Calendar.getInstance()
                var d1 = calendar.time
                //TODO position

                //TODO device and Device param as intent extra
                val device: Device = Device("IOIO",d1.toString(),null,dataLive.value?.toJson())
                setPersistant(device)
                //sendData(Device)
            }
        }
        LocalBroadcastManager.getInstance(application).registerReceiver(BReceiver, IntentFilter(application.getString(R.string.dataBroadcastFilter)))
    }

    var dataLive : MutableLiveData<IntfSensorData> = MutableLiveData()

    private fun sendData(device: Device) {
        val requestBody: String = RequestParser.createRequestBody(device)
        val api : ApiService = ApiUtils.apiService
        val postCall : Call<InfluxBody> = api.savePost("test","toto","root",requestBody)
        val postResponse: Response<InfluxBody>
        postResponse = postCall.execute();
        if (postResponse.isSuccessful()) {
            Toast.makeText(getApplication(), "Data send: success", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplication(), "Data send: Failure, message = " + postResponse.message(), Toast.LENGTH_SHORT).show();
        }
    }

    private fun setPersistant(device: Device) {

        val sensorModel: SensorDao = AppDatabase.getInstance(getApplication())
        sensorModel.insertOne(device)
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
//            appDatabase.SensorModel().insertOne(sensor);
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



}