package science.apolline.models

/**
 * Created by sparow on 10/20/17.
 */

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ch.hsr.geohash.GeoHash

@Entity
data class Position(
        @SerializedName("provider")
        @Expose
        var provider: String,
        @SerializedName("geohash")
        @Expose
        var geohash: String,
        @SerializedName("transport")
        @Expose
        var transport: String

) {
    @Ignore
    constructor() : this("no", "no", "no")

    override fun toString(): String {
        return """
        |Provider = $provider
        |Geohash = $geohash
        |Location = $transport
        """.trimMargin()
    }

}