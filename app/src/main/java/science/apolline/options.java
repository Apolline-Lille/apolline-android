package science.apolline;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import science.apolline.ioio.ioioActivity;


/**
 * Created by Cyril on 17/02/2017.
 */

public class options extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private SharedPreferences prefs;

    private Switch swChart;
    private Switch swTemperature;
    private Switch swTracking;
    private Switch swMaps;
    private Switch swAtmo;
    private SeekBar sbFrequency;
    private EditText etSensorId;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        prefs = getApplicationContext().getSharedPreferences(MainActivity.MY_PREFS_NAME,MODE_PRIVATE);

        swChart = (Switch) findViewById(R.id.opt_sw_chart_types);
        swChart.setOnCheckedChangeListener(this);
        swTemperature = (Switch) findViewById(R.id.opt_sw_temp);
        swTemperature.setOnCheckedChangeListener(this);
        swTracking = (Switch) findViewById(R.id.opt_sw_auto_tracking);
        swTracking.setOnCheckedChangeListener(this);
        swMaps = (Switch) findViewById(R.id.opt_sw_maps);
        swMaps.setOnCheckedChangeListener(this);
        swAtmo = (Switch) findViewById(R.id.opt_sw_ATMO);
        swAtmo.setOnCheckedChangeListener(this);

        sbFrequency = (SeekBar) findViewById(R.id.opt_sb_frequency);

        etSensorId = (EditText) findViewById(R.id.opt_et_sensor_id);

        // perform seek bar change listener event used for getting the progress value
        sbFrequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("Frequency", seekBar.getProgress());
                Toast.makeText(getApplicationContext(), "Enregistrement toute les :" + seekBar.getProgress() + "s",
                        Toast.LENGTH_SHORT).show();
                editor.apply();
            }
        });

        // getting the id sensor value (EditText)
        etSensorId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    SharedPreferences.Editor editor = prefs.edit();
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    editor.putString("IDSensor", v.getText().toString());
                    editor.apply();
                    return true; // Focus will do whatever you put in the logic.
                }
                return false;  // Focus will change according to the actionId
            }
        });


    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        SharedPreferences.Editor editor = prefs.edit();

        if(buttonView==swAtmo){
            editor.putBoolean("Atmo", isChecked);
        }else if(buttonView==swTracking){
            editor.putBoolean("Tracking", isChecked);
        }else if(buttonView==swMaps){
            editor.putBoolean("Maps", isChecked);
        }else if(buttonView==swTemperature){
            editor.putBoolean("Temperature", isChecked);
        }else if(buttonView==swChart){
            editor.putBoolean("swChart", isChecked);
        }

        editor.apply();
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


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        prefs=getApplicationContext().getSharedPreferences(ioioActivity.MY_PREFS_NAME,MODE_PRIVATE);
        swChart.setChecked(prefs.getBoolean("swChart", true));
        swTemperature.setChecked(prefs.getBoolean("Temperature", true));
        swTracking.setChecked(prefs.getBoolean("Tracking", true));
        swMaps.setChecked(prefs.getBoolean("Maps", true));
        swAtmo.setChecked(prefs.getBoolean("Atmo", true));
        sbFrequency.setProgress(prefs.getInt("Frequency", 60));
        etSensorId.setText(prefs.getString("IDSensor", getResources().getString(R.string.opt_sensor_id_default)));
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("swChart", swChart.isChecked());
        editor.putBoolean("Temperature", swTemperature.isChecked());
        editor.putBoolean("Tracking", swTracking.isChecked());
        editor.putBoolean("Maps", swMaps.isChecked());
        editor.putBoolean("Atmo", swAtmo.isChecked());
        editor.putInt("Frequency", sbFrequency.getProgress());
        editor.putString("IDSensor", etSensorId.getText().toString());
        editor.apply();
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();

    }


}


