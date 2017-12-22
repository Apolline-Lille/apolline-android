package science.apolline.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HourAxisValueFormatter(private val referenceTimestamp: Long // minimum timestamp in your data set
) : IAxisValueFormatter {
    private val mDataFormat: DateFormat
    private val mDate: Date

    init {
        this.mDataFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        this.mDate = Date()
    }

    /**
     * Called when a value from an axis is to be formatted
     * before being drawn. For performance reasons, avoid excessive calculations
     * and memory allocations inside this method.
     */

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        // convertedTimestamp = originalTimestamp - referenceTimestamp
        val convertedTimestamp = value.toLong()

        // Retrieve original timestamp
        val originalTimestamp = referenceTimestamp + convertedTimestamp

        // Convert timestamp to hour:minute
        return getHour(originalTimestamp)
    }

    private fun getHour(timestamp: Long): String {
        try {
            mDate.time = timestamp * 1000
            return mDataFormat.format(mDate)
        } catch (ex: Exception) {
            return "xx"
        }

    }
}