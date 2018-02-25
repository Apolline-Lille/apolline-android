package science.apolline.root

import android.arch.lifecycle.ViewModel
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.KodeinInjected
import com.github.salomonbrys.kodein.KodeinInjector

/**
 * Created by sparow on 2/25/2018.
 */


abstract class RootViewModel<out T> : ViewModel(), KodeinInjected {

    override val injector: KodeinInjector = KodeinInjector()

    @Suppress("UNCHECKED_CAST")
    fun init(kodein: Kodein): T {
        injector.inject(kodein)
        return this as T
    }

}