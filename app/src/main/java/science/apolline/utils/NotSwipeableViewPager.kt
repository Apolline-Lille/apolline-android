package science.apolline.utils

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Custom ViewPager without the swipe feature
 * @author Jérémy Da Costa
 */
class NotSwipeableViewPager(context: Context,attrs: AttributeSet) : ViewPager(context,attrs)
{
    /**
     * This function always return false to ensure that the swipe feature is not available
     *@return false
     */
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    /**
     * This function always return false to ensure that the swipe feature is not available
     * @return false
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}