package com.example.glovotestapp

import com.example.glovotestapp.data.MapRepository
import com.example.glovotestapp.data.source.remote.MapRemoteDataSource

object Injection {

    fun provideTasksRepository(): MapRepository {
        return MapRepository.getInstance(MapRemoteDataSource)
    }

}