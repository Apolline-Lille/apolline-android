package science.apolline.utils

import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import io.reactivex.functions.Function

/**
 * Created by sparow on 3/25/2018.
 */
class ToMostProbableActivity : Function<ActivityRecognitionResult, DetectedActivity> {
    override fun apply(activityRecognitionResult: ActivityRecognitionResult): DetectedActivity {
        return activityRecognitionResult.mostProbableActivity
    }
}