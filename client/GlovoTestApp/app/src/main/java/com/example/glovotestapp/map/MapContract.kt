package com.example.glovotestapp.map

import com.example.glovotestapp.BasePresenter
import com.example.glovotestapp.BaseView
import com.example.glovotestapp.data.City
import com.google.android.gms.maps.model.LatLng

interface MapContract {
    interface View : BaseView<Presenter> {

        fun showCityPicker(cities: List<City>)

        fun showPolygonsForCity(city: City)

        fun onCityDetailsLoaded(city: City)

        fun showMapMarkers(citiesCoordinates: HashMap<City, LatLng>)

        fun showError(error: String?)
    }

    interface Presenter : BasePresenter {

        fun checkLocation(latitude: Double, longitude: Double)

        fun loadCitiesForPicker()

        fun getCityDetails(cityCode: String)

        fun loadMarkersForCities()
    }
}