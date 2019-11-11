package science.apolline.models

import android.location.Location
import android.os.Parcel

class GpsModel {
    var position : Location = Location.CREATOR.createFromParcel( Parcel.obtain() );
    var date : String = "";
    var satellites : Int = 0;
}