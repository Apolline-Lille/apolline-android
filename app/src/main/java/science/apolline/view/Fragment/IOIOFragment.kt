package science.apolline.view.Fragment

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import science.apolline.R
import science.apolline.models.IOIOData
import science.apolline.service.sensor.IOIOService
import science.apolline.utils.*
import science.apolline.viewModel.SensorViewModel
import java.util.*


class IOIOFragment : Fragment(), LifecycleOwner, OnChartValueSelectedListener, AnkoLogger {


    private var progressPM1: ProgressBar? = null
    private var progressPM2: ProgressBar? = null
    private var progressPM10: ProgressBar? = null

    private var textViewPM1: TextView? = null
    private var textViewPM2: TextView? = null
    private var textViewPM10: TextView? = null

    private var save_fab: FloatingActionButton? = null

    private val pieton: Button? = null
    private val velo: Button? = null
    private val voiture: Button? = null
    private val other: Button? = null

    private val mapFragment: MapFragment? = null
    private val map: GoogleMap? = null

    private var mChart: LineChart? = null
    private var referenceTimestamp: Long = 0  // minimum timestamp in your data set
    private val export = DataExport()

    private lateinit var dataList: List<ILineDataSet>

    private lateinit var disposable: CompositeDisposable

    private lateinit var viewModel: SensorViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ioio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disposable = CompositeDisposable()
        viewModel = SensorViewModel(activity!!.application)
        progressPM1 = view.findViewById(R.id.fragment_ioio_progress_pm1)
        textViewPM1 = view.findViewById(R.id.fragment_ioio_tv_pm1_value)
        progressPM2 = view.findViewById(R.id.fragment_ioio_progress_pm2)
        textViewPM2 = view.findViewById(R.id.fragment_ioio_tv_pm2_value)
        progressPM10 = view.findViewById(R.id.fragment_ioio_progress_pm10)
        textViewPM10 = view.findViewById(R.id.fragment_ioio_tv_pm10_value)
        mChart = view.findViewById(R.id.chart1)
        save_fab = view.findViewById(R.id.fab_save)
        save_fab!!.setOnClickListener {
            export.exportToCsv(activity!!.application)
        }
        //        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_ioio_map);
        //        pieton = view.findViewById(R.id.fragment_ioio_pieton);
        //        velo = view.findViewById(R.id.fragment_ioio_velo);
        //        voiture = view.findViewById(R.id.fragment_ioio_voiture);
        //        other = view.findViewById(R.id.fragment_ioio_other);

        dataList = createMultiSet()
        initGraph()
    }

    //init graph on create view
    private fun initGraph() {

        referenceTimestamp = System.currentTimeMillis() / 1000
        val marker = CustomMarkerView(context!!, R.layout.graph_custom_marker, referenceTimestamp)
        mChart!!.marker = marker

        // LineTimeChart
        mChart!!.setOnChartValueSelectedListener(this)
        // enable description text
        mChart!!.description.isEnabled = false


        mChart!!.dragDecelerationFrictionCoef = 0.9f
        mChart!!.isHighlightPerDragEnabled = true
        // set an alternative background color
        //        mChart.setBackgroundColor(Color.WHITE);
        //        mChart.setViewPortOffsets(0f, 0f, 0f, 0f);

        // enable touch gestures
        mChart!!.setTouchEnabled(true)
        // enable scaling and dragging
        mChart!!.isDragEnabled = true
        mChart!!.setScaleEnabled(true)
        mChart!!.setDrawGridBackground(false)
        // if disabled, scaling can be done on x- and y-axis separately
        mChart!!.setPinchZoom(false)
        // set an alternative background color
        mChart!!.setBackgroundColor(Color.TRANSPARENT)


        val data = LineData()
        data.setValueTextColor(Color.WHITE)
        // add empty data
        mChart!!.data = data

        mChart!!.invalidate()

        //        // get the legend (only possible after setting data)
        val l = mChart!!.legend
        // modify the legend ...
        l.form = Legend.LegendForm.LINE
        l.typeface = Typeface.DEFAULT
        l.textColor = Color.WHITE
        //        Legend l = mChart.getLegend();
        //        l.setEnabled(false);

        val xl = mChart!!.xAxis
        xl.typeface = Typeface.DEFAULT
        xl.textColor = Color.BLACK
        xl.setDrawGridLines(false)
        xl.setAvoidFirstLastClipping(true)
        xl.isEnabled = true
        xl.setCenterAxisLabels(true)
        xl.granularity = 1f // one hour
        xl.position = XAxis.XAxisPosition.TOP_INSIDE

        val xAxisFormatter = HourAxisValueFormatter(referenceTimestamp)
        xl.valueFormatter = xAxisFormatter

        val leftAxis = mChart!!.axisLeft
        leftAxis.typeface = Typeface.DEFAULT
        leftAxis.textColor = Color.BLACK
        leftAxis.axisMaximum = 2500f
        leftAxis.axisMinimum = 0f
        //        leftAxis.setSpaceTop(80);
        //        leftAxis.setSpaceBottom(20);

        leftAxis.setDrawGridLines(true)

        val rightAxis = mChart!!.axisRight
        rightAxis.isEnabled = false

    }

    // Add new points to graph
    private fun addEntry(dataDisplay: IntArray) {
        val data = mChart!!.data

        if (data != null) {
            if (data.dataSetCount != 3)
                for (temp in dataList) {
                    data.addDataSet(temp)
                }
        }
        val now = System.currentTimeMillis() / 1000
        data!!.addEntry(Entry((now - referenceTimestamp).toFloat(), dataDisplay[0].toFloat()), 0)
        data.addEntry(Entry((now - referenceTimestamp).toFloat(), dataDisplay[1].toFloat()), 1)
        data.addEntry(Entry((now - referenceTimestamp).toFloat(), dataDisplay[2].toFloat()), 2)


        data.dataSets.forEach {

            if (it.entryCount >= MAX_VISIBLE_ENTRIES) {
                it.removeFirst()
            }
        }

        data.notifyDataChanged()
        // let the chart know it's data has changed
        mChart!!.notifyDataSetChanged()
        // invalidate data in case of data removal du to max entryCount value
        mChart!!.invalidate()
        // limit the number of visible entries
        mChart!!.setVisibleXRangeMaximum(5f)
        // Sets the size of the area (range on the y-axis) that should be maximum visible at once
        mChart!!.setVisibleYRangeMaximum(100f, YAxis.AxisDependency.LEFT)
        // mChart.setVisibleYRange(30, AxisDependency.LEFT);
        // move to the latest entry
        mChart!!.moveViewToX(data.entryCount.toFloat())
        // this automatically refreshes the chart (calls invalidate())
        //             mChart.moveViewTo(data.getEntryCount() -7, 55f,
        //             YAxis.AxisDependency.LEFT);

    }


    //Graph init
    private fun createMultiSet(): ArrayList<ILineDataSet> {

        val dataSets = ArrayList<ILineDataSet>()

        val setPM1 = LineDataSet(null, "PM1")
        setPM1.axisDependency = YAxis.AxisDependency.LEFT
        setPM1.color = Color.BLUE
        setPM1.setCircleColor(Color.BLUE)
        setPM1.lineWidth = 2f
        setPM1.circleRadius = 4f
        setPM1.fillAlpha = 65
        setPM1.fillColor = Color.BLUE
        setPM1.highLightColor = Color.rgb(244, 117, 117)
        setPM1.valueTextColor = Color.BLUE
        setPM1.valueTextSize = 9f
        setPM1.setDrawValues(false)


        val setPM2 = LineDataSet(null, "PM2")
        setPM2.axisDependency = YAxis.AxisDependency.LEFT
        setPM2.color = Color.GREEN
        setPM2.setCircleColor(Color.GREEN)
        setPM2.lineWidth = 2f
        setPM2.circleRadius = 4f
        setPM2.fillAlpha = 65
        setPM2.fillColor = Color.GREEN
        setPM2.highLightColor = Color.rgb(244, 117, 117)
        setPM2.valueTextColor = Color.GREEN
        setPM2.valueTextSize = 9f
        setPM2.setDrawValues(false)

        val setPM10 = LineDataSet(null, "PM10")
        setPM10.axisDependency = YAxis.AxisDependency.LEFT
        setPM10.color = Color.YELLOW
        setPM10.setCircleColor(Color.YELLOW)
        setPM10.lineWidth = 2f
        setPM10.circleRadius = 4f
        setPM10.fillAlpha = 65
        setPM10.fillColor = Color.YELLOW
        setPM10.highLightColor = Color.rgb(244, 117, 117)
        setPM10.valueTextColor = Color.YELLOW
        setPM10.valueTextSize = 9f
        setPM10.setDrawValues(false)

        dataSets.add(setPM1)
        dataSets.add(setPM2)
        dataSets.add(setPM10)

        return dataSets
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity!!.startService(Intent(activity, IOIOService::class.java))
    }

    override fun onValueSelected(e: Entry, h: Highlight) {
    }

    override fun onNothingSelected() {
    }


    override fun onStart() {
        super.onStart()
        disposable.add(viewModel.deviceListObserver
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {

                    if (it.isNotEmpty()) {
                        doAsync {
                            val device = it.first()
                            //info(device.toString())
                            val gson = GsonBuilder().registerTypeAdapter(IOIOData::class.java, DataDeserializer()).create()
                            val data = gson.fromJson(device.data, IOIOData::class.java)

                            val PM01Value = data!!.pm01Value
                            val PM2_5Value = data.pm2_5Value
                            val PM10Value = data.pm10Value

                            progressPM1!!.progress = PM01Value
                            progressPM2!!.progress = PM2_5Value
                            progressPM10!!.progress = PM10Value
                            textViewPM1!!.text = "$PM01Value"
                            textViewPM2!!.text = "$PM2_5Value"
                            textViewPM10!!.text = "$PM10Value"
                            val dataToDisplay = intArrayOf(PM01Value, PM2_5Value, PM10Value)
                            addEntry(dataToDisplay)
                        }

                    } else {
                        progressPM1!!.progress = 0
                        progressPM2!!.progress = 0
                        progressPM10!!.progress = 0
                        textViewPM1!!.text = "-1"
                        textViewPM2!!.text = "-1"
                        textViewPM10!!.text = "-1"
                    }
                })
    }

    override fun onStop() {
        super.onStop()
        if (!disposable.isDisposed) {
            disposable.clear()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
    }

    companion object {
        //TODO : allow user to change that value
        private const val MAX_VISIBLE_ENTRIES: Int = 15
    }
}


