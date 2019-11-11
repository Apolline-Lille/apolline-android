package science.apolline.models.deserializer

import science.apolline.models.SensorMessageModel
import java.lang.Exception
import java.util.*
import java.util.function.Consumer

class SensorMessageModelDeserializer {

    val pmKind : List< Double > = Arrays.asList( 1.0, 2.5, 10.0 );

    /**
     * @brief associate a key to a way to deserialize
     */
    val deserializer : Map< String, (model : SensorMessageModel, input : List<String> ) -> Boolean > = hashMapOf(
            "pm" to {model : SensorMessageModel, input : List<String> ->
                for( (i, kind) in pmKind.withIndex() ){
                    model.pm.put( kind, input.get( i ).toInt() );
                }
                true;
            },
            "gps" to {model : SensorMessageModel, input : List<String> ->
                var i = 0;
                model.gps.date = input.get( i++ );
                model.gps.satellites = input.get( i++ ).toInt();
                model.gps.position.longitude = input.get( i++ ).toDouble();
                model.gps.position.latitude = input.get( i++ ).toDouble();
                model.gps.position.accuracy = input.get( i++ ).toFloat();
                model.gps.position.speed = input.get( i++ ).toFloat();
                true;
            },
            "batterie" to { model: SensorMessageModel, input: List<String> ->
                model.batterieModel = input.get(0).toDouble();
                true;
            },
            "weather" to { model: SensorMessageModel, input: List<String> ->
                var i = 0;
                model.weatherModel.pression = input.get( i++ ).toFloat();
                model.weatherModel.temperature = input.get( i++ ).toFloat();
                model.weatherModel.humidity = input.get( i++ ).toFloat();
                true;
            }
    );

    /**
     * @brief for each key given say which position he has inside the serialized string
     */
    val mapping : Map< String, IntArray > = hashMapOf(
            "pm" to intArrayOf( 1, 2, 3 ),
            "gps" to intArrayOf( 0, 15, 11, 12, 13, 14 ),
            "batterie" to intArrayOf( 20 ),
            "weather" to intArrayOf( 17, 18, 19 )
    )

    /**
     * @brief parse the value of the model
     *
     * @param value is the input
     * @param output is where are stored
     *
     * @return true if the parsing has successed
     *
     */
    fun fromString( value : String, output : SensorMessageModel ) : Boolean {

        var splited = value.split( ";" );
        var success = true;

        for( entry in mapping ){

            if( deserializer.containsKey( entry.key ) ){

                var manip = deserializer[ entry.key ];
                try{
                    success = success && manip.invoke( output, entry.value.map { splited[ it ] } );
                } catch ( e : Exception ){
                    success = false;
                }

            } else {
                success = false;
            }

        }

        return success;
    }

}