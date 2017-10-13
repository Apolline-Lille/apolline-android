package science.apolline.influxdb

import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Field
import java.util.*


/**
 * Created by sparow on 13/10/2017.
 */

interface APIService {

    @POST("/write")
    @FormUrlEncoded
    fun savePost(@Field("dbName") dbName: String,
                 @Field("dbUsername") dbUserName: String,
                 @Field("dbPassword") dbPassword: String,
                 @Field("dbData") data: String)

}