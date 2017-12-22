package science.apolline.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

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
}