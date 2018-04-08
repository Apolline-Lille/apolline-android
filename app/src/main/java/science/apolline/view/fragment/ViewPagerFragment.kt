package science.apolline.view.fragment

import android.content.Context
import android.content.Intent
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
import org.jetbrains.anko.info
import science.apolline.service.sensor.IOIOService
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.IntentFilter
import com.github.mikephil.charting.jobs.MoveViewJob


/**
 * Created by sparow on 2/27/2018.
 */
class ViewPagerFragment : RootFragment(), AnkoLogger {

    private val mFragmentIOIO by instance<IOIOFragment>()

    private val mFragmentChart by instance<ChartFragment>()

    private val mFragmentMaps by instance<MapFragment>()

    private lateinit var mAdapter: Adapter

    private var mServiceStatus: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        activity!!.registerReceiver(mReceiver, filter)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_viewpager, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = Adapter(childFragmentManager)
        setupViewPager(pager)
        tabs.setupWithViewPager(pager)
        pager.addOnPageChangeListener(pageChangeListener)
        pager.offscreenPageLimit = 3

        pager.setOnTouchListener { _, _ -> true  }
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

        val f2 = mFragmentChart
        mAdapter.addFragment(f2, "CHART")

        val f3 = mFragmentMaps
        mAdapter.addFragment(f3, "MAP")

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

    override fun onStop() {
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        super.onStop()
    }

    override fun onDestroyView() {
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        super.onDestroyView()
        activity!!.unregisterReceiver(mReceiver)
        info("ViewPager onDestroyView")
    }

    override fun onDestroy() {
        MoveViewJob.getInstance(null, 0f, 0f, null, null)
        super.onDestroy()
        info("ViewPager onDestroyView")
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity!!.startService(Intent(activity, IOIOService::class.java))
    }


    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        info("Bluetooth off")
                        activity!!.stopService(Intent(activity, IOIOService::class.java))
                    }
                    BluetoothAdapter.STATE_TURNING_OFF -> info("Turning Bluetooth off...")
                    BluetoothAdapter.STATE_ON -> {info("Bluetooth on")
                        activity!!.startService(Intent(activity, IOIOService::class.java))
                    }
                    BluetoothAdapter.STATE_TURNING_ON -> info("Turning Bluetooth on...")
                }
            }
        }
    }

    companion object {
        fun getServiceStatus(): Boolean {
            return IOIOService.getServiceStatus()
        }
    }

}