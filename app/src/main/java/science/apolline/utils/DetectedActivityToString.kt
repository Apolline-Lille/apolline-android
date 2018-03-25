package science.apolline.utils

import com.google.android.gms.location.DetectedActivity
import io.reactivex.functions.Function

/**
 * Created by sparow on 3/25/2018.
 */
class DetectedActivityToString : Function<DetectedActivity, String> {
    override fun apply(detectedActivity: DetectedActivity): String {
        return getNameFromType(detectedActivity.type) + " : " + detectedActivity.confidence
    }

    private fun getNameFromType(activityType: Int): String {
        when (activityType) {
            DetectedActivity.RUNNING -> return "running"
            DetectedActivity.IN_VEHICLE -> return "in_vehicle"
            DetectedActivity.ON_BICYCLE -> return "on_bicycle"
            DetectedActivity.ON_FOOT -> return "on_foot"
            DetectedActivity.STILL -> return "still"
            DetectedActivity.UNKNOWN -> return "unknown"
            DetectedActivity.TILTING -> return "tilting"
        }
        return "unknown"
    }
}