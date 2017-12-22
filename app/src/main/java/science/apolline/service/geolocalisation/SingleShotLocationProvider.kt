package science.apolline.service.geolocalisation

import android.Manifest
import android.content.Context
import android.location.Criteria
import android.location.LocationManager
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.support.v4.content.ContextCompat
import science.apolline.utils.CheckPermission.checkCoarseLocationPermission
import science.apolline.utils.CheckPermission.checkFineLocationPermission


/**
 * Created by damien-lenovo on 24/11/2017.
 */
object SingleShotLocationProvider {


    // calls back to calling thread, note this is for low grain: if you want higher precision, swap the
    // contents of the else and if. Also be sure to check gps permission/settings are allowed.
    // call usually takes <10ms
    fun requestSingleUpdate(context: Context, mLocationListener: LocationListener){
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (isNetworkEnabled && checkCoarseLocationPermission(context)) {
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_COARSE
            locationManager.requestSingleUpdate(criteria, mLocationListener, null)
        } else {
            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (isGPSEnabled && checkFineLocationPermission(context)) {
                val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_FINE
                locationManager.requestSingleUpdate(criteria, mLocationListener, null)
            }
        }
    }



}