package science.apolline.ioio;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import science.apolline.R;

public class IOIOFragment extends Fragment {

    static LineGraphSeries<DataPoint> series;
    static LineGraphSeries<DataPoint> series2;
    static LineGraphSeries<DataPoint> series10;

    private ProgressBar progressPM1;
    private ProgressBar progressPM2;
    private ProgressBar progressPM10;

    private TextView textViewPM1;
    private TextView textViewPM2;
    private TextView textViewPM10;

    private Button pieton;
    private Button velo;
    private Button voiture;
    private Button other;

    private MapFragment mapFragment;
    private GoogleMap map = null;

    private BroadcastReceiver BReceiver;

    private GraphView graph;

    public IOIOFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ioio,container,false);
        progressPM1 = view.findViewById(R.id.fragment_ioio_progress_pm1);
        textViewPM1 = view.findViewById(R.id.fragment_ioio_tv_pm1_value);
        progressPM2 = view.findViewById(R.id.fragment_ioio_progress_pm2);
        textViewPM2 = view.findViewById(R.id.fragment_ioio_tv_pm2_value);
        progressPM10 = view.findViewById(R.id.fragment_ioio_progress_pm10);
        textViewPM10 = view.findViewById(R.id.fragment_ioio_tv_pm10_value);
        graph = view.findViewById(R.id.fragment_ioio_graph);
        initGraph(graph);
//        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_ioio_map);
//        pieton = view.findViewById(R.id.fragment_ioio_pieton);
//        velo = view.findViewById(R.id.fragment_ioio_velo);
//        voiture = view.findViewById(R.id.fragment_ioio_voiture);
//        other = view.findViewById(R.id.fragment_ioio_other);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActivity().startService(new Intent(getActivity(), IOIOService.class));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

        BReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle b = intent.getBundleExtra("dataBundle");

                Log.e("receiver","PM01Value : "+b.getInt("PM01Value"));

                progressPM1.setProgress(b.getInt("PM01Value"));
                progressPM2.setProgress(b.getInt("PM2_5Value"));
                progressPM10.setProgress(b.getInt("PM10Value"));

                textViewPM1.setText(b.getInt("PM01Value")+"");
                textViewPM2.setText(b.getInt("PM2_5Value")+"");
                textViewPM10.setText(b.getInt("PM10Value")+"");
                Calendar c = Calendar.getInstance();
                Date d1 = c.getTime();

                int nbPoint = 10 * 60; // 10 min * 60 second, 1 point per second

                series.appendData(new DataPoint(d1,b.getInt("PM01Value")),true,nbPoint);
                series2.appendData(new DataPoint(d1,b.getInt("PM2_5Value")),true,nbPoint);
                series10.appendData(new DataPoint(d1,b.getInt("PM10Value")),true,nbPoint);

            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(BReceiver, new IntentFilter("IOIOdata"));
    }

    public void initGraph(GraphView graph) {

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(),
                DateFormat.getTimeInstance(DateFormat.SHORT)));

        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScrollableY(true); // enables vertical scrolling

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        graph.getGridLabelRenderer().setNumHorizontalLabels(9);
        graph.getGridLabelRenderer().setNumVerticalLabels(5);
        graph.getGridLabelRenderer().setLabelVerticalWidth(45);

        Calendar calendar = Calendar.getInstance();
        Date d1 = calendar.getTime();
        graph.getViewport().setMinX(d1.getTime());

        calendar.add(Calendar.MINUTE,10);
        d1=calendar.getTime();
        graph.getViewport().setMaxX(d1.getTime());

        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(100);

        graph.getViewport().setDrawBorder(true);

        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.BLACK);

        //series
        series = new LineGraphSeries<>();
        series2 = new LineGraphSeries<>();
        series10 = new LineGraphSeries<>();
        graph.addSeries(series);
        graph.addSeries(series2);
        graph.addSeries(series10);

        //LEgend
        series.setTitle("PM1.0");
        series2.setTitle("PM2.5");
        series10.setTitle("PM10");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

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
        //graph.getGridLabelRenderer().setPadding(15);
    }
}
