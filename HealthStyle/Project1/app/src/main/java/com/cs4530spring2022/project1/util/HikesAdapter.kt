package com.cs4530spring2022.project1.util

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs4530spring2022.project1.R


class HikesAdapter(private var hikesData: List<ItemsHikesRow> = listOf()) : RecyclerView.Adapter<HikesAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v){
        var name: TextView = v.findViewById(R.id.tv_hike)
        var address: TextView = v.findViewById(R.id.tv_address)
        val pic: ImageView = v.findViewById(R.id.iv_pic)
    }

    override fun getItemCount() = hikesData.size

    override fun onCreateViewHolder(vg: ViewGroup, type: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(vg.context).inflate(R.layout.hike_row, vg,false))
    }

    override fun onBindViewHolder(vh: ViewHolder, idx: Int) {
        vh.name.text = hikesData[idx].name
        vh.pic.setImageResource(hikesData[idx].image)
        vh.address.text = hikesData[idx].address
        vh.itemView.setOnClickListener {
            // onClick behavior! Use this information!
            val lat = hikesData[idx].latitude
            val lon = hikesData[idx].longitude
            // Create a Uri from an intent string. Use the result to create an Intent.
            val gmmIntentUri =
                Uri.parse("geo:" + lat.toString() + "," + lon.toString() + "?q=" + vh.name.text)
            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            // Make the Intent explicit by setting the Google Maps package
            mapIntent.setPackage("com.google.android.apps.maps")
            // Attempt to start an activity that can handle the Intent
            vh.pic.getContext().startActivity(mapIntent)
        }
    }

    fun setHikesData(data: List<ItemsHikesRow>) {
        hikesData = data
        notifyDataSetChanged()
    }

    // TODO : onHikeClicked()

}
