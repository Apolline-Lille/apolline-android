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
import science.apolline.root.RootFragment

/**
 * Created by sparow on 2/27/2018.
 */
class ViewPagerFragment : RootFragment(), AnkoLogger {

    private val mFragmentIOIO by instance<IOIOFragment>()

    private val mFragmentMaps by instance<MapFragment>()



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v: View = inflater.inflate(R.layout.fragment_viewpager, container, false)
        val pager = v.findViewById(R.id.pager) as ViewPager
        setupViewPager(pager)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabs.setupWithViewPager(pager)
    }

    private fun setupViewPager(pager: ViewPager?) {
        val adapter = Adapter(childFragmentManager)

        val f1 = mFragmentIOIO
        adapter.addFragment(f1, "IOIO")

        val f2 = mFragmentMaps
        adapter.addFragment(f2, "MAP")

        pager?.adapter = adapter
    }

    private class Adapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        val fragments = ArrayList<Fragment>()
        val titles = ArrayList<String>()
        override fun getItem(position: Int): Fragment = fragments.get(position)

        override fun getCount(): Int = fragments.size

        override fun getPageTitle(position: Int): CharSequence? = titles.get(position)

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }
    }
}