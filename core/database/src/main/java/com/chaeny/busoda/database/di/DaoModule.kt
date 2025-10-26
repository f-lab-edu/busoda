package com.chaeny.busoda.database.di

import com.chaeny.busoda.database.AppDatabase
import com.chaeny.busoda.database.dao.FavoriteStopDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaoModule {
    @Provides
    fun provideFavoriteStopDao(
        database: AppDatabase
    ): FavoriteStopDao = database.favoriteStopDao()
}
