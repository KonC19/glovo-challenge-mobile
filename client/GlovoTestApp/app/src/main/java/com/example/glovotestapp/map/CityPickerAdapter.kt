package com.example.glovotestapp.map

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import com.example.glovotestapp.R
import com.example.glovotestapp.data.City
import kotlinx.android.synthetic.main.picker_item.view.*


class CityPickerAdapter(var items: List<City>, val cityCallback: (City) -> Unit) : RecyclerView.Adapter<CityPickerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.picker_item, parent, false)

        val vh = ViewHolder(view)

        view.setOnClickListener {
            cityCallback(items[vh.adapterPosition])
        }

        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cityName.text = items[position].name + " (" + items[position].countryCode + ')'
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun getItem(position: Int): City {
        return items[position]
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cityName = itemView.picker_item_text
    }

}