package science.apolline;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Set;

import science.apolline.ioio.IOIOService;


////////////////////////////////////////////////////////////////////////////////////////////////////
public class MainActivity extends AppCompatActivity {

    static LineGraphSeries<DataPoint> series;
    static LineGraphSeries<DataPoint> series2;
    static LineGraphSeries<DataPoint> series10;
    static public GoogleMap Map = null;
    static public GraphView graph;

    static String tempview;
    static double Lat;
    static double Long;
    static double LatOld;
    static double LongOld;
    static int color;
    static final byte LENG = 31;   //0x42 + 31 bytes equal to 32 bytes
    static int[] buff = new int[64];
    static int PM01Value = 0;          //define PM1.0 value of the air detector module
    static int PM2_5Value = 0;         //define PM2.5 value of the air detector module
    static int PM10Value = 0;         //define PM10 value of the air detector module
    static int PM0_3Above = 0;
    static int PM0_5Above = 0;
    static int PM1Above = 0;
    static int PM2_5Above = 0;
    static int PM5Above = 0;
    static int PM10Above = 0;
    static int count = 0;
    static int newmaxY = 10;
    static MapFragment mapFragment;
    static ProgressBar pm1;
    static ProgressBar pm2;
    static ProgressBar pm10;
    static public TextView textViewPM1;
    static public TextView textViewPM2;
    static public TextView textViewPM10;
    static public TextView textPM1;
    static public TextView textPM2;
    static public TextView textPM10;
    static public Switch SwitchOpt1;
    static public Switch SwitchOpt2;
    static public Switch SwitchOpt3;
    static public Switch SwitchOpt4;
    static public Switch SwitchOpt5;
    static public final String MY_PREFS_NAME = "MyPrefsFile";
    static public SeekBar simpleSeekBar;
    static public int frequency;
    static public EditText SensorId;
    static public String IDSensor;
    static public WebView view;
    static public Button pieton;
    static public Button velo;
    static public Button voiture;
    static public Button other;
    static public String remarque;
    static public EditText Remark;

    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    private Set<BluetoothDevice> devices;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // garde l'écran allumé mais consomme batterie
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textView1 = (TextView) findViewById(R.id.textView);

        graph = (GraphView) findViewById(R.id.graph);
        initGraph(graph);
        pm1 = (ProgressBar) findViewById(R.id.progressBar3);

        textViewPM1 = (TextView) findViewById(R.id.textPM1);
        textPM1 = (TextView) findViewById(R.id.PM1);
        pm2 = (ProgressBar) findViewById(R.id.progressBar4);
        textViewPM2 = (TextView) findViewById(R.id.textPM2);
        textPM2 = (TextView) findViewById(R.id.PM2);
        pm10 = (ProgressBar) findViewById(R.id.progressBar5);
        textViewPM10 = (TextView) findViewById(R.id.textPM10);
        textPM10 = (TextView) findViewById(R.id.PM10);

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean("chart", true) == true) {
            graph.setVisibility(View.INVISIBLE);
            pm1.setVisibility(View.VISIBLE);
            pm2.setVisibility(View.VISIBLE);
            pm10.setVisibility(View.VISIBLE);
            textViewPM1.setVisibility(View.VISIBLE);
            textViewPM2.setVisibility(View.VISIBLE);
            textViewPM10.setVisibility(View.VISIBLE);
            textPM1.setVisibility(View.VISIBLE);
            textPM2.setVisibility(View.VISIBLE);
            textPM10.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = graph.getLayoutParams();
            params.height = 0;
            graph.setLayoutParams(params);
        } else {
            graph.setVisibility(View.VISIBLE);
            pm1.setVisibility(View.INVISIBLE);
            pm2.setVisibility(View.INVISIBLE);
            pm10.setVisibility(View.INVISIBLE);
            textViewPM1.setVisibility(View.INVISIBLE);
            textViewPM2.setVisibility(View.INVISIBLE);
            textViewPM10.setVisibility(View.INVISIBLE);
            textPM1.setVisibility(View.INVISIBLE);
            textPM2.setVisibility(View.INVISIBLE);
            textPM10.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = graph.getLayoutParams();
            params.height = 270 * 2;
            graph.setLayoutParams(params);
        }

        frequency = prefs.getInt("Frequency", frequency);
        IDSensor = prefs.getString("IDSensor", IDSensor);

        String url =
                "<iframe src='http://www.atmo-hdf.fr/index.php?option=com_atmo&view=widgets_indices&tmpl=widgets&agglos=13&auto=true&speed=4000'"
                        +
                        " scrolling='no' border='0' style='border: none;width:345px;height:380px'></iframe>";
        view = (WebView) this.findViewById(R.id.webView);
        view.getSettings().setJavaScriptEnabled(true);
        view.loadDataWithBaseURL("", url, "text/html", "UTF-8", "");

        if (prefs.getBoolean("Atmo", true) == true) {
            view.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = 320 * 2;
            view.setLayoutParams(params);
        } else {
            view.setVisibility(View.INVISIBLE);
            view.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = 0;
            view.setLayoutParams(params);
        }

        onMap();
        if (prefs.getBoolean("Maps", true) == true) {
            mapFragment.getView().setVisibility(View.VISIBLE);
            mapFragment.getView().setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
            params.height = 256 * 2;
            mapFragment.getView().setLayoutParams(params);
        } else {
            mapFragment.getView().setVisibility(View.INVISIBLE);
            mapFragment.getView().setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
            params.height = 0;
            mapFragment.getView().setLayoutParams(params);
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
        }

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Voulez-vous activer le GPS?", Toast.LENGTH_LONG).show();
            Intent i = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(i, 1);
        }

        pieton = (Button) findViewById(R.id.button2);
        velo = (Button) findViewById(R.id.button3);
        voiture = (Button) findViewById(R.id.button4);
        other = (Button) findViewById(R.id.button5);
        pieton.setOnClickListener(myhandler1);
        velo.setOnClickListener(myhandler2);
        voiture.setOnClickListener(myhandler3);
        other.setOnClickListener(myhandler4);

        if (prefs.getBoolean("pieton", true) == true) {
            pieton.setBackgroundResource(R.drawable.ic_directions_run_white_48dp);
            remarque = "pieton";
        } else {
            pieton.setBackgroundResource(R.drawable.ic_directions_run_black_48dp);
        }
        if (prefs.getBoolean("velo", true) == true) {
            velo.setBackgroundResource(R.drawable.ic_directions_bike_white_48dp);
            remarque = "velo";
        } else {
            velo.setBackgroundResource(R.drawable.ic_directions_bike_black_48dp);
        }
        if (prefs.getBoolean("voiture", true) == true) {
            voiture.setBackgroundResource(R.drawable.ic_directions_car_white_48dp);
            remarque = "voiture";
        } else {
            voiture.setBackgroundResource(R.drawable.ic_directions_car_black_48dp);
        }
        other.setBackgroundResource(R.drawable.ic_navigation_black_24dp);
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean("other", false);
        editor.commit();

        Remark = (EditText) findViewById(R.id.editText2);
        Remark.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    remarque = v.getText().toString();
                    other.setVisibility(View.VISIBLE);
                    Remark.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Votre remarque:" + remarque, Toast.LENGTH_LONG).show();
                    return true; // Focus will do whatever you put in the logic.
                }
                return false;  // Focus will change according to the actionId
            }
        });


        startService(new Intent(this, IOIOService.class));

    }


    View.OnClickListener myhandler1 = new View.OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "En marche!", Toast.LENGTH_LONG).show();
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            if (prefs.getBoolean("pieton", true) == false) {
                editor.putBoolean("pieton", true);
                editor.putBoolean("velo", false);
                editor.putBoolean("voiture", false);
                editor.putBoolean("other", false);
                pieton.setBackgroundResource(R.drawable.ic_directions_run_white_48dp);
                velo.setBackgroundResource(R.drawable.ic_directions_bike_black_48dp);
                voiture.setBackgroundResource(R.drawable.ic_directions_car_black_48dp);
                other.setBackgroundResource(R.drawable.ic_navigation_black_24dp);
                editor.commit();
                remarque = "pieton";
            }
        }
    };

    View.OnClickListener myhandler2 = new View.OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "Ca roule!", Toast.LENGTH_LONG).show();
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            if (prefs.getBoolean("velo", true) == false) {
                editor.putBoolean("velo", true);
                editor.putBoolean("pieton", false);
                editor.putBoolean("voiture", false);
                editor.putBoolean("other", false);
                velo.setBackgroundResource(R.drawable.ic_directions_bike_white_48dp);
                pieton.setBackgroundResource(R.drawable.ic_directions_run_black_48dp);
                voiture.setBackgroundResource(R.drawable.ic_directions_car_black_48dp);
                other.setBackgroundResource(R.drawable.ic_navigation_black_24dp);
                editor.commit();
                remarque = "velo";
            }
        }
    };

    View.OnClickListener myhandler3 = new View.OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "Pas de sport aujourd'hui?", Toast.LENGTH_LONG).show();
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            if (prefs.getBoolean("voiture", true) == false) {
                editor.putBoolean("voiture", true);
                editor.putBoolean("pieton", false);
                editor.putBoolean("velo", false);
                editor.putBoolean("other", false);
                voiture.setBackgroundResource(R.drawable.ic_directions_car_white_48dp);
                pieton.setBackgroundResource(R.drawable.ic_directions_run_black_48dp);
                velo.setBackgroundResource(R.drawable.ic_directions_bike_black_48dp);
                other.setBackgroundResource(R.drawable.ic_navigation_black_24dp);
                editor.commit();
                remarque = "voiture";
            }
        }
    };

    View.OnClickListener myhandler4 = new View.OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(getApplicationContext(), "Une remarque?", Toast.LENGTH_LONG).show();
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            if (prefs.getBoolean("other", true) == false) {
                editor.putBoolean("other", true);
                editor.putBoolean("pieton", false);
                editor.putBoolean("velo", false);
                editor.putBoolean("voiture", false);
                other.setBackgroundResource(R.drawable.ic_navigation_white_24dp);
                pieton.setBackgroundResource(R.drawable.ic_directions_run_black_48dp);
                velo.setBackgroundResource(R.drawable.ic_directions_bike_black_48dp);
                voiture.setBackgroundResource(R.drawable.ic_directions_car_black_48dp);
                editor.commit();
            }
            other.setVisibility(View.INVISIBLE);
            Remark.setVisibility(View.VISIBLE);
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    };

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //gère le click sur une action de l'ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options:
                Intent intent = new Intent(this, options.class);
                startActivity(intent);
                return true;
            case R.id.informations:
                Intent intent_2 = new Intent(this, informations.class);
                startActivity(intent_2);
                return true;
            case R.id.contact:
                Intent intent_3 = new Intent(this, contact.class);
                startActivity(intent_3);
                return true;
            case R.id.close:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                stopService(new Intent(this, IOIOService.class));
                final NotificationManager notificationManager = (NotificationManager) getSystemService(
                        Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(R.drawable.logoandroidpng);
                finish();
                return true;
            case R.id.refresh:
                startService(new Intent(this, IOIOService.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        stopService(new Intent(this, IOIOService.class));
        final NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(R.drawable.logoandroidpng);
        finish();
        super.onDestroy();
    }

    public void onMap() {
        // récupérer info GPS
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Map = googleMap;
                if (ActivityCompat
                        .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat
                        .checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Map.setMyLocationEnabled(true);
                LocationManager locationManager = (LocationManager) getSystemService(
                        Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                Location location = locationManager
                        .getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                if (location != null) {
                    Map.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(),
                                    location.getLongitude()))      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    Map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    Map.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(), location.getLongitude()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title("Start"));
                    LatOld = location.getLatitude();
                    LongOld = location.getLongitude();
                    Lat = location.getLatitude();
                    Long = location.getLongitude();
                }
            }
        });
    }

    //	@Override
    public void initGraph(GraphView graph) {
        // set date label formatter
        // enable scaling and scrolling
        graph.getViewport().setScalable(true);
        graph.getGridLabelRenderer().setLabelFormatter(
                new DateAsXAxisLabelFormatter(graph.getContext(), new SimpleDateFormat("HH:mm")));
        graph.getViewport().setScrollable(true);
        graph.getViewport().setScalableY(true);
        //	graph.getGridLabelRenderer().setNumVerticalLabels(5);
        graph.getGridLabelRenderer().setNumHorizontalLabels(5);
        graph.getGridLabelRenderer().setLabelVerticalWidth(45);
        // set manual x bounds to have nice steps
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10 * 60 * 1000); //10mn
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getViewport().setDrawBorder(true);

        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);

        //series
        series = new LineGraphSeries<DataPoint>();
        series2 = new LineGraphSeries<DataPoint>();
        series10 = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        graph.addSeries(series2);
        graph.addSeries(series10);

        //LEgend
        series.setTitle("PM1.0");
        series2.setTitle("PM2.5");
        series10.setTitle("PM10");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.BOTTOM);
        series.setColor(Color.BLUE);
        series.setDrawDataPoints(true);
        series.setThickness(7);
        series.setDataPointsRadius(8);
        series2.setColor(Color.GREEN);
        series2.setDrawDataPoints(true);
        series2.setThickness(7);
        series2.setDataPointsRadius(8);
        series10.setColor(Color.YELLOW);
        series10.setDrawDataPoints(true);
        series10.setThickness(7);
        series10.setDataPointsRadius(8);
        // as we use dates as labels, the human rounding to nice readable numbers
        // is not nessecary
        graph.getGridLabelRenderer().setHumanRounding(false);
        graph.getGridLabelRenderer().setPadding(15);

    }

    public void toast(final String message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}