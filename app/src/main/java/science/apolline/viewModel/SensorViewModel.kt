package science.apolline.viewModel

import science.apolline.service.database.SensorDao
import science.apolline.models.Device
import org.jetbrains.anko.*
import com.github.salomonbrys.kodein.instance
import io.reactivex.Flowable
import science.apolline.root.RootViewModel


class SensorViewModel: RootViewModel<SensorViewModel>(), AnkoLogger {


    private val sensorModel by injector.instance<SensorDao>()

    private lateinit var deviceListObserver: Flowable<List<Device>>


    fun getDeviceList(): Flowable<List<Device>>{
        deviceListObserver = sensorModel.getLastEntries()
        return deviceListObserver
    }
}