package science.apolline.view.activity

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_qr_code.*
import science.apolline.R
import science.apolline.view.adapter.SettingAdapter

class QrCodeActivity : AppCompatActivity() {

    private var Adapter = SettingAdapter()
    private var paramFromParsedUri = HashMap<String,String>()

    private lateinit var mPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)
        var rvSetting = this.findViewById<RecyclerView>(R.id.recycler_param)
        rvSetting.layoutManager = LinearLayoutManager(this)
        rvSetting.adapter = Adapter
        btn_save.isEnabled =false

        initFunction()
    }

    private fun initFunction() {
        btn_scan.setOnClickListener{
            initScan()
        }

        btn_exit.setOnClickListener{
            closeActivity()
        }
    }

    private fun initScan(){
        IntentIntegrator(this).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
        //check if result is qrcode
        if(result != null){
            if(result.contents.isNullOrEmpty()){
                //the result data is null or empty then
                Toast.makeText(this,"The data is empty",Toast.LENGTH_SHORT).show()
            }else{
                LoadResult(result.contents.toString())
            }
        }else{
            // the camera will not close
            // if there is no QR_Code the camera will continue to scan until user cancel action
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    /**
     * Parameter / Type / Key / Default value
     * URL du serveur / URL + PORT / BACKEND_URL / http://apolline.lille.inria.fr:8086
     * Label des données / String / MEASUREMENT_LABEL / sandbox
     * Fréquence de syncrhonisation (app->serveur) / int(secondes) / SYNC_FREQUENCY / 60(1minute)
     * Fréquence d'acquisition(capteur->app) / int(secondes) / ACQ_FREQUENCY / 5(seconde)
     * Synchronisation en wifi uniquement / Boolean / SYNC_WIFI / False(Synchro 4G autorisée)
     * Affichage données / Boolean / DISPLAY_DATA / True(Les données sont affichées)
     */
    private fun LoadResult(result : String){
        val uri = Uri.parse(result)

        var iterator = uri.queryParameterNames.iterator()

        var paramFromUriParsedList = ArrayList<String>()

        while(iterator.hasNext()){

            var key = iterator.next()
            var value = uri.getQueryParameter(key)
            paramFromUriParsedList.add(key + ":" + value)
            paramFromParsedUri.put(key,value)
        }

        paramFromParsedUri.put("host","http://"+uri.host)
        paramFromParsedUri.put("port",uri.port.toString())

        paramFromUriParsedList.add("host" + ":" + uri.host)
        paramFromUriParsedList.add("port" + ":" + uri.port)

        Adapter.setValue(paramFromUriParsedList)

        btn_save.isEnabled = true
        btn_save.setOnClickListener{
            saveSettings()
        }

    }

    private fun saveSettings() {

        val editor = mPrefs.edit()

        paramFromParsedUri.forEach {
            (key, value) -> editor.putString(key.toLowerCase(),paramFromParsedUri.get(key))
        }

        editor.apply()

        Toasty.success(applicationContext, "Settings saved !", Toast.LENGTH_LONG, true).show()

        this.finish()
    }

    private fun closeActivity(){
        Toasty.warning(applicationContext, "Settings not saved !", Toast.LENGTH_LONG, true).show()
        this.finish()
    }
}
