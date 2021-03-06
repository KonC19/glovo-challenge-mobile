package com.example.glovotestapp.map

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.glovotestapp.R
import com.example.glovotestapp.data.City
import kotlinx.android.synthetic.main.dialog_fragment_city_picker.*


class CityPickerDialogFragment : DialogFragment() {

    lateinit var cityCallback: (City) -> Unit

    companion object {
        val CITY_LIST_PARAM = "city_list_param"

        fun newInstance(cities: List<City>): CityPickerDialogFragment {
            val dialogFragment = CityPickerDialogFragment()
            val args = Bundle()
            args.putParcelableArrayList(CITY_LIST_PARAM, ArrayList<Parcelable>(cities))
            dialogFragment.arguments = args
            return dialogFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_city_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cityList = arguments?.getParcelableArrayList<Parcelable>(CITY_LIST_PARAM) as List<City>

        city_picker_recycler_view.layoutManager = LinearLayoutManager(context)
        val adapter = CityPickerAdapter(cityList, cityCallback)
        city_picker_recycler_view.adapter = adapter

    }
}