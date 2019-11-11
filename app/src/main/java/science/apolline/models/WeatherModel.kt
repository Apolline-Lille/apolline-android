package science.apolline.models

class WeatherModel {
    var pression : Float = 0F;

    var temperature : Float = 0F;

    var temperatureKelvin : Float
            get() = temperature + 273.15F;
            set( value ) { temperature = value - 273.15F }

    var humidity = 0F;

    var rht : Float
        get() = humidity / (1.0546F - 0.00216F * temperature) * 10;
        set(value){}
}