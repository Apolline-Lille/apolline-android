package science.apolline.influxdb

/**
 * Created by sparow on 13/10/2017.
 */


object ApiUtils {

    val BASE_URL = "http://localhost:8086/"

    val apiService: APIService
        get() = RetrofitClient.getClient(BASE_URL)!!.create(APIService::class.java)

}