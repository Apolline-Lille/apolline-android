package science.apolline.view.fragment

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.salomonbrys.kodein.android.appKodein
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.TileOverlay
import com.google.gson.GsonBuilder
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.AnkoLogger
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import science.apolline.R
import science.apolline.models.IOIOData
import science.apolline.root.RootFragment
import science.apolline.utils.CheckUtility
import science.apolline.utils.DataDeserializer
import science.apolline.utils.GeoHashHelper
import science.apolline.utils.MapStateManager
import science.apolline.viewModel.SensorViewModel
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import org.jetbrains.anko.info
import com.google.maps.android.heatmaps.Gradient
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import science.apolline.root.FragmentLifecycle


/**
 * Created by sparow on 2/26/2018.
 */


class MapFragment : RootFragment(), FragmentLifecycle, OnMapReadyCallback, AnkoLogger {

    private lateinit var mProvider: HeatmapTileProvider
    private lateinit var mOverlay: TileOverlay
    private lateinit var mOldGeoHash: String
    private lateinit var mMapFragment: SupportMapFragment
    private var mHeatMap: GoogleMap? = null
    private lateinit var mViewModel: SensorViewModel
    private lateinit var mLocationProvider: ReactiveLocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(SensorViewModel::class.java).init(appKodein())
        this.retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_map, container, false)

        mMapFragment = childFragmentManager.findFragmentById(R.id.fragment_mapview) as SupportMapFragment
        mMapFragment.getMapAsync(this)
        mMapFragment.apply {
            onCreate(savedInstanceState)
            onResume()
        }

        try {
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (e: Exception) {
            error("Can't init Maps: " + e.printStackTrace())
        }

        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLocationProvider = ReactiveLocationProvider(context)
        mOldGeoHash = ""
    }


    override fun onStart() {
        super.onStart()
        mMapFragment.onStart()
        if (mHeatMap != null && CheckUtility.checkFineLocationPermission(context!!.applicationContext) && CheckUtility.canGetLocation(context!!.applicationContext)) {
            if (!mHeatMap!!.isMyLocationEnabled) {
                mHeatMap.apply {
                    this!!.isMyLocationEnabled = true
                    uiSettings.isMyLocationButtonEnabled = true
                }
            }
        }
    }

    override fun onResume() {
        mMapFragment.onResume()
        super.onResume()
    }


    override fun onPause() {
        if (mHeatMap != null) {
            if (CheckUtility.checkFineLocationPermission(context!!.applicationContext) && CheckUtility.canGetLocation(context!!.applicationContext)) {
                mHeatMap.apply {
                    this!!.isMyLocationEnabled = false
                }
            }
            val mgr = MapStateManager(activity!!.baseContext, MAPS_NAME)
            mgr.saveMapState(mHeatMap)
        }
        super.onPause()
        mMapFragment.onPause()
    }


    override fun onStop() {
        super.onStop()
        mMapFragment.onStop()
        info("MAP onStop")
    }


    override fun onDestroy() {
        super.onDestroy()
        mMapFragment.onDestroy()
        info("MAP onDestroy")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mMapFragment.onDestroy()
        info("MAP onDestroyView")
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mMapFragment.onLowMemory()
    }

    override fun onPauseFragment() {
        if (mHeatMap != null) {
            if (CheckUtility.checkFineLocationPermission(context!!.applicationContext)) {
                mHeatMap.apply {
                    this!!.isMyLocationEnabled = false
                    uiSettings.isMyLocationButtonEnabled = false
                }
            }
        }
        info("MAP onPauseFragment")
    }

    override fun onResumeFragment() {
        if (mHeatMap != null) {
            if (CheckUtility.checkFineLocationPermission(context!!.applicationContext) && CheckUtility.canGetLocation(context!!.applicationContext)) {
                mHeatMap.apply {
                    this!!.isMyLocationEnabled = true
                    uiSettings.isMyLocationButtonEnabled = true
                }
            } else {
                mHeatMap.apply {
                    this!!.isMyLocationEnabled = false
                    uiSettings.isMyLocationButtonEnabled = false
                }
            }
        }
        info("MAP onResumeFragment")
    }

    override fun onMapReady(mapM: GoogleMap?) {
        mHeatMap = mapM as GoogleMap
        if (mHeatMap != null) {
            mHeatMap.apply {
                this!!.uiSettings.isCompassEnabled = true
                uiSettings.isIndoorLevelPickerEnabled = true
                uiSettings.isZoomControlsEnabled = true
                isIndoorEnabled = true
                isTrafficEnabled = false
                mapType = GoogleMap.MAP_TYPE_TERRAIN
            }

            val mgr = MapStateManager(context!!.applicationContext, MAPS_NAME)
            val position = mgr.savedCameraPosition

            if (position != null) {
                val update = CameraUpdateFactory.newCameraPosition(position)
                mHeatMap.apply {
                    this!!.moveCamera(update)
                    mapType = mgr.savedMapType
                }
            }

            if (CheckUtility.checkFineLocationPermission(context!!.applicationContext) && CheckUtility.canGetLocation(context!!.applicationContext)) {
                mHeatMap.apply {
                    this!!.isMyLocationEnabled = true
                    uiSettings.isMyLocationButtonEnabled = true
                }
            }

            initHeatMap()
        }

    }


    private fun addHeatMap(list: List<WeightedLatLng>) {
        val colors = intArrayOf(Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        )

        val startPoints = floatArrayOf(0.0625f, 1f)
        val gradient = Gradient(colors, startPoints)

        mProvider = HeatmapTileProvider.Builder()
                .weightedData(list)
                .gradient(gradient)
                .build()
        // Add a tile overlay to the map, using the heat map tile provider.
        if (mHeatMap != null)
            mOverlay = mHeatMap!!.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))

    }


    private fun initHeatMap() {
        doAsync {
            val listAllDevices = mViewModel.getDeviceList(MAX_DEVICE).blockingFirst()
            info("Size of list before: " + listAllDevices.size)
            val geo: MutableList<WeightedLatLng> = mutableListOf()
            if (listAllDevices.isNotEmpty()) {
                listAllDevices.forEach {
                    val gsonBuilder = GsonBuilder().registerTypeAdapter(IOIOData::class.java, DataDeserializer()).create()
                    val data = gsonBuilder.fromJson(it.data, IOIOData::class.java)
                    //val pm01 = data!!.pm01Value
                    val pm25 = data.pm2_5Value
                    //val pm10 = data.pm10Value
                    val geoHashStr = it.position?.geohash
                    if (geoHashStr == null || geoHashStr == "no") {
                        // info("geohash null or no")
                    } else {
                        geo.add(WeightedLatLng(GeoHashHelper.decode(geoHashStr), pm25.toDouble()))
                    }
                }

                geo.toList().distinctBy {
                    it.point
                }

                uiThread {
                    info("Size of list after: " + geo.size)
                    if (geo.isNotEmpty()) {
                        addHeatMap(geo)
                    }
                }
            } else {
                info("No list of Device objects returned")
            }
        }
    }

    companion object {
        private const val MAPS_NAME = "HEAT_MAP_HIST"
        private const val MAX_DEVICE = 10000L
    }
}