package science.apolline.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import science.apolline.R

class SettingAdapter() :
RecyclerView.Adapter<SettingAdapter.MyViewHodler>() {

    private var myDataset = ArrayList<String>()

    class MyViewHodler(val textView: TextView) : RecyclerView.ViewHolder(textView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHodler {
        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_setting, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters

        return MyViewHodler(textView)
    }

    override fun getItemCount(): Int {
        return myDataset.size
    }

    override fun onBindViewHolder(holder: MyViewHodler, position: Int) {
        holder.textView.text = myDataset[position]
    }

    fun setValue(array:ArrayList<String>){
        myDataset = array
        notifyDataSetChanged()
    }
}