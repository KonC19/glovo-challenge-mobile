package com.example.glovotestapp.data.source.remote

import com.example.glovotestapp.data.City
import com.example.glovotestapp.data.MapDataSource
import com.example.glovotestapp.network.ApiClient
import com.example.glovotestapp.network.ApiService
import io.reactivex.Single

object MapRemoteDataSource : MapDataSource {
    override fun getCities(): Single<List<City>> {
        return ApiClient.retrofit.create(ApiService::class.java).getCities()
    }

    override fun getCityDetails(cityCode: String): Single<City> {
       return ApiClient.retrofit.create(ApiService::class.java).getCityDetails(cityCode)
    }
}