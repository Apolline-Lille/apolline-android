package science.apolline.service.networks

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.lang.IllegalArgumentException

/**
 * Created by sparow on 13/10/2017.
 */

object RetrofitClient {

    /**
     * @brief This object create an orm to a rest api webservice
     */
    private var retrofit: Retrofit? = null

    /**
     * @brief create a retrofit client
     *
     * @param baseUrl is the url to the rest api
     *
     * @return a retrofit instance
     */
    fun createClient( baseUrl: String ) : Retrofit {

        if( baseUrl == "null" ){
            throw IllegalArgumentException( "base url has the value null. Please chane it." );
        }

        retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit!!;
    }

    /**
     * @brief get the current retrofit client instancied
     *
     * @return a retrofit client
     */
    fun getClient() : Retrofit {

        if( retrofit == null ){
            throw IllegalArgumentException( "No retrofit client created. Please create one first." );
        }

        return retrofit!!;
    }

    /**
     * @brief get the current client if he doesn't exists then create a new one.
     *
     * @param baseUrl is the url to the rest api
     *
     * @return a retrofit client
     */
    fun getOrCreateClient( baseUrl: String ): Retrofit {

        if( retrofit == null )
            return createClient( baseUrl );

        return retrofit!!;
    }
}