package com.example.glovotestapp.data

import io.reactivex.Single

class MapRepository(val remoteDataSource: MapDataSource, val localDataSource: MapDataSource) : MapDataSource{

    var cachedCities : Single<List<City>>? = null

    companion object {

        private var INSTANCE: MapRepository? = null

        fun getInstance(remoteDataSource: MapDataSource, localDataSource: MapDataSource): MapRepository {
            return INSTANCE ?: MapRepository(remoteDataSource, localDataSource)
                .apply { INSTANCE = this }
        }
    }

    override fun getCountries(): Single<List<Country>> {
        return remoteDataSource.getCountries()
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