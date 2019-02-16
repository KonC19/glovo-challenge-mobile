package com.example.glovotestapp

import com.example.glovotestapp.data.City
import com.example.glovotestapp.data.Country
import com.google.android.gms.maps.model.LatLng

interface MapContract {
    interface View : BaseView<Presenter> {

        fun onCountriesLoaded(countries: List<Country>)

        fun onCitiesLoaded(countries: List<City>)

        fun showCityPicker(countries: List<City>)

        fun showPolygonsForCity(city: City)

        fun onCityDetailsLoaded(city: City)

        fun showMapMarkers(citiesCoordinates: HashMap<City, LatLng>)
    }

    interface Presenter : BasePresenter {

        fun loadCountries()

        fun checkLocation(latitude: Double, longitude: Double)

        fun loadCitiesForPicker()

        fun getCityDetails(cityCode: String)

        fun showMarkersForCities()
    }
}