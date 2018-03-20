package science.apolline.view.fragment

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.google.gson.GsonBuilder
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_chart_content.*
import kotlinx.android.synthetic.main.fragment_ioio.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import science.apolline.R
import science.apolline.models.Device
import science.apolline.models.IOIOData
import science.apolline.root.FragmentLifecycle
import science.apolline.utils.CustomMarkerView
import science.apolline.utils.DataDeserializer
import science.apolline.utils.DataExport.exportShareCsv
import science.apolline.utils.DataExport.exportToCsv
import science.apolline.utils.DataExport.exportToJson
import science.apolline.utils.HourAxisValueFormatter
import science.apolline.root.RootFragment
import science.apolline.service.database.SensorDao
import science.apolline.viewModel.SensorViewModel
import kotlin.math.roundToLong


class ChartFragment : RootFragment(), OnChartValueSelectedListener, FragmentLifecycle, AnkoLogger {

    private var mReferenceTimestamp: Long = MIN_TIMESTAMP
    private val mSensorDao by instance<SensorDao>()
    private lateinit var mDataList: List<ILineDataSet>
    private lateinit var mDisposable: CompositeDisposable
    private lateinit var mViewModel: SensorViewModel
    private lateinit var mPrefs: SharedPreferences


    private var MAX_VISIBLE_ENTRIES: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(SensorViewModel::class.java).init(appKodein())
        this.retainInstance = true

        // Preferences.
        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity!!.applicationContext)
        //mPrefs = this.getSharedPreferences( IDENTIFIER, Context.MODE_PRIVATE)
        MAX_VISIBLE_ENTRIES = (mPrefs.getString("visible_entries", "100")).toInt()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDisposable = CompositeDisposable()

        floating_action_menu_json.setOnClickListener {
            exportToJson(activity!!.application, mSensorDao)
        }
        floating_action_menu_csv.setOnClickListener {
            exportToCsv(activity!!.application, mSensorDao)
        }
        floating_action_menu_share.setOnClickListener {
            exportShareCsv(activity!!.application, mSensorDao)
        }

        mDataList = createParticleGauges()
        configureParticleHistory()
    }


    private fun createParticleGauges() : List<LineDataSet> {
        val setPM1 = createParticleGauge("PM1", Color.rgb(48, 79, 254))
        val setPM2 = createParticleGauge("PM2.5", Color.rgb(0, 200, 83))
        val setPM10 = createParticleGauge("PM10", Color.rgb(213, 0, 0))

        return listOf(setPM1, setPM2, setPM10)
    }

    private fun createParticleGauge(label: String, color: Int): LineDataSet {
        val gauge = LineDataSet(null, label)

        gauge.axisDependency = YAxis.AxisDependency.LEFT
        gauge.color = color
        gauge.setCircleColor(color)
        gauge.lineWidth = 2f
        gauge.circleRadius = 4f
        gauge.fillAlpha = 65
        gauge.fillColor = color
        gauge.highLightColor = Color.rgb(244, 117, 117)
        gauge.valueTextColor = color
        gauge.valueTextSize = 9f
        gauge.setDrawValues(false)

        return gauge
    }


    private fun configureParticleHistory() {
        mReferenceTimestamp = now()

        chart.marker = CustomMarkerView(context!!, R.layout.graph_custom_marker, mReferenceTimestamp)

        chart.setOnChartValueSelectedListener(this)
        chart.description.isEnabled = false

        chart.dragDecelerationFrictionCoef = 0.9f
        chart.isHighlightPerDragEnabled = true

        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(true)
        chart.setBackgroundColor(Color.TRANSPARENT)

        val data = LineData()
        data.setValueTextColor(Color.WHITE)
        chart.data = data
        chart.invalidate()

        val legend = chart.legend
        legend.form = Legend.LegendForm.LINE
        legend.typeface = Typeface.DEFAULT
        legend.textColor = Color.BLACK

        val xAxis = chart.xAxis
        xAxis.typeface = Typeface.DEFAULT
        xAxis.textColor = Color.BLACK
        xAxis.setDrawGridLines(false)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.isEnabled = true
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f // one hour
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        val xAxisFormatter = HourAxisValueFormatter(mReferenceTimestamp)
        xAxis.valueFormatter = xAxisFormatter

        val leftAxis = chart.axisLeft
        leftAxis.typeface = Typeface.DEFAULT
        leftAxis.textColor = Color.BLACK
        leftAxis.axisMaximum = MAX_Y_AXIS
        leftAxis.axisMinimum = MIN_Y_AXIS

        leftAxis.setDrawGridLines(true)

        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
    }


    private fun addValuesToHistory(inputData: IntArray) {
        val data = chart.data

        if (data != null) {
            if (data.dataSetCount != 3)
                for (temp in mDataList)
                    data.addDataSet(temp)

            val now = now()
            for (i in inputData.indices)
                data.addEntry(Entry((now - mReferenceTimestamp).toFloat(), inputData[i].toFloat()), i)

            data.dataSets.forEach {
                if (it.entryCount >= MAX_VISIBLE_ENTRIES)
                    for (i in 0 until MAX_REMOVED_ENTRIES)
                        it.removeEntry(i)
            }

            data.notifyDataChanged()
            // let the chart know it's data has changed
            chart.notifyDataSetChanged()
            // invalidate data in case of data removal du to max entryCount value
            chart.invalidate()
        }

        chart.setVisibleXRangeMaximum(MAX_X_RANGE)
        chart.setVisibleYRangeMaximum(MAX_Y_RANGE, YAxis.AxisDependency.LEFT)

        val count = chart.data.getDataSetByIndex(1).entryCount
        val entry = chart.data.getDataSetByIndex(1).getEntryForIndex(count - 1)
        chart.moveViewTo(entry.x, entry.y, YAxis.AxisDependency.LEFT)
    }

    private fun now() = System.currentTimeMillis() / 1000

    override fun onValueSelected(e: Entry, h: Highlight) {
        chart.setDrawMarkers(true)
    }

    override fun onNothingSelected() {
    }

    override fun onStart() {
        super.onStart()
        chart.fitScreen()
        mDisposable.add(mViewModel.getDeviceList(MAX_DEVICE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .onExceptionResumeNext {
                    Flowable.empty<Device>()
                 }
                .onErrorReturn {
                    error("Error device list not found $it")
                }
                .subscribe {
                    if (it.isNotEmpty()) {
                        val device = it.first()
                        val gsonBuilder = GsonBuilder().registerTypeAdapter(IOIOData::class.java, DataDeserializer()).create()
                        val data = gsonBuilder.fromJson(device.data, IOIOData::class.java)

                        val pm01 = data!!.pm01Value
                        val pm25 = data.pm2_5Value
                        val pm10 = data.pm10Value

//                        val tempC = data.tempCelcius.roundToLong()
//                        val tempK = data.tempKelvin.roundToLong()
//                        val humidComp = data.rht.roundToLong()

                        addValuesToHistory(intArrayOf(pm01, pm25, pm10))
                    } else {
                        info("No availlable data")
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
        if (!mDisposable.isDisposed)
            mDisposable.dispose()
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        super.onDestroyView()
        info("onDestroyView")
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroy() {
        if (!mDisposable.isDisposed)
            mDisposable.dispose()
        super.onDestroy()
        info("onDestroy")
    }

    override fun onPauseFragment() {
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        info("IOIO onPauseFragment")
    }

    override fun onResumeFragment() {
        info("IOIO onResumeFragment")
    }

    companion object {
        private const val MAX_REMOVED_ENTRIES: Int = 50
        private const val MAX_X_RANGE: Float = 40.0f
        private const val MAX_Y_RANGE: Float = 50.0f
        private const val MAX_Y_AXIS: Float = 3000.0f
        private const val MIN_Y_AXIS: Float = 0.0f
        private const val MIN_TIMESTAMP: Long = 0
        private const val MAX_DEVICE: Long = 10
    }
}
