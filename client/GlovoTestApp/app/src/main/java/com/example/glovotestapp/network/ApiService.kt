package com.example.glovotestapp.network

import com.example.glovotestapp.data.City
import com.example.glovotestapp.data.Country
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    @GET("countries")
    fun getCountries(): Single<List<Country>>

    @GET("cities")
    fun getCities(): Single<List<City>>

    @GET("cities/{cityCode}")
    fun getCityDetails(@Path("cityCode") cityCode: String): Single<City>
}