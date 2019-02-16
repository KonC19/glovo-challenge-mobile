package com.example.glovotestapp.network

import com.example.glovotestapp.data.City
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path


interface ApiService {

    @GET("cities")
    fun getCities(): Single<List<City>>

    @GET("cities/{cityCode}")
    fun getCityDetails(@Path("cityCode") cityCode: String): Single<City>
}