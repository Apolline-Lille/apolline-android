package science.apolline.view.activity

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_qr_code.*
import science.apolline.R

class QrCodeActivity : AppCompatActivity() {

    private lateinit var mPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_code)

        initFunction()
    }

    private fun initFunction() {
        btn_scan.setOnClickListener{
            initScan()
        }
    }

    private fun initScan(){
        IntentIntegrator(this).initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
        //check if result is qrcode
        if(result != null){
            if(result.contents == null){
                //the result data is null or empty then
                Toast.makeText(this,"The data is empty",Toast.LENGTH_SHORT).show()
            }else{
                //result may be empty then we use settext
                edt_scan_value.setText(result.contents.toString())
                saveSettings(result.contents.toString())
            }
        }else{
            // the camera will not close
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
     * Affiachage données / Boolean / DISPLAY_DATA / True(Les données sont affichées)
     */
    private fun saveSettings(result : String){
        val uri = Uri.parse(result)
        //println(uri)
        //println(uri.queryParameterNames)
        //println(uri.getQueryParameter(uri.queryParameterNames.iterator().next()))

        var iterator = uri.queryParameterNames.iterator()

        while(iterator.hasNext()){
            var key = iterator.next()
            var value = uri.getQueryParameter(key)
            println(key +" "+ value)
        }


        //val editor = mPrefs.edit()
        //with(editor) {

        //    apply()
        //}
    }
}
