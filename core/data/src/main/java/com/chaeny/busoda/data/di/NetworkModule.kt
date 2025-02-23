package com.chaeny.busoda.data.di

import com.chaeny.busoda.data.network.BusApiService
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://ws.bus.go.kr/api/rest/stationinfo/")
            .client(okHttpClient)
            .addConverterFactory(TikXmlConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBusApiService(retrofit: Retrofit): BusApiService {
        return retrofit.create(BusApiService::class.java)
    }
}
