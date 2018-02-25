package science.apolline.view.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_options.*

import science.apolline.R


/**
 * Created by Cyril on 17/02/2017.
 */

class OptionsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {

    private var prefs: SharedPreferences? = null

    private var swChart: Switch? = null
    private var swTemperature: Switch? = null
    private var swTracking: Switch? = null
    private var swMaps: Switch? = null
    private var swAtmo: Switch? = null
    private var sbFrequency: SeekBar? = null
    private var etSensorId: EditText? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        prefs = applicationContext.getSharedPreferences(MainActivity.MY_PREFS_NAME, Context.MODE_PRIVATE)


        swChart = opt_sw_chart_types
        swChart!!.setOnCheckedChangeListener(this)
        swTemperature = opt_sw_temp
        swTemperature!!.setOnCheckedChangeListener(this)
        swTracking = opt_sw_auto_tracking
        swTracking!!.setOnCheckedChangeListener(this)
        swMaps = opt_sw_maps
        swMaps!!.setOnCheckedChangeListener(this)
        swAtmo = opt_sw_ATMO
        swAtmo!!.setOnCheckedChangeListener(this)

        sbFrequency = opt_sb_frequency

        etSensorId = opt_et_sensor_id

        // perform seek bar change listener event used for getting the progress value
        sbFrequency!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val editor = prefs!!.edit()
                editor.putInt("Frequency", seekBar.progress)
                Toast.makeText(applicationContext, "Enregistrement toute les :" + seekBar.progress + "s",
                        Toast.LENGTH_SHORT).show()
                editor.apply()
            }
        })

        // getting the id sensor value (EditText)
        etSensorId!!.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val editor = prefs!!.edit()
                val imm = applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                editor.putString("IDSensor", v.text.toString())
                editor.apply()
                return@OnEditorActionListener true // Focus will do whatever you put in the logic.
            }
            false  // Focus will change according to the actionId
        })


    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {

        val editor = prefs!!.edit()

        if (buttonView === swAtmo) {
            editor.putBoolean("Atmo", isChecked)
        } else if (buttonView === swTracking) {
            editor.putBoolean("Tracking", isChecked)
        } else if (buttonView === swMaps) {
            editor.putBoolean("Maps", isChecked)
        } else if (buttonView === swTemperature) {
            editor.putBoolean("Temperature", isChecked)
        } else if (buttonView === swChart) {
            editor.putBoolean("swChart", isChecked)
        }

        editor.apply()
        //        if (swChart.isChecked()) {
        //            graph.setVisibility(View.INVISIBLE);
        //            pm1.setVisibility(View.VISIBLE);
        //            pm2.setVisibility(View.VISIBLE);
        //            pm10.setVisibility(View.VISIBLE);
        //            textViewPM1.setVisibility(View.VISIBLE);
        //            textViewPM2.setVisibility(View.VISIBLE);
        //            textViewPM10.setVisibility(View.VISIBLE);
        //            textPM1.setVisibility(View.VISIBLE);
        //            textPM2.setVisibility(View.VISIBLE);
        //            textPM10.setVisibility(View.VISIBLE);
        //            ViewGroup.LayoutParams params = graph.getLayoutParams();
        //            params.height = 0;
        //            graph.setLayoutParams(params);
        //        } else {
        //
        //            graph.setVisibility(View.VISIBLE);
        //            pm1.setVisibility(View.INVISIBLE);
        //            pm2.setVisibility(View.INVISIBLE);
        //            pm10.setVisibility(View.INVISIBLE);
        //            textViewPM1.setVisibility(View.INVISIBLE);
        //            textViewPM2.setVisibility(View.INVISIBLE);
        //            textViewPM10.setVisibility(View.INVISIBLE);
        //            textPM1.setVisibility(View.INVISIBLE);
        //            textPM2.setVisibility(View.INVISIBLE);
        //            textPM10.setVisibility(View.INVISIBLE);
        //            ViewGroup.LayoutParams params = graph.getLayoutParams();
        //            params.height = 270 * 2;
        //            graph.setLayoutParams(params);
        //        }
        //        if (swMaps.isChecked()) {
        //            mapFragment.getView().setVisibility(View.VISIBLE);
        //            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        //            params.height = 256 * 2;
        //            mapFragment.getView().setLayoutParams(params);
        //        } else {
        //            mapFragment.getView().setVisibility(View.INVISIBLE);
        //            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        //            params.height = 0;
        //            mapFragment.getView().setLayoutParams(params);
        //        }
        //        if (swAtmo.isChecked()) {
        //            view.setVisibility(View.VISIBLE);
        //            ViewGroup.LayoutParams params = view.getLayoutParams();
        //            params.height = 320 * 2;
        //            view.setLayoutParams(params);
        //        } else {
        //            view.setVisibility(View.INVISIBLE);
        //            ViewGroup.LayoutParams params = view.getLayoutParams();
        //            params.height = 0;
        //            view.setLayoutParams(params);
        //        }
    }


    public override fun onResume() {
        super.onResume()  // Always call the superclass method first
        prefs = applicationContext.getSharedPreferences(MainActivity.MY_PREFS_NAME, Context.MODE_PRIVATE)
        swChart!!.isChecked = prefs!!.getBoolean("swChart", true)
        swTemperature!!.isChecked = prefs!!.getBoolean("Temperature", true)
        swTracking!!.isChecked = prefs!!.getBoolean("Tracking", true)
        swMaps!!.isChecked = prefs!!.getBoolean("Maps", true)
        swAtmo!!.isChecked = prefs!!.getBoolean("Atmo", true)
        sbFrequency!!.progress = prefs!!.getInt("Frequency", 60)
        etSensorId!!.setText(prefs!!.getString("IDSensor", resources.getString(R.string.opt_sensor_id_default)))
    }

    public override fun onPause() {
        super.onPause()  // Always call the superclass method first
        val editor = prefs!!.edit()
        editor.putBoolean("swChart", swChart!!.isChecked)
        editor.putBoolean("Temperature", swTemperature!!.isChecked)
        editor.putBoolean("Tracking", swTracking!!.isChecked)
        editor.putBoolean("Maps", swMaps!!.isChecked)
        editor.putBoolean("Atmo", swAtmo!!.isChecked)
        editor.putInt("Frequency", sbFrequency!!.progress)
        editor.putString("IDSensor", etSensorId!!.text.toString())
        editor.apply()
    }

    override fun onStop() {
        // call the superclass method first
        super.onStop()

    }


}


