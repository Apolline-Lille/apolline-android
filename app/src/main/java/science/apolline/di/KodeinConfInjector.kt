package science.apolline.di

import android.content.Context
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.eagerSingleton
import science.apolline.service.database.AppDatabase
import science.apolline.service.database.SensorDao

/**
 * Created by sparow on 2/25/2018.
 */

class KodeinConfInjector(context: Context) {

    val kodein = Kodein {
        // import(autoAndroidModule(this@ApollineApplication))
        val database: AppDatabase = AppDatabase.getInstance(context)
        bind<SensorDao>() with eagerSingleton { database.sensorDao() }
    }

}