package science.apolline.viewModel

import science.apolline.service.database.SensorDao
import science.apolline.models.Device
import org.jetbrains.anko.*
import com.github.salomonbrys.kodein.instance
import io.reactivex.Flowable
import science.apolline.root.RootViewModel


class SensorViewModel: RootViewModel<SensorViewModel>(), AnkoLogger {


    private val sensorDao by instance<SensorDao>()

    private lateinit var deviceListObserver: Flowable<List<Device>>

    private lateinit var deviceAllListForMapObserver: Flowable<List<Device>>


    fun getDeviceList(): Flowable<List<Device>>{
        deviceListObserver = sensorDao.getLastEntries()
        return deviceListObserver
    }

    fun getAllDeviceForMapList(): Flowable<List<Device>>{
        deviceAllListForMapObserver = sensorDao.getAllForMap()
        return deviceAllListForMapObserver
    }
}