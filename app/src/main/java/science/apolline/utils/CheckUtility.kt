package science.apolline.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.location.LocationManager
import android.content.Context.LOCATION_SERVICE
import android.net.NetworkInfo
import android.net.ConnectivityManager
import android.telephony.TelephonyManager


/**
 * Created by sparow on 22/12/2017.
 */


object CheckUtility {

    /**
     * To get device consuming netowork type is 2g,3g,4g
     *
     * @param context
     * @return "2g","3g","4g" as a String based on the network type
     */
    fun getNetworkType(context: Context): String {
        val mTelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkType = mTelephonyManager.networkType
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
            TelephonyManager.NETWORK_TYPE_EVDO_A,
            TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            else -> "Notfound"
        }
    }

    /**
     * To check device has internet
     *
     * @param context
     * @return boolean as per status
     */
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }


    fun isWifiNetworkConnected(context: Context): Boolean {
        var wifiNetworkState = false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo

        if (netInfo.type == ConnectivityManager.TYPE_WIFI ){
            wifiNetworkState = true
        }

        return netInfo != null && wifiNetworkState
    }

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
        var gpsEnabled = false
        var networkEnabled = false

        val lm = context.getSystemService(LOCATION_SERVICE) as LocationManager

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {

        }

        return !(!gpsEnabled && !networkEnabled)
    }

}