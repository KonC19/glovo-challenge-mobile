package com.example.glovotestapp.map

import com.example.glovotestapp.data.City
import com.example.glovotestapp.data.MapRepository
import com.example.glovotestapp.util.schedulers.BaseSchedulerProvider
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import java.util.*


class MapPresenter(val mapRepository: MapRepository, val mapView: MapContract.View, val schedulerProvider : BaseSchedulerProvider) :
    MapContract.Presenter {

    private val compositeDisposable: CompositeDisposable

    init {
        mapView.presenter = this
        compositeDisposable = CompositeDisposable()
    }

    override fun checkLocation(latitude: Double, longitude: Double) {
        compositeDisposable.add(
            mapRepository.getCities()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .map {
                    return@map  it.sortedBy { city ->
                        city.countryCode
                    }
                }
                .subscribeWith(object : DisposableSingleObserver<List<City>>() {
                    override fun onSuccess(value: List<City>) {
                        val city = getCityFromLocation(value, latitude, longitude)
                        if (city == null) {
                            mapView.showCityPicker(value)
                        } else {
                            getCityDetails(city.code)
                            mapView.showPolygonsForCity(city)
                        }
                    }

                    override fun onError(e: Throwable) {
                        mapView.showError(e.message)
                    }
                }))
    }

    override fun loadCitiesForPicker() {
        compositeDisposable.add(
            mapRepository.getCities()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .map {
                    return@map  it.sortedBy { city ->
                        city.countryCode
                    }
                }
                .subscribeWith(object : DisposableSingleObserver<List<City>>() {
                    override fun onSuccess(value: List<City>) {
                        mapView.showCityPicker(value)
                    }

                    override fun onError(e: Throwable) {
                        mapView.showError(e.message)
                    }
                }))
    }

    override fun loadMarkersForCities() {
        compositeDisposable.add(
            mapRepository.getCities()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(object : DisposableSingleObserver<List<City>>() {
                    override fun onSuccess(value: List<City>) {
                        mapView.showMapMarkers(getCitiesCoordinates(value))
                    }

                    override fun onError(e: Throwable) {
                        mapView.showError(e.message)
                    }
                }))
    }

    fun getCitiesCoordinates(cities: List<City>) : HashMap<City, LatLng> {
        val cityPointListMap : HashMap<City, LatLng> = hashMapOf()
        cities.forEach {
            for (polyline in it.workingArea) {
                val decoded = PolyUtil.decode(polyline)
                if (decoded.size != 0) {
                    cityPointListMap[it] = decoded[0]
                    break
                }
            }
        }
        return cityPointListMap
    }

    override fun getCityDetails(cityCode: String) {
        compositeDisposable.add(
            mapRepository.getCityDetails(cityCode)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribeWith(object : DisposableSingleObserver<City>() {
                    override fun onSuccess(value: City) {
                        mapView.onCityDetailsLoaded(value)
                    }

                    override fun onError(e: Throwable) {
                        mapView.showError(e.message)
                    }
                }))
    }

    fun getCityFromLocation(cities: List<City>, latitude: Double, longitude: Double) : City? {
        cities.forEach {
            if (isLocationInWorkingArea(it.workingArea, latitude, longitude)) {
                return it
            }
        }
        return null
    }

    private fun isLocationInWorkingArea(workingArea: List<String>, lat: Double, lng: Double) : Boolean {
        for (polyline in workingArea) {
            val decoded = PolyUtil.decode(polyline)
            if (decoded.size != 0) {
                if (PolyUtil.containsLocation(lat, lng, decoded, false)) {
                    return true
                }
            }
        }
        return false
    }

    override fun subscribe() {
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }
}