package science.apolline;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jjoe64.graphview.series.DataPoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;


public class IOIOService extends ioio.lib.util.android.IOIOService {

    String symb;
    float temperature;
    boolean led = true;
    int PM10Val;
    double RHT;
    int delay = 0;
    int counter = 30;

    @Override
    protected IOIOLooper createIOIOLooper() {
        return new BaseIOIOLooper() {
            private DigitalOutput led_;
            private Uart uart_ = null;
            private InputStream uartIn_ = null;
            private AnalogInput input;
            private AnalogInput inputHum;

            @Override
            protected void setup() throws ConnectionLostException,
                    InterruptedException {

                led_ = ioio_.openDigitalOutput(0, true);
                uart_ = ioio_.openUart(7, 6, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
                //	uartOut_ = uart_.getOutputStream();
                uartIn_ = uart_.getInputStream();
                input = ioio_.openAnalogInput(44);
                inputHum = ioio_.openAnalogInput(42);
            }

            @Override
            public void loop() throws ConnectionLostException,
                    InterruptedException {
                try {
                    int availableCount = this.uartIn_.available();
                    if (availableCount > 0) {
                        byte[] buffer = new byte[availableCount];
                        int col;
                        this.uartIn_.read(buffer);
                        for (int b : buffer) {
                            switch (b) {
                                case 0x42:
                                    MainActivity.count = 0;
                                    break;

                                default:
                                    MainActivity.buff[MainActivity.count] = b & 0xff;
                                    MainActivity.count++;
                                    if (MainActivity.count == MainActivity.LENG) {
                                        MainActivity.count = 0;
                                        if (checkValue(MainActivity.buff, MainActivity.LENG)) {
                                            led = !led;
                                            led_.write(led);

                                            MainActivity.PM01Value = transmitPM01(MainActivity.buff); //count PM1.0 value of the air detector module
                                            MainActivity.PM2_5Value = transmitPM2_5(MainActivity.buff); //count PM2.5 value of the air detector module
                                            MainActivity.PM10Value = transmitPM10(MainActivity.buff); //count PM10 value of the air detector module
                                            MainActivity.PM0_3Above = transmitPMAbove0_3(MainActivity.buff);
                                            MainActivity.PM0_5Above = transmitPMAbove0_5(MainActivity.buff);
                                            MainActivity.PM1Above = transmitPMAbove1(MainActivity.buff);
                                            MainActivity.PM2_5Above = transmitPMAbove2_5(MainActivity.buff);
                                            MainActivity.PM5Above = transmitPMAbove5(MainActivity.buff);
                                            MainActivity.PM10Above = transmitPMAbove10(MainActivity.buff);

                                            MainActivity.newmaxY = (int) MainActivity.graph.getViewport().getMaxY(true);
                                            if (MainActivity.newmaxY < MainActivity.PM01Value)
                                                MainActivity.newmaxY = MainActivity.PM01Value;
                                            MainActivity.graph.getViewport().setMaxY(MainActivity.newmaxY);
                                            if (MainActivity.newmaxY < MainActivity.PM2_5Value)
                                                MainActivity.newmaxY = MainActivity.PM2_5Value;
                                            MainActivity.graph.getViewport().setMaxY(MainActivity.newmaxY);
                                            if (MainActivity.newmaxY < MainActivity.PM10Value)
                                                MainActivity.newmaxY = MainActivity.PM10Value;
                                            MainActivity.graph.getViewport().setMaxY(MainActivity.newmaxY);

                                            // generate Dates
                                            Calendar calendar = Calendar.getInstance();
                                            Date d1 = calendar.getTime();

                                            delay++;
                                            if (delay >= MainActivity.frequency) {
                                                // you can directly pass Date objects to DataPoint-Constructor
                                                // this will convert the Date to double via Date#getTime()
                                                MainActivity.series.appendData(new DataPoint(d1, MainActivity.PM01Value), false, 10000);
                                                MainActivity.series2.appendData(new DataPoint(d1, MainActivity.PM2_5Value), false, 10000);
                                                MainActivity.series10.appendData(new DataPoint(d1, MainActivity.PM10Value), true, 10000);

                                                if (MainActivity.PM10Value <= 15) {
                                                    col = MainActivity.PM10Value * 17;
                                                    MainActivity.color = Color.rgb(col, 0xFF, 0);
                                                } else if (MainActivity.PM10Value <= 30) {
                                                    col = 0xFF - ((MainActivity.PM10Value - 15) * 17);
                                                    MainActivity.color = Color.rgb(0xFF, col, 0);
                                                } else
                                                    MainActivity.color = Color.rgb(0xFF, 0, 0);

                                                //Bar
                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    public void run() {
                                                        MainActivity.pm1.setProgress(MainActivity.PM01Value);
                                                        MainActivity.pm2.setProgress(MainActivity.PM2_5Value);
                                                        MainActivity.pm10.setProgress(MainActivity.PM10Value);
                                                        MainActivity.textViewPM1.setTextColor(MainActivity.color);
                                                        MainActivity.textViewPM2.setTextColor(MainActivity.color);
                                                        MainActivity.textViewPM10.setTextColor(MainActivity.color);
                                                        if (MainActivity.PM01Value < 100)
                                                            MainActivity.textViewPM1.setText(Integer.toString(MainActivity.PM01Value));
                                                        else
                                                            MainActivity.textViewPM1.setText("/!\\");
                                                        if (MainActivity.PM2_5Value < 100)
                                                            MainActivity.textViewPM2.setText(Integer.toString(MainActivity.PM2_5Value));
                                                        else
                                                            MainActivity.textViewPM2.setText("/!\\");
                                                        if (MainActivity.PM10Value < 100)
                                                            MainActivity.textViewPM10.setText(Integer.toString(MainActivity.PM10Value));
                                                        else {
                                                            MainActivity.textViewPM10.setText("/!\\");

                                                            // Service starting. Create a notification.
                                                            Notification.Builder builder = new Notification.Builder(IOIOService.this);
                                                            Intent notificationIntent = new Intent(IOIOService.this, MainActivity.class);
                                                            PendingIntent pendingIntent = PendingIntent.getActivity(IOIOService.this, 0, notificationIntent, 0);
                                                            builder.setSmallIcon(R.drawable.logoandroidpng)
                                                                    .setContentTitle("PM Datalogger Running")
                                                                    .setContentText("/!\\ Air pollution Warning")
                                                                    .setContentIntent(pendingIntent);
                                                            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                                            Notification notification = builder.getNotification();
//                                                            notification.vibrate = new long[]{0, 200, 0};
                                                            notificationManager.notify(R.drawable.logoandroidpng, notification);
                                                        }
                                                    }
                                                });
                                            }

                                            // récupérer info GPS
                                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                            Criteria criteria = new Criteria();
                                            if (ActivityCompat.checkSelfPermission(IOIOService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(IOIOService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                // TODO: Consider calling
                                                //    ActivityCompat#requestPermissions
                                                // here to request the missing permissions, and then overriding
                                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                //                                          int[] grantResults)
                                                // to handle the case where the user grants the permission. See the documentation
                                                // for ActivityCompat#requestPermissions for more details.
                                                return;
                                            }
                                            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                                            if (location != null) {
                                                MainActivity.Lat = location.getLatitude();
                                                MainActivity.Long = location.getLongitude();
                                            }

                                            // suivi GPS
                                            if (delay >= MainActivity.frequency) {
                                                Handler UIHandler = new Handler(Looper.getMainLooper());
                                                UIHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        MainActivity.mapFragment.getMapAsync(new OnMapReadyCallback() {
                                                            @Override
                                                            public void onMapReady(GoogleMap googleMap) {
                                                                if (googleMap != null) {
                                                                    Polyline line = MainActivity.Map.addPolyline(new PolylineOptions()
                                                                            .add(new LatLng(MainActivity.LatOld, MainActivity.LongOld),
                                                                                    new LatLng(MainActivity.Lat, MainActivity.Long))
                                                                            .width(6)
                                                                            .color(MainActivity.color));
                                                                }
                                                                if (MainActivity.Lat != MainActivity.LatOld)
                                                                    MainActivity.LatOld = MainActivity.Lat;
                                                                if (MainActivity.Long != MainActivity.LongOld)
                                                                    MainActivity.LongOld = MainActivity.Long;
                                                                //suivi automatique
                                                                SharedPreferences prefs = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                                                                if (prefs.getBoolean("Tracking", true) == true) {
                                                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(MainActivity.Lat, MainActivity.Long)));
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                            //Température & Hum
                                            //	if (counter > 5) {
                                            float tension = input.getVoltage();
                                            SharedPreferences prefs = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                                            if (prefs.getBoolean("Temperature", true) == true) {
                                                temperature = tension * 100;
                                                symb = "K";
                                            } else {
                                                temperature = (float) ((tension * 100) - 273.15);
                                                symb = "°C";
                                            }

                                            float tensionHum = inputHum.getVoltage();
                                            float RH = (float) (((tensionHum / 3.3) - 0.1515) / 0.0636); //pour avoir l'humidité à 25°C
                                            RHT = (RH / (1.0546 - 0.00216 * ((tension * 100) - 273.15))) * 10; //compensé en t°

                                            counter = 0;
                                            MainActivity.tempview = "T°:" + String.format("%.02f", temperature) + symb + "     Hum:" + String.format("%.02f", RHT) + "%";
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                public void run() {
                                                    MainActivity.textView1.setText(MainActivity.tempview);
                                                }
                                            });
                                            //	}
                                            //	counter++;

                                            if (delay >= MainActivity.frequency) {
                                                delay = 0;

                                                // écrire dans un fichier
                                                SimpleDateFormat DateFormat = new SimpleDateFormat("yy_MM_dd_");
                                                String date = DateFormat.format(d1);
                                                File myFile = new File(Environment.getExternalStorageDirectory() +
                                                        File.separator + "data", date + "Dust_sensor_" + MainActivity.IDSensor + ".txt"); //on déclare notre futur fichier

                                                File myDir = new File(Environment.getExternalStorageDirectory() +
                                                        File.separator + "data"); //pour créer le repertoire dans lequel on va mettre notre fichier
                                                Boolean success = true;
                                                if (!myDir.exists()) {
                                                    success = myDir.mkdir(); //On crée le répertoire (s'il n'existe pas!!)
                                                }

                                                if (success) {
                                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss\t");
                                                    String data = "";
                                                    if (myFile.exists() == false) {
                                                        data += "#Fichier des données brutes issues du capteur de poussières SEN0177\r\n#Format:\r\n#AA-MM-JJ hh:mm:ss\tPM1.0;PM2.5;PM10(ug/m3)\tAbove PM0.3;PM0.5;PM1;PM2.5;PM5;PM10(ug/m3)\tLatitude;Longitude;Température;Humidité\tRemarques\r\n";
                                                    }

                                                    data += simpleDateFormat.format(d1);
                                                    data += Integer.toString(MainActivity.PM01Value) + ";" + Integer.toString(MainActivity.PM2_5Value) + ";" + Integer.toString(MainActivity.PM10Value) + "\t";
                                                    data += Integer.toString(MainActivity.PM0_3Above) + ";" + Integer.toString(MainActivity.PM0_5Above) + ";" + Integer.toString(MainActivity.PM1Above) + ";" + Integer.toString(MainActivity.PM2_5Above) + ";" + Integer.toString(MainActivity.PM5Above) + ";" + Integer.toString(MainActivity.PM10Above) + "\t";
                                                    data += String.format("%.05f", MainActivity.Lat) + ";" + String.format("%.05f", MainActivity.Long) + ";" + String.format("%.02f", temperature) + ";" + String.format("%.02f", RHT) + "\t";
                                                    data += MainActivity.remarque + "\r\n";

                                                    FileOutputStream output = new FileOutputStream(myFile, true); //le true est pour écrire en fin de fichier, et non l'écraser
                                                    output.write(data.getBytes());
                                                } else {
                                                    Log.e("écriture fichier", "ERROR DE CREATION DE DOSSIER");
                                                }
                                            }
                                        }
                                    }
                            }
                        }
                    }
                } catch (IOException ioe) {
                    Log.e("Communication", "An IOException happened");
                }
            }
        };
    }

    /* PM function*/
    public boolean checkValue(int[] thebuf, byte leng) {
        boolean receiveflag = false;
        int receiveSum = 0;

        for (int i = 0; i < (leng - 2); i++) {
            receiveSum = receiveSum + thebuf[i];
        }
        receiveSum = receiveSum + 0x42;

        if (receiveSum == ((thebuf[leng - 2] << 8) + thebuf[leng - 1])) //check the serial data
        {
            receiveSum = 0;
            receiveflag = true;
        }
        return receiveflag;
    }

    //transmit PM Value
    int transmitPM01(int[] thebuf) {
        int PM01Val;
        PM01Val = ((thebuf[3] << 8) + thebuf[4]); //count PM1.0 value of the air detector module
        return PM01Val;
    }

    //transmit PM Value
    int transmitPM2_5(int[] thebuf) {
        int PM2_5Val;
        PM2_5Val = ((thebuf[5] << 8) + thebuf[6]); //count PM2.5 value of the air detector module
        return PM2_5Val;
    }

    //transmit PM Value
    int transmitPM10(int[] thebuf) {
        PM10Val = ((thebuf[7] << 8) + thebuf[8]); //count PM10 value of the air detector module
        return PM10Val;
    }

    //transmit Above PM Value
    int transmitPMAbove0_3(int[] thebuf) {
        PM10Val = ((thebuf[15] << 8) + thebuf[16]);
        return PM10Val;
    }

    //transmit Above PM Value
    int transmitPMAbove0_5(int[] thebuf) {
        PM10Val = ((thebuf[17] << 8) + thebuf[18]);
        return PM10Val;
    }

    //transmit Above PM Value
    int transmitPMAbove1(int[] thebuf) {
        PM10Val = ((thebuf[19] << 8) + thebuf[20]);
        return PM10Val;
    }

    //transmit Above PM Value
    int transmitPMAbove2_5(int[] thebuf) {
        PM10Val = ((thebuf[21] << 8) + thebuf[22]);
        return PM10Val;
    }

    //transmit Above PM Value
    int transmitPMAbove5(int[] thebuf) {
        PM10Val = ((thebuf[23] << 8) + thebuf[24]);
        return PM10Val;
    }

    //transmit Above PM Value
    int transmitPMAbove10(int[] thebuf) {
        PM10Val = ((thebuf[25] << 8) + thebuf[26]);
        return PM10Val;
    }

    /******************************************************/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (intent != null && intent.getAction() != null
                && intent.getAction().equals("stop")) {
            // User clicked the notification. Need to stop the service.
            nm.cancel(0);
            stopSelf();
        } else {
            // Service starting. Create a notification.
            Notification.Builder builder = new Notification.Builder(this);
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            builder.setSmallIcon(R.drawable.logoandroidpng)
                    .setContentTitle("PM Datalogger Running")
                    .setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification = builder.getNotification();
            notificationManager.notify(R.drawable.logoandroidpng, notification);
        }
        return result;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}
