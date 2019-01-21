package science.apolline.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import at.grabner.circleprogress.TextMode
import com.fondesa.kpermissions.extension.listeners
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.PermissionRequest
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.google.gson.GsonBuilder
import es.dmoral.toasty.Toasty
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_ioio.*
import kotlinx.android.synthetic.main.fragment_ioio_content.*
import org.jetbrains.anko.*
import science.apolline.R
import science.apolline.models.Device
import science.apolline.models.IOIOData
import science.apolline.root.FragmentLifecycle
import science.apolline.utils.DataDeserializer
import science.apolline.utils.DataExport.exportShareCsv
import science.apolline.utils.DataExport.exportToCsv
import science.apolline.utils.DataExport.exportToJson
import science.apolline.root.RootFragment
import science.apolline.service.database.SensorDao
import science.apolline.utils.CheckUtility
import science.apolline.viewModel.SensorViewModel
import kotlin.math.roundToLong
import android.content.Context
import android.preference.PreferenceManager
import android.widget.TextView
import science.apolline.view.activity.MainActivity


class IOIOFragment : RootFragment(), FragmentLifecycle, AnkoLogger {

    private val mSensorDao by instance<SensorDao>()
    private lateinit var mDisposable: CompositeDisposable
    private lateinit var mViewModel: SensorViewModel
    private var mIsWriteToExternalStoragePermissionGranted = false



    private lateinit var mPrefs: SharedPreferences
    private lateinit var deviceAddress : String
    private lateinit var deviceName : String



    private val mRequestWriteToExternalStoragePermission by lazy {
        permissionsBuilder(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(SensorViewModel::class.java).init(appKodein())
        this.retainInstance = true
        MainActivity.mFragment = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ioio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDisposable = CompositeDisposable()

        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
        deviceAddress = mPrefs.getString("sensor_mac_address","sensor_mac_address does not exist")
        deviceName =  mPrefs.getString("sensor_name", "sensor_name does not exist")


        mIsWriteToExternalStoragePermissionGranted = CheckUtility.checkWriteToExternalStoragePermissionPermission (activity!!.applicationContext)

        floating_action_menu_json.setOnClickListener {

            if (!mIsWriteToExternalStoragePermissionGranted) {
                checkWriteToExternalStoragePermission(mRequestWriteToExternalStoragePermission)
            } else {
                exportToJson(activity!!.application, mSensorDao)
            }

        }
        floating_action_menu_csv_multi.setOnClickListener {
            if (!mIsWriteToExternalStoragePermissionGranted) {
                checkWriteToExternalStoragePermission(mRequestWriteToExternalStoragePermission)
            } else {
                exportToCsv(activity!!.application, mSensorDao, true)
            }
        }
        floating_action_menu_csv.setOnClickListener {
            if (!mIsWriteToExternalStoragePermissionGranted) {
                checkWriteToExternalStoragePermission(mRequestWriteToExternalStoragePermission)
            } else {
                exportToCsv(activity!!.application, mSensorDao, false)
            }
        }
        floating_action_menu_share.setOnClickListener {
            if (!mIsWriteToExternalStoragePermissionGranted) {
                checkWriteToExternalStoragePermission(mRequestWriteToExternalStoragePermission)
            } else {
                exportShareCsv(activity!!.application, mSensorDao, false)
            }
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

        if(this.deviceName.toLowerCase().contains(regex = "^ioio.".toRegex())) {

            mDisposable.add(mViewModel.getDeviceList(MAX_DEVICE)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onExceptionResumeNext {
                        Flowable.empty<Device>()
                    }
                    .onErrorReturn {
                        error("Error device list not found $it")
                        emptyList()
                    }
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
        }
        else {
            sensorName.text = "sensor name : " + this.deviceName
            fragment_ioio_progress_pm1.spin()
            fragment_ioio_progress_pm1.setText("Loading...")
            fragment_ioio_progress_pm2_5.spin()
            fragment_ioio_progress_pm2_5.setText("Loading...")
            fragment_ioio_progress_pm10.spin()
            fragment_ioio_progress_pm10.setText("Loading...")
            fragment_ioio_progress_rht.spin()
            fragment_ioio_progress_rht.setText("Loading...")
            fragment_ioio_progress_tmpk.spin()
            fragment_ioio_progress_tmpk.setText("Loading...")
            fragment_ioio_progress_tmpc.spin()
            fragment_ioio_progress_tmpc.setText("Loading...")


        }
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

    private fun checkWriteToExternalStoragePermission(request: PermissionRequest) {

        info("check permission")
        request.detachAllListeners()
        request.send()
        request.listeners {

            onAccepted {
                mIsWriteToExternalStoragePermissionGranted = true
                Toasty.success(activity!!.applicationContext, "WRITE_EXTERNAL_STORAGE permission granted.", Toast.LENGTH_SHORT, true).show()
            }

            onDenied {
                mIsWriteToExternalStoragePermissionGranted = false
                Toasty.error(activity!!.applicationContext, "WRITE_EXTERNAL_STORAGE permission denied.", Toast.LENGTH_SHORT, true).show()
            }

            onPermanentlyDenied {
                mIsWriteToExternalStoragePermissionGranted = false
                Toasty.error(activity!!.applicationContext, "Fatal error, WRITE_EXTERNAL_STORAGE permission permanently denied, please grant it manually", Toast.LENGTH_LONG, true).show()
            }

            onShouldShowRationale { _, _ ->
                mIsWriteToExternalStoragePermissionGranted = false

                activity!!.alert("Apolline couldn't export any file, please grant WRITE_EXTERNAL_STORAGE permission.", "Request write permission") {
                    yesButton {
                        checkWriteToExternalStoragePermission(mRequestWriteToExternalStoragePermission)
                    }
                    noButton {}
                }.show()

            }
        }

    }

    companion object {
        val TAG = "IOIOFragment"
        private const val MAX_DEVICE: Long = 10
    }
}
