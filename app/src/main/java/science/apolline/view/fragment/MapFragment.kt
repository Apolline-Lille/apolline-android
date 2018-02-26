package science.apolline.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.AnkoLogger
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider
import science.apolline.R
import science.apolline.root.RootFragment
import science.apolline.utils.CheckUtility
import science.apolline.utils.MapStateManager


/**
 * Created by sparow on 2/26/2018.
 */


class MapFragment : RootFragment(), OnMapReadyCallback, AnkoLogger {

    private lateinit var mHeatMapView: MapView
    private lateinit var mHeatMap: GoogleMap
    private lateinit var mDisposable: CompositeDisposable
    private lateinit var mLocationProvider: ReactiveLocationProvider
    private val mRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER)
            .setNumUpdates(5)
            .setInterval(100)


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
                        mHeatMap.isMyLocationEnabled = true
                        mHeatMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(t.latitude, t.longitude), DEFAULT_ZOOM))
                        mHeatMap.uiSettings.isMyLocationButtonEnabled = true
                    }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mHeatMapView.onResume()
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
        mHeatMap.uiSettings.isMyLocationButtonEnabled = false
        mHeatMapView.onDestroy()
    }

    override fun onDestroyView() {
        if (!mDisposable.isDisposed) {
            mDisposable.dispose()
        }
        super.onDestroyView()
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mHeatMapView.onLowMemory()
    }

    override fun onMapReady(mapM: GoogleMap) {
        mHeatMap = mapM

        val mgr = MapStateManager(context!!.applicationContext, MAPS_NAME)
        val position = mgr.savedCameraPosition

        if (position != null) {
            val update = CameraUpdateFactory.newCameraPosition(position)
            mHeatMap.moveCamera(update)
            mHeatMap.mapType = mgr.savedMapType
        }
    }

    companion object {

        private val MAPVIEW_BUNDLE_KEY = "HEAT_MAP"
        private const val MAPS_NAME = "HEAT_MAP_HIST"
        private const val DEFAULT_ZOOM = 15.0f
    }
}