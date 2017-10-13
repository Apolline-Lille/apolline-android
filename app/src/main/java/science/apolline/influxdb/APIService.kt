package science.apolline.influxdb

import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.POST


/**
 * Created by sparow on 13/10/2017.
 */

interface APIService {

    @POST("write")
    //@FormUrlEncoded
    fun savePost(@Query("db") dbName: String,
                 @Query("u") dbUserName: String,
                 @Query("p") dbPassword: String,
                 @Body data: String): Call<Post>

}