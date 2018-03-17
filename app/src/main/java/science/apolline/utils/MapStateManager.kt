package science.apolline.utils


import android.content.Context
import android.content.SharedPreferences

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

/**
 * Created by sparow on 2/26/2018.
 */
class MapStateManager(context: Context, private val mMapName: String) {

    private val mapStatePrefs: SharedPreferences

    val savedCameraPosition: CameraPosition?
        get() {
            val latitude = mapStatePrefs.getFloat(LATITUDE + "_" + mMapName, 0f).toDouble()
            if (latitude == 0.0)
                return null
            val longitude = mapStatePrefs.getFloat(LONGITUDE + "_" + mMapName, 0f).toDouble()
            val target = LatLng(latitude, longitude)

            val zoom = mapStatePrefs.getFloat(ZOOM + "_" + mMapName, 0f)
            val bearing = mapStatePrefs.getFloat(BEARING + "_" + mMapName, 0f)
            val tilt = mapStatePrefs.getFloat(TILT + "_" + mMapName, 0f)

            return CameraPosition(target, zoom, tilt, bearing)
        }

    val savedMapType: Int
        get() = mapStatePrefs.getInt(MAPTYPE, GoogleMap.MAP_TYPE_NORMAL)

    init {
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveMapState(mapMie: GoogleMap?) {
        if (mapMie != null) {
            val editor = mapStatePrefs.edit()
            val position = mapMie.cameraPosition

            with(editor) {
                putFloat(LATITUDE + "_" + mMapName, position.target.latitude.toFloat())
                putFloat(LONGITUDE + "_" + mMapName, position.target.longitude.toFloat())
                putFloat(ZOOM + "_" + mMapName, position.zoom)
                putFloat(TILT + "_" + mMapName, position.tilt)
                putFloat(BEARING + "_" + mMapName, position.bearing)
                putInt(MAPTYPE + "_" + mMapName, mapMie.mapType)
                apply()
            }
        }
    }

    companion object {
        private const val LONGITUDE = "longitude"
        private const val LATITUDE = "latitude"
        private const val ZOOM = "zoom"
        private const val BEARING = "bearing"
        private const val TILT = "tilt"
        private const val MAPTYPE = "MAPTYPE"
        private const val PREFS_NAME = "mapCameraState"
    }
}