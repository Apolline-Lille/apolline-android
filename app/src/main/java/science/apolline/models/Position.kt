package science.apolline.models

/**
 * Created by sparow on 10/20/17.
 */

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Position (
        @SerializedName("provider")
        @Expose
        val provider: String,
        @SerializedName("longitude")
        @Expose
        val longitude: Double,
        @SerializedName("latitude")
        @Expose
        val latitude: Double,
        @SerializedName("location")
        @Expose
        val location: String

) {

    override fun toString(): String {
        return """
        |Provider = $provider
        |Longitude = $longitude
        |Latitude = $latitude
        |Location = $location
        """.trimMargin()
    }

}