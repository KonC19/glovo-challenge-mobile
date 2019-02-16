package com.example.glovotestapp.data

import io.reactivex.Single

class MapRepository(val remoteDataSource: MapDataSource) : MapDataSource{

    var cachedCities : Single<List<City>>? = null

    companion object {

        private var INSTANCE: MapRepository? = null

        fun getInstance(remoteDataSource: MapDataSource): MapRepository {
            return INSTANCE ?: MapRepository(remoteDataSource)
                .apply { INSTANCE = this }
        }
    }

    override fun getCities(): Single<List<City>> {
        if (cachedCities == null) {
            cachedCities = remoteDataSource.getCities().cache()
        }
        return cachedCities as Single<List<City>>
    }

    override fun getCityDetails(cityCode: String): Single<City> {
        return remoteDataSource.getCityDetails(cityCode)
    }
}