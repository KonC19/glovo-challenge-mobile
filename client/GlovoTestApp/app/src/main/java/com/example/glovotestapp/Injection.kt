package com.example.glovotestapp

import android.content.Context
import com.example.glovotestapp.data.MapRepository
import com.example.glovotestapp.data.source.MapLocalDataSource
import com.example.glovotestapp.data.source.MapRemoteDataSource

object Injection {

    fun provideTasksRepository(context: Context): MapRepository {
//        val database = ToDoDatabase.getInstance(context)
        return MapRepository.getInstance(MapRemoteDataSource,
            MapLocalDataSource)
    }

}