package science.apolline.root

import android.os.Bundle
import android.support.v4.app.Fragment
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.android.appKodein

/**
 * Created by sparow on 2/25/2018.
 */

open class RootFragment : Fragment(), KodeinInjected {

    override val injector = KodeinInjector()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(appKodein())
    }
}