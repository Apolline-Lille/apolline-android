package science.apolline.service.networks

/**
 * Created by sparow on 13/10/2017.
 */


object ApiUtils {

    val BASE_URL = "http://localhost:8086/"

    val apiService: ApiService
        get() = RetrofitClient.getClient(BASE_URL)!!.create(ApiService::class.java)

}