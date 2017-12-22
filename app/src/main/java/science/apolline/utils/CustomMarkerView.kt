package science.apolline.utils

import android.content.Context
import android.widget.TextView

import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import science.apolline.R


class CustomMarkerView(context: Context, layoutResource: Int, private val referenceTimestamp: Long  // minimum timestamp in your data set
) : MarkerView(context, layoutResource) {

    private val tvContent: TextView
    private val mDataFormat: DateFormat
    private val mDate: Date

    private var mOffset: MPPointF? = null

    init {

        // find your layout components
        tvContent = findViewById(R.id.tvContent)
        this.mDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        this.mDate = Date()
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val currentTimestamp = e!!.x.toInt() + referenceTimestamp
        tvContent.text = String.format("%s at %s", e.y, getTimedate(currentTimestamp))
        // this will perform necessary layouting
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {

        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }

        return mOffset as MPPointF
    }

    private fun getTimedate(timestamp: Long): String {

        try {
            mDate.time = timestamp * 1000
            return mDataFormat.format(mDate)
        } catch (ex: Exception) {
            return "xx"
        }

    }
}