package science.apolline.view.fragment

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.salomonbrys.kodein.android.appKodein
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlay
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
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


/**
 * Created by sparow on 2/26/2018.
 */


class MapFragment : RootFragment(), OnMapReadyCallback, AnkoLogger {

    private lateinit var mProvider: HeatmapTileProvider
    private lateinit var mOverlay: TileOverlay
    private lateinit var mHeatMapView: MapView
    private lateinit var mOldGeoHash: String

    private lateinit var mHeatMap: GoogleMap
    private lateinit var mDisposable: CompositeDisposable
    private lateinit var mViewModel: SensorViewModel
    private lateinit var mLocationProvider: ReactiveLocationProvider
    private val mRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER)
            .setNumUpdates(5)
            .setInterval(100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(SensorViewModel::class.java).init(appKodein())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v: View = inflater.inflate(R.layout.fragment_map, container, false)

        mHeatMapView = v.findViewById(R.id.fragment_mapview) as MapView

        mHeatMapView.getMapAsync(this)
        mHeatMapView.onCreate(savedInstanceState)
        mHeatMapView.onResume()

        try {
            MapsInitializer.initialize(activity!!.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mDisposable = CompositeDisposable()
        mLocationProvider = ReactiveLocationProvider(context)
        mOldGeoHash = ""
    }

    override fun onStart() {
        super.onStart()
        mHeatMapView.onStart()
        if (CheckUtility.checkFineLocationPermission(context!!.applicationContext) && CheckUtility.canGetLocation(context!!.applicationContext)) {
            mDisposable.add(mLocationProvider.getUpdatedLocation(mRequest)
                    .onExceptionResumeNext(Observable.empty())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError {
                        mHeatMap.uiSettings.isMyLocationButtonEnabled = false
                        error("Android reactive location error" + it.toString())
                    }
                    .subscribe { t ->
                        if (!mHeatMap.isMyLocationEnabled){
                            mHeatMap.isMyLocationEnabled = true
                        }
                        mHeatMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(t.latitude, t.longitude), DEFAULT_ZOOM))
                    }
            )

            mDisposable.add(mViewModel.getDeviceList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {

                        if (it.isNotEmpty()) {

                            val device = it.first()
                            val gsonBuilder = GsonBuilder().registerTypeAdapter(IOIOData::class.java, DataDeserializer()).create()
                            val data = gsonBuilder.fromJson(device.data, IOIOData::class.java)
                            //val pm01 = data!!.pm01Value
                            val pm25 = data.pm2_5Value
                            //val pm10 = data.pm10Value

                            val geoHashStr = device.position?.geohash

                            if (geoHashStr == null || geoHashStr == "no") {

                            } else {

                                val lonLat = WeightedLatLng(GeoHashHelper.decode(geoHashStr), pm25.toDouble())
                                val geo = listOf(lonLat)

                                if (mOldGeoHash == geoHashStr) {
                                    info("Same geoHash with old Device object")
                                    //mOverlay.remove()
                                    addHeatMap(geo)
                                } else {
                                    addHeatMap(geo)
                                    mOldGeoHash = geoHashStr
                                }


                            }

                        } else {
                            info("No list of Device objects returned")
                        }
                    })
        }
    }

    override fun onResume() {
        mHeatMapView.onResume()
        super.onResume()
    }


    override fun onPause() {

        val mgr = MapStateManager(activity!!.baseContext, MAPS_NAME)
        mgr.saveMapState(mHeatMap)

        super.onPause()
        mHeatMapView.onPause()

    }


    override fun onStop() {
        if (!mDisposable.isDisposed) {
            mDisposable.dispose()
        }
        super.onStop()
        mHeatMapView.onStop()
    }


    override fun onDestroy() {
        if (!mDisposable.isDisposed) {
            mDisposable.dispose()
        }

        super.onDestroy()
        info("MAP onDestroy")
    }


    override fun onDestroyView() {
        if (!mDisposable.isDisposed) {
            mDisposable.dispose()
        }

        mHeatMapView.onPause()
        mHeatMapView.onDestroy()
        if (CheckUtility.checkFineLocationPermission(context!!.applicationContext) && CheckUtility.canGetLocation(context!!.applicationContext)) {
            if(mHeatMap!=null){

            }
            mHeatMap.isMyLocationEnabled = false
            mHeatMap.clear()
        }

        super.onDestroyView()
        info("MAP onDestroyView")
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mHeatMapView.onLowMemory()
    }

    override fun onMapReady(mapM: GoogleMap) {
        mHeatMap = mapM

        mHeatMap.uiSettings.isCompassEnabled = true
        mHeatMap.uiSettings.isMyLocationButtonEnabled = true
        mHeatMap.uiSettings.isIndoorLevelPickerEnabled = true
        mHeatMap.uiSettings.isZoomControlsEnabled = true
        mHeatMap.isIndoorEnabled = true
        mHeatMap.isBuildingsEnabled = true
        mHeatMap.isTrafficEnabled = true
        mHeatMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        val mgr = MapStateManager(context!!.applicationContext, MAPS_NAME)
        val position = mgr.savedCameraPosition

        if (position != null) {
            val update = CameraUpdateFactory.newCameraPosition(position)
            mHeatMap.moveCamera(update)
            mHeatMap.mapType = mgr.savedMapType
        }

        // Add existing points to map
        initHeatMap()
    }


    private fun addHeatMap(list: List<WeightedLatLng>) {

        // Create the gradient.
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
        mOverlay = mHeatMap.addTileOverlay(TileOverlayOptions().tileProvider(mProvider))
    }


    private fun initHeatMap() {

        doAsync {

            val listAllDevices = mViewModel.getAllDeviceForMapList().blockingFirst()

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
                        val lonLat = WeightedLatLng(GeoHashHelper.decode(geoHashStr), pm25.toDouble())
                        geo.add(lonLat)
                    }
                }

                geo.toList().distinctBy {
                    it.point
                }

                uiThread {
                    info("Size of list after: " + geo.size)
                    addHeatMap(geo)
                }

            } else {
                info("No list of Device objects returned")
            }


        }


    }


    companion object {
        private const val MAPS_NAME = "HEAT_MAP_HIST"
        private const val DEFAULT_ZOOM = 20.0f
    }
}