package com.chaeny.busoda.data.di

import com.chaeny.busoda.data.repository.ApiBusStopDetailRepository
import com.chaeny.busoda.data.repository.ApiBusStopRepository
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.BusStopRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBusStopRepository(implementation: ApiBusStopRepository): BusStopRepository

    @Binds
    @Singleton
    abstract fun bindBusStopDetailRepository(implementation: ApiBusStopDetailRepository): BusStopDetailRepository
}
