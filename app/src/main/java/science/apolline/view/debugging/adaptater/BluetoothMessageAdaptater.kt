package science.apolline.view.debugging.adaptater

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.bluetooth_fragement.view.*
import org.jetbrains.anko.backgroundColor
import science.apolline.models.SensorMessageModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BluetoothMessageAdaptater : RecyclerView.Adapter< BluetoothMessageAdaptater.MessageHolder >() {

    /**
     * Hold all ui for a sensor message
     */
    class MessageHolder( val layout : ConstraintLayout ) : RecyclerView.ViewHolder( layout ) {
        val since = layout.getViewById( R.id.Since ) as TextView;

        val pm01 = layout.getViewById( R.id.PM1_value ) as TextView;
        val pm10 = layout.getViewById( R.id.PM10_value ) as TextView;
        val pm2_5 = layout.getViewById( R.id.PM2_5_value ) as TextView;

        val color = layout.getViewById( R.id.ColorValue ) as ConstraintLayout;

        val batterieLevel = layout.getViewById( R.id.BatterieValue ) as ProgressBar;

        val longitude = layout.getViewById( R.id.LongitudeValue ) as TextView;
        val latitude = layout.getViewById( R.id.LagitudeValue ) as TextView;
        val speed = layout.getViewById( R.id.SpeedValue ) as TextView;

        val temperature = layout.getViewById( R.id.TemperatureValue ) as TextView;
        val pression = layout.getViewById( R.id.PressionValue ) as TextView;
        val humidity = layout.getViewById( R.id.HumidityValue ) as TextView;
    }

    internal class Message( val msg : SensorMessageModel ){
        val time = System.currentTimeMillis();
    }

    private var messages = ArrayList< Message >()

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MessageHolder {
        // create a new view
        val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.bluetooth_fragement, parent, false) as ConstraintLayout

        return MessageHolder(layout)
    }

    val format = SimpleDateFormat( "hh:mm:ss" )

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val values = messages.get( position )

        holder.since.text = format.format( Date( values.time ) );

        holder.pm01.text = values.msg.pm.get( 1.0 ).toString()
        holder.pm10.text = values.msg.pm.get( 10.0 ).toString()
        holder.pm2_5.text = values.msg.pm.get( 2.5 ).toString()

        holder.color.backgroundColor = values.msg.color;

        holder.batterieLevel.progress = values.msg.batterieModel as Int;

        holder.longitude.text = values.msg.gps.position.longitude.toString()
        holder.latitude.text = values.msg.gps.position.latitude.toString()
        holder.speed.text = values.msg.gps.position.speed.toString()

        holder.temperature.text = values.msg.weatherModel.temperature.toString()
        holder.pression.text = values.msg.weatherModel.pression.toString()
        holder.humidity.text = values.msg.weatherModel.humidity.toString()
    }

    fun addMessage( msg : SensorMessageModel ){
        this.messages.add( Message( msg ) );
        this.notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = messages.size

}