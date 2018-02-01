package science.apolline.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.location.LocationManager
import android.content.Context.LOCATION_SERVICE



/**
 * Created by sparow on 22/12/2017.
 */


object CheckPermission {

    fun checkCoarseLocationPermission(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    fun checkFineLocationPermission(context: Context): Boolean {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    fun canGetLocation(context: Context): Boolean {
        var gps_enabled = false
        var network_enabled = false

        val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {

        }

        return !(!gps_enabled && !network_enabled)
    }

}