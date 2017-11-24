package science.apolline.service.geolocalisation

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

/**
 * Created by damien-lenovo on 24/11/2017.
 */
class mLocationListerner  () : LocationListener{
    override fun onLocationChanged(location: Location?) {

    }

    override fun onStatusChanged(location: String?, p1: Int, p2: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(location: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(location: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}