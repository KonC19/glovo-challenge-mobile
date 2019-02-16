package com.example.glovotestapp

import com.example.glovotestapp.data.MapRepository
import com.example.glovotestapp.data.source.remote.MapRemoteDataSource
import com.example.glovotestapp.util.schedulers.BaseSchedulerProvider
import com.example.glovotestapp.util.schedulers.SchedulerProvider

object Injection {

    fun provideTasksRepository(): MapRepository {
        return MapRepository.getInstance(MapRemoteDataSource)
    }

    fun provideSchedulerProvider(): BaseSchedulerProvider {
        return SchedulerProvider.instance
    }

}