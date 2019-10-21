package science.apolline.service.networks

import science.apolline.BuildConfig
import kotlin.IllegalArgumentException

/**
 * Created by sparow on 13/10/2017.
 */
object ApiUtils {

    /**
     * @brief this regex valide the given url before store them
     */
    private var regexUrl = "\\w+:(\\/?\\/?)[^\\s]+".toRegex();

    /**
     * @brief It's InfluxDB base url
     */
    private var baseUrl = validateUrl( BuildConfig.INFLUXDB_URL );

    /**
     * @brief observe the given and return it.
     * If the url is malformed then throw an exception.
     *
     * @param url is the
     *
     * @return the url given
     */
    private fun validateUrl( url : String ) : String {

        if( ! regexUrl.matches( url ) ){
            throw IllegalArgumentException( "The given is malformed. InfluxDB url : ${ url }" );
        }

        return url;
    }

    /**
     * @brief Change the base url of the influxdb database
     *
     * @param url is the new value of the influxdb url
     *
     * @return the new base url
     */
    fun setUrl( url: String ): String {
        baseUrl = validateUrl( url );
        return baseUrl;
    }

    /**
     * @brief get the current influx db url
     *
     * @return an url
     */
    fun getUrl(): String {
        return baseUrl
    }

    /**
     * An new api service instance from Influx DB url
     */
    val apiService: ApiService
        get() = RetrofitClient.getOrCreateClient( baseUrl ).create( ApiService::class.java )
}