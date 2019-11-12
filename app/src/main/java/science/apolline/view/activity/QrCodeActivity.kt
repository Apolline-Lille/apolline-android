package science.apolline.view.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_qr_code.*
import science.apolline.R

class QrCodeActivity : AppCompatActivity() {

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
        if(result != null){
            if(result.contents == null){
                //the result data is null or empty then
                Toast.makeText(this,"The data is empty",Toast.LENGTH_SHORT).show()
            }else{
                //result maybe empty then we use settext
                edt_scan_value.setText(result.contents.toString())
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

    }
}
