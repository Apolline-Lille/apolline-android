package science.apolline.utils

import android.content.Context

import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.graph_custom_marker.view.*

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class CustomMarkerView(context: Context, layoutResource: Int, private val referenceTimestamp: Long  // minimum timestamp in your data set
) : MarkerView(context, layoutResource) {
    private val mDataFormat: DateFormat
    private val mDate: Date
    private var mOffset: MPPointF? = null

    init {
        this.mDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        this.mDate = Date()
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val currentTimestamp = e!!.x.toInt() + referenceTimestamp
        tvContent.text = String.format("%s at %s", e.y, getTimedate(currentTimestamp))
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        if (mOffset == null)
            mOffset = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
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