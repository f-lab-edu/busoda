package com.chaeny.busoda.data.network

import com.chaeny.busoda.data.model.StopDetailResponse
import com.chaeny.busoda.data.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface BusApiService {
    @GET("stationinfo/getStationByUid")
    suspend fun getStationByUid(
        @Query("serviceKey", encoded = true) serviceKey: String = BuildConfig.SERVICE_KEY,
        @Query("arsId") arsId: String
    ): StopDetailResponse
}
