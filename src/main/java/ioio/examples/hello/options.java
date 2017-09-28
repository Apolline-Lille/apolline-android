package ioio.examples.hello;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Cyril on 17/02/2017.
 */

public class options extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener {

    private SharedPreferences settings;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);

        MainActivity.SwitchOpt1 = (Switch) findViewById(R.id.switch2);
        MainActivity.SwitchOpt1.setOnCheckedChangeListener(this);
        MainActivity.SwitchOpt2 = (Switch) findViewById(R.id.switch3);
        MainActivity.SwitchOpt2.setOnCheckedChangeListener(this);
        MainActivity.SwitchOpt3 = (Switch) findViewById(R.id.switch4);
        MainActivity.SwitchOpt3.setOnCheckedChangeListener(this);
        MainActivity.SwitchOpt4 = (Switch) findViewById(R.id.switch5);
        MainActivity.SwitchOpt4.setOnCheckedChangeListener(this);
        MainActivity.SwitchOpt5 = (Switch) findViewById(R.id.switch6);
        MainActivity.SwitchOpt5.setOnCheckedChangeListener(this);
        MainActivity.simpleSeekBar=(SeekBar)findViewById(R.id.simpleSeekBar);
        MainActivity.SensorId = (EditText) findViewById(R.id.editText);

        // perform seek bar change listener event used for getting the progress value
        MainActivity.simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MainActivity.frequency = progress+1;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                final SeekBar.OnSeekBarChangeListener context = this;
                Toast.makeText(options.this, "Enregistrement toute les :" + MainActivity.frequency + "s",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // getting the id sensor value (EditText)
        MainActivity.SensorId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    MainActivity.IDSensor = v.getText().toString();

                    return true; // Focus will do whatever you put in the logic.
                }
                return false;  // Focus will change according to the actionId
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(MainActivity.SwitchOpt1.isChecked()){
            MainActivity.graph.setVisibility(View.INVISIBLE);
            MainActivity.pm1.setVisibility(View.VISIBLE);
            MainActivity.pm2.setVisibility(View.VISIBLE);
            MainActivity.pm10.setVisibility(View.VISIBLE);
            MainActivity.textViewPM1.setVisibility(View.VISIBLE);
            MainActivity.textViewPM2.setVisibility(View.VISIBLE);
            MainActivity.textViewPM10.setVisibility(View.VISIBLE);
            MainActivity.textPM1.setVisibility(View.VISIBLE);
            MainActivity.textPM2.setVisibility(View.VISIBLE);
            MainActivity.textPM10.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = MainActivity.graph.getLayoutParams();
            params.height = 0;
            MainActivity.graph.setLayoutParams(params);

        }else{
            MainActivity.graph.setVisibility(View.VISIBLE);
            MainActivity.pm1.setVisibility(View.INVISIBLE);
            MainActivity.pm2.setVisibility(View.INVISIBLE);
            MainActivity.pm10.setVisibility(View.INVISIBLE);
            MainActivity.textViewPM1.setVisibility(View.INVISIBLE);
            MainActivity.textViewPM2.setVisibility(View.INVISIBLE);
            MainActivity.textViewPM10.setVisibility(View.INVISIBLE);
            MainActivity.textPM1.setVisibility(View.INVISIBLE);
            MainActivity.textPM2.setVisibility(View.INVISIBLE);
            MainActivity.textPM10.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = MainActivity.graph.getLayoutParams();
            params.height = 270*2;
            MainActivity.graph.setLayoutParams(params);
        }
        if(MainActivity.SwitchOpt4.isChecked()){
            MainActivity.mapFragment.getView().setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = MainActivity.mapFragment.getView().getLayoutParams();
            params.height = 256*2;
            MainActivity.mapFragment.getView().setLayoutParams(params);
        }
        else {
            MainActivity.mapFragment.getView().setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = MainActivity.mapFragment.getView().getLayoutParams();
            params.height = 0;
            MainActivity.mapFragment.getView().setLayoutParams(params);
        }
        if(MainActivity.SwitchOpt5.isChecked()){
            MainActivity.view.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = MainActivity.view.getLayoutParams();
            params.height = 320*2;
            MainActivity.view.setLayoutParams(params);
        }
        else {
            MainActivity.view.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = MainActivity.view.getLayoutParams();
            params.height = 0;
            MainActivity.view.setLayoutParams(params);
        }
    }


    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        SharedPreferences prefs = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
        MainActivity.SwitchOpt1.setChecked( prefs.getBoolean("chart",true));
        MainActivity.SwitchOpt2.setChecked( prefs.getBoolean("Temperature",true));
        MainActivity.SwitchOpt3.setChecked( prefs.getBoolean("Tracking",true));
        MainActivity.SwitchOpt4.setChecked( prefs.getBoolean("Maps",true));
        MainActivity.SwitchOpt5.setChecked( prefs.getBoolean("Atmo",true));
        MainActivity.simpleSeekBar.setProgress(prefs.getInt("Frequency", MainActivity.frequency));
        MainActivity.SensorId.setText(prefs.getString("IDSensor", MainActivity.IDSensor));
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        SharedPreferences.Editor editor =  getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("chart", MainActivity.SwitchOpt1.isChecked());
        editor.putBoolean("Temperature", MainActivity.SwitchOpt2.isChecked());
        editor.putBoolean("Tracking", MainActivity.SwitchOpt3.isChecked());
        editor.putBoolean("Maps", MainActivity.SwitchOpt4.isChecked());
        editor.putBoolean("Atmo", MainActivity.SwitchOpt5.isChecked());
        editor.putInt("Frequency",MainActivity.frequency);
        editor.putString("IDSensor",MainActivity.IDSensor);
        editor.commit();
    }

    @Override
    protected void onStop() {
        // call the superclass method first
        super.onStop();

    }



}


