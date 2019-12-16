package science.apolline.view.debugging

import android.app.Activity
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import science.apolline.models.SensorMessageModel
import science.apolline.service.sensor.BluetoothService
import science.apolline.view.debugging.adaptater.BluetoothMessageAdaptater
import science.apolline.R

class BluetoothServiceDebuggingActivity : Activity() {

    private var mService : BluetoothService? = null;

    fun hasService() = mService != null;
    fun getBluetoothService() = mService!!;

    private var mConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if( service is BluetoothService.BluetoothServiceBinder ){
                mService = service.getService()
                getBluetoothService().getGateway().getData().observe(
                        this@BluetoothServiceDebuggingActivity as LifecycleOwner,
                        Observer<SensorMessageModel> { msg ->
                            if( msg != null )
                                this@BluetoothServiceDebuggingActivity.onData( msg )
                        } );
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null;
        }

    };

    val viewAdapter = BluetoothMessageAdaptater()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debugging_bluetooth)

        val viewManager = LinearLayoutManager(this)

        val recyclerView = findViewById(R.id.DebugMessagesView) as RecyclerView;
        recyclerView.layoutManager = viewManager
        recyclerView.adapter = viewAdapter

    }

    fun onData( msg : SensorMessageModel ){
        viewAdapter.addMessage( msg );
    }

    private var mIsBound = false;

    override fun onStart() {
        super.onStart()

        bindService(
                Intent( this, BluetoothService.javaClass ),
                mConnection,
                Context.BIND_AUTO_CREATE
        )
        mIsBound = true
    }

    fun doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    override fun onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

}