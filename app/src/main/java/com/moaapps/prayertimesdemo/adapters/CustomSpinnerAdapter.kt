package com.moaapps.prayertimesdemo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.modules.Country

class CountriesSpinnerAdapter(val list: List<Country>):BaseAdapter() {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(p0: Int): Any {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val view:View
        if (p1 == null){
            view = LayoutInflater.from(p2?.context).inflate(R.layout.item_country, p2, false)
        }else{
            view = p1
        }


        val country = list[p0]
        val emoji:TextView = view.findViewById(R.id.emoji)
        val name:TextView = view.findViewById(R.id.country_name)
        val native:TextView = view.findViewById(R.id.country_name_native)

        emoji.text = country.emoji
        name.text = country.name
        native.text = country.native

        return view
    }
}