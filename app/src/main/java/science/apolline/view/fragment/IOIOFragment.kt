package science.apolline.view.fragment

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
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
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
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
import science.apolline.root.FragmentLifecycle
import science.apolline.service.sensor.IOIOService
import science.apolline.utils.CustomMarkerView
import science.apolline.utils.DataDeserializer
import science.apolline.utils.DataExport.exportShareCsv
import science.apolline.utils.DataExport.exportToCsv
import science.apolline.utils.DataExport.exportToJson
import science.apolline.utils.HourAxisValueFormatter
import science.apolline.root.RootFragment
import science.apolline.service.database.SensorDao
import science.apolline.viewModel.SensorViewModel
import kotlin.math.round
import kotlin.math.roundToLong


class IOIOFragment : RootFragment(), FragmentLifecycle, AnkoLogger {

    private val mSensorDao by instance<SensorDao>()
    private lateinit var mDisposable: CompositeDisposable
    private lateinit var mViewModel: SensorViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(SensorViewModel::class.java).init(appKodein())
        this.retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ioio, container, false)
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

        fragment_ioio_progress_pm1.apply {
            setTextMode(TextMode.VALUE)
            setValue(0F)
        }

        fragment_ioio_progress_pm2_5.apply {
            setTextMode(TextMode.VALUE)
            setValue(0F)
        }
        fragment_ioio_progress_pm10.apply {
            setTextMode(TextMode.VALUE)
            setValue(0F)
        }

        fragment_ioio_progress_rht.apply {
            setTextMode(TextMode.VALUE)
            setValue(0F)
        }

        fragment_ioio_progress_tmpk.apply {
            setTextMode(TextMode.VALUE)
            setValue(0F)
        }
        fragment_ioio_progress_tmpc.apply {
            setTextMode(TextMode.VALUE)
            setValue(0F)
        }

    }



    override fun onStart() {
        super.onStart()
        mDisposable.add(mViewModel.getDeviceList(MAX_DEVICE)
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
                        val tempC = data.tempCelcius.roundToLong()
                        val tempK = data.tempKelvin.roundToLong()
                        val humidComp = data.rht.roundToLong()

                        fragment_ioio_progress_pm1.setValueAnimated(pm01.toFloat())
                        fragment_ioio_progress_pm2_5.setValueAnimated(pm25.toFloat())
                        fragment_ioio_progress_pm10.setValueAnimated(pm10.toFloat())
                        fragment_ioio_progress_rht.setValueAnimated(humidComp.toFloat())
                        fragment_ioio_progress_tmpk.setValueAnimated(tempK.toFloat())
                        fragment_ioio_progress_tmpc.setValueAnimated(tempC.toFloat())

                    } else {
                        fragment_ioio_progress_pm1.setValue(0.0f)
                        fragment_ioio_progress_pm2_5.setValue(0.0f)
                        fragment_ioio_progress_pm10.setValue(0.0f)
                        fragment_ioio_progress_rht.setValue(0.0f)
                        fragment_ioio_progress_tmpk.setValue(0.0f)
                        fragment_ioio_progress_tmpc.setValue(0.0f)

                    }
                })
        info("onStart")
    }

    override fun onResume() {
        super.onResume()
        info("onResume")
    }

    override fun onPause() {
        super.onPause()
        info("onPause")
    }

    override fun onStop() {
        super.onStop()
        info("onStop")
    }

    override fun onDestroyView() {
        if (!mDisposable.isDisposed)
            mDisposable.dispose()
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
        info("IOIO onPauseFragment")
    }

    override fun onResumeFragment() {
        info("IOIO onResumeFragment")
    }

    companion object {
        private const val MAX_DEVICE: Long = 10
    }
}
