package science.apolline.influxdb

/**
 * Created by sparow on 10/20/17.
 */
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(
        @SerializedName("name")
        @Expose
        val name: String,
        @SerializedName("value")
        @Expose
        val value: Double,
        @SerializedName("unit")
        @Expose
        val unit: String
) {


    override fun toString(): String {
        return """
        |Name = $name ,
        |Value = $value ,
        |Unit = $unit,
        """.trimMargin()
    }

}