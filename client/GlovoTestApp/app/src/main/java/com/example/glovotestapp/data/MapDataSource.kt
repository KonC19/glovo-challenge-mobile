package com.example.glovotestapp.data

import io.reactivex.Single

interface MapDataSource {

    fun getCountries(): Single<List<Country>>

    fun getCities(): Single<List<City>>

    fun getCityDetails(city: String): Single<City>
}