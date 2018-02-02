package science.apolline.utils

import ch.hsr.geohash.GeoHash
import com.google.android.gms.maps.model.LatLng

/**
 * Created by patrick on 28/01/18.
 */

object GeoHashHelper {

    /**
     * Return a geohash code from given latitude and longitude
     */
    fun encode(latitude : Double, longitude : Double) : String{
        return GeoHash.geoHashStringWithCharacterPrecision(latitude,longitude,10)
    }

    /**
     * Return a couple of latitude and longitude based on geohash
     */
    fun decode(geohash : String) : LatLng{
        val latitude = GeoHash.fromGeohashString(geohash).point.latitude
        val longitude = GeoHash.fromGeohashString(geohash).point.longitude
        return LatLng(latitude,longitude)
    }

    /**
     * Return the latitude from given geohash
     */
    fun getLatitude(geohash : String) : Double{
        return GeoHash.fromGeohashString(geohash).point.latitude
    }

    /**
     * Return Longitude from given geohash
     */
    fun getLongitude(geohash : String) : Double{
        return GeoHash.fromGeohashString(geohash).point.longitude
    }
}