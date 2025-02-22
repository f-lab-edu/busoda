package com.chaeny.busoda.data.di

import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.BusStopRepository
import com.chaeny.busoda.data.repository.DummyBusStopDetailRepository
import com.chaeny.busoda.data.repository.DummyBusStopRepository
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
    abstract fun bindBusStopRepository(implementation: DummyBusStopRepository): BusStopRepository

    @Binds
    @Singleton
    abstract fun bindBusStopDetailRepository(implementation: DummyBusStopDetailRepository): BusStopDetailRepository
}
