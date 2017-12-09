package science.apolline.service.networks

import science.apolline.BuildConfig

/**
 * Created by sparow on 13/10/2017.
 */


object ApiUtils {
    private var baseUrl = BuildConfig.INFLUXDB_URL

    fun setUrl(url: String): String {
        baseUrl = url
        return baseUrl
    }

    val apiService: ApiService
        get() = RetrofitClient.getClient(baseUrl)!!.create(ApiService::class.java)

}