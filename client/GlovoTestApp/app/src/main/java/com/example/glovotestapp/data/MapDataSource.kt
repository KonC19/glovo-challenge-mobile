package com.example.glovotestapp.data

import io.reactivex.Single

interface MapDataSource {

    fun getCities(): Single<List<City>>

    fun getCityDetails(city: String): Single<City>
}