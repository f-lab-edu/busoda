package com.chaeny.busoda.domain.di

import com.chaeny.busoda.domain.usecase.AddFavoriteBusUseCase
import com.chaeny.busoda.domain.usecase.DefaultAddFavoriteBusUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindAddFavoriteBusUseCase(implementation: DefaultAddFavoriteBusUseCase): AddFavoriteBusUseCase
}
