package science.apolline.view.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.salomonbrys.kodein.instance
import kotlinx.android.synthetic.main.fragment_viewpager.*

import org.jetbrains.anko.AnkoLogger
import science.apolline.R
import science.apolline.root.FragmentLifecycle
import science.apolline.root.RootFragment
import android.support.v4.view.ViewPager.OnPageChangeListener


/**
 * Created by sparow on 2/27/2018.
 */
class ViewPagerFragment : RootFragment(), AnkoLogger {

    private val mFragmentIOIO by instance<IOIOFragment>()

    private val mFragmentMaps by instance<MapFragment>()

    private lateinit var mAdapter: Adapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_viewpager, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = Adapter(childFragmentManager)
        setupViewPager(pager)
        tabs.setupWithViewPager(pager)
        pager.addOnPageChangeListener(pageChangeListener)
    }

    private val pageChangeListener = object : OnPageChangeListener {

        internal var currentPosition = 0

        override fun onPageSelected(newPosition: Int) {

            val fragmentToShow = mAdapter.getItem(newPosition) as FragmentLifecycle
            fragmentToShow.onResumeFragment()

            val fragmentToHide = mAdapter.getItem(currentPosition) as FragmentLifecycle
            fragmentToHide.onPauseFragment()

            currentPosition = newPosition
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}

        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    private fun setupViewPager(pager: ViewPager?) {

        val f1 = mFragmentIOIO
        mAdapter.addFragment(f1, "IOIO")

        val f2 = mFragmentMaps
        mAdapter.addFragment(f2, "MAP")

        pager?.adapter = mAdapter
    }


    private class Adapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {

        val fragments = ArrayList<Fragment>()

        val titles = ArrayList<String>()

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence? = titles[position]

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }
    }
}