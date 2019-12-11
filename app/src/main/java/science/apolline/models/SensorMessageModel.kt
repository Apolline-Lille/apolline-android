package science.apolline.models

import android.graphics.Color

class SensorMessageModel{
    var pm : MutableMap< Double, Int > = HashMap();
    var gps : GpsModel = GpsModel();
    var batterieModel : Double = 0.0;
    var weatherModel : WeatherModel = WeatherModel();


    var color : Int
            get(){

                val keyPm10 = 10.0;
                var x  = 0;
                if( pm.containsKey( keyPm10 ) )
                    x = pm.get( keyPm10 )!!;

                return when {
                    (x <= 25.0) -> Color.rgb( x, 0xFF, 0 );
                    (x <= 50.0) -> 0xFF - ( x - 25 ) * 10
                    else -> Color.RED;
                };
            }
            set( v ){}
}