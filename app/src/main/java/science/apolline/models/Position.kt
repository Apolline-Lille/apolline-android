package science.apolline.models

/**
 * Created by sparow on 10/20/17.
 */

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Entity
data class Position(
        @SerializedName("provider")
        @Expose
        var provider: String,
        @SerializedName("longitude")
        @Expose
        var longitude: Double,
        @SerializedName("latitude")
        @Expose
        var latitude: Double,
        @SerializedName("location")
        @Expose
        var location: String

) {
    constructor() : this("", 0.0, 0.0, "")

    override fun toString(): String {
        return """
        |Provider = $provider
        |Longitude = $longitude
        |Latitude = $latitude
        |Location = $location
        """.trimMargin()
    }

}