package com.chaeny.busoda.data.network

import com.chaeny.busoda.data.model.StopDetailResponse
import com.chaeny.busoda.data.BuildConfig
import com.chaeny.busoda.data.model.StopListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BusApiService {
    @GET("stationinfo/getStationByUid")
    suspend fun getStationByUid(
        @Query("serviceKey", encoded = true) serviceKey: String = BuildConfig.SERVICE_KEY,
        @Query("arsId") stopId: String
    ): StopDetailResponse

    @GET("stationinfo/getStationByName")
    suspend fun getStationByName(
        @Query("serviceKey", encoded = true) serviceKey: String = BuildConfig.SERVICE_KEY,
        @Query("stSrch") stopName: String
    ): StopListResponse
}
