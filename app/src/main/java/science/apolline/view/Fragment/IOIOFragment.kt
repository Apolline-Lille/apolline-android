package science.apolline.view.Fragment

import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.grabner.circleprogress.TextMode
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.jobs.MoveViewJob
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_ioio.*
import kotlinx.android.synthetic.main.fragment_ioio_content.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import science.apolline.R
import science.apolline.models.IOIOData
import science.apolline.service.sensor.IOIOService
import science.apolline.utils.*
import science.apolline.utils.DataExport.exportShareCsv
import science.apolline.utils.DataExport.exportToJson
import science.apolline.utils.DataExport.exportToCsv
import science.apolline.viewModel.SensorViewModel


class IOIOFragment : Fragment(), LifecycleOwner, OnChartValueSelectedListener, AnkoLogger {


    private var referenceTimestamp: Long = MIN_TIME_STAMP

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

        floating_action_menu_json.setOnClickListener {
            exportToJson(activity!!.application)
        }
        floating_action_menu_csv.setOnClickListener {
            exportToCsv(activity!!.application)
        }
        floating_action_menu_share.setOnClickListener {
            exportShareCsv(activity!!.application)
        }

        fragment_ioio_progress_pm1.setTextMode(TextMode.VALUE)
        fragment_ioio_progress_pm2.setTextMode(TextMode.VALUE)
        fragment_ioio_progress_pm10.setTextMode(TextMode.VALUE)

        createGraphMultiSets()
        setupGraphView()

    }


    private fun createGraphMultiSets(){

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


        val setPM2 = LineDataSet(null, "PM2.5")
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
        setPM10.color = Color.RED
        setPM10.setCircleColor(Color.RED)
        setPM10.lineWidth = 2f
        setPM10.circleRadius = 4f
        setPM10.fillAlpha = 65
        setPM10.fillColor = Color.RED
        setPM10.highLightColor = Color.rgb(244, 117, 117)
        setPM10.valueTextColor = Color.RED
        setPM10.valueTextSize = 9f
        setPM10.setDrawValues(false)

        dataList = listOf(setPM1,setPM2,setPM10)

    }


    private fun setupGraphView() {

        referenceTimestamp = System.currentTimeMillis() / 1000

        val marker = CustomMarkerView(context!!, R.layout.graph_custom_marker, referenceTimestamp)
        chart.marker = marker

        // LineTimeChart
        chart.setOnChartValueSelectedListener(this)
        // enable description text
        chart.description.isEnabled = false


        chart.dragDecelerationFrictionCoef = 0.9f
        chart.isHighlightPerDragEnabled = true

        // set an alternative background color
        //        chart.setBackgroundColor(Color.WHITE);
        //        chart.setViewPortOffsets(0f, 0f, 0f, 0f);

        // enable touch gestures
        chart.setTouchEnabled(true)
        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true)
        // set an alternative background color
        chart.setBackgroundColor(Color.TRANSPARENT)


        val data = LineData()
        data.setValueTextColor(Color.WHITE)
        // add empty data
        chart.data = data

        chart.invalidate()

        // get the legend (only possible after setting data)
        val l = chart.legend
        // modify the legend ...
        l.form = Legend.LegendForm.LINE
        l.typeface = Typeface.DEFAULT
        l.textColor = Color.BLACK
        //        Legend l = chart.getLegend();
        //        l.setEnabled(false);

        val xl = chart.xAxis
        xl.typeface = Typeface.DEFAULT
        xl.textColor = Color.BLACK
        xl.setDrawGridLines(false)
        xl.setAvoidFirstLastClipping(true)
        xl.isEnabled = true
        xl.setCenterAxisLabels(true)
        xl.granularity = 1f // one hour
        xl.position = XAxis.XAxisPosition.BOTTOM

        val xAxisFormatter = HourAxisValueFormatter(referenceTimestamp)
        xl.valueFormatter = xAxisFormatter

        val leftAxis = chart.axisLeft
        leftAxis.typeface = Typeface.DEFAULT
        leftAxis.textColor = Color.BLACK
        leftAxis.axisMaximum = MAX_Y_AXIS
        leftAxis.axisMinimum = MIN_Y_AXIS
        //        leftAxis.setSpaceTop(80);
        //        leftAxis.setSpaceBottom(20);

        leftAxis.setDrawGridLines(true)

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

    }


    // Add new points to graph
    private fun addGraphEntry(dataDisplay: IntArray) {
        val data = chart.data

        if (data != null) {
            if (data.dataSetCount != 3)
                for (temp in dataList) {
                    data.addDataSet(temp)
                }

            val now = System.currentTimeMillis() / 1000
            data.addEntry(Entry((now - referenceTimestamp).toFloat(), dataDisplay[0].toFloat()), 0)
            data.addEntry(Entry((now - referenceTimestamp).toFloat(), dataDisplay[1].toFloat()), 1)
            data.addEntry(Entry((now - referenceTimestamp).toFloat(), dataDisplay[2].toFloat()), 2)


            data.dataSets.forEach {
                if (it.entryCount >= MAX_VISIBLE_ENTRIES) {
                    info("TEST")
                    for (i in 0 until MAX_REMOVED_ENTRIES) {
                        it.removeEntry(i)
                    }

                }
            }

            data.notifyDataChanged()
            // let the chart know it's data has changed
            chart.notifyDataSetChanged()
            // invalidate data in case of data removal du to max entryCount value
            chart.invalidate()

        }

        // limit the number of visible entries
        chart.setVisibleXRangeMaximum(MAX_X_RANGE)
        // Sets the size of the area (range on the y-axis) that should be maximum visible at once
        chart.setVisibleYRangeMaximum(MAX_Y_RANGE, YAxis.AxisDependency.LEFT)
        // chart.setVisibleYRange(30, AxisDependency.LEFT);
        // move to the latest entry

        //chart.moveViewToX(data.entryCount.toFloat())
        // this automatically refreshes the chart (calls invalidate())

        val count = chart.data.getDataSetByIndex(1).entryCount
        val entry = chart.data.getDataSetByIndex(1).getEntryForIndex(count-1)

        //chart.moveViewToAnimated(entry.x, entry.y, YAxis.AxisDependency.LEFT,500)
        chart.moveViewTo(entry.x, entry.y, YAxis.AxisDependency.LEFT)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity!!.startService(Intent(activity, IOIOService::class.java))
        info("onAttach")
    }

    override fun onDetach() {
        super.onDetach()
        info("onDetach")
    }

    override fun onValueSelected(e: Entry, h: Highlight) {
        chart.setDrawMarkers(true)
    }

    override fun onNothingSelected() {
    }

    override fun onStart() {
        super.onStart()
        chart.fitScreen()
        disposable.add(viewModel.deviceListObserver
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {

                    if (it.isNotEmpty()) {
                        val device = it.first()

                        val gsonBuilder = GsonBuilder().registerTypeAdapter(IOIOData::class.java, DataDeserializer()).create()
                        val data = gsonBuilder.fromJson(device.data, IOIOData::class.java)

                        val pm01 = data!!.pm01Value
                        val pm25 = data.pm2_5Value
                        val pm10 = data.pm10Value

                        fragment_ioio_progress_pm1.setValueAnimated(pm01.toFloat())
                        fragment_ioio_progress_pm2.setValueAnimated(pm25.toFloat())
                        fragment_ioio_progress_pm10.setValueAnimated(pm10.toFloat())


                        val dataToDisplay = intArrayOf(pm01, pm25, pm10)

                        addGraphEntry(dataToDisplay)
                    } else {
                        fragment_ioio_progress_pm1.setValueAnimated(0.0f)
                        fragment_ioio_progress_pm2.setValueAnimated(0.0f)
                        fragment_ioio_progress_pm10.setValueAnimated(0.0f)
                    }
                })
        info("onStart")
    }


    override fun onResume() {
        super.onResume()
        info("onResume")
    }
    override fun onPause() {
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        super.onPause()
        info("onPause")
    }

    override fun onStop() {
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        super.onStop()
        info("onStop")
    }

    override fun onDestroyView() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        super.onDestroyView()
        info("onDestroyView")
    }

    override fun onDestroy() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        super.onDestroy()
        info("onDestroy")
    }

    companion object {
        //TODO : allow user to change that value
        // Graph params
        private const val MAX_VISIBLE_ENTRIES: Int = 100
        private const val MAX_REMOVED_ENTRIES: Int = 50
        private const val MAX_X_RANGE: Float = 40.0f
        private const val MAX_Y_RANGE: Float = 50.0f
        private const val MAX_Y_AXIS: Float = 3000.0f
        private const val MIN_Y_AXIS: Float = 0.0f
        private const val MIN_TIME_STAMP: Long = 0
    }
}


