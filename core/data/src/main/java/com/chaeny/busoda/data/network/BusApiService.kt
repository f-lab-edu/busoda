package com.chaeny.busoda.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface BusApiService {
    @GET("getStationByUid")
    suspend fun getStationByUid(
        @Query("serviceKey") serviceKey: String,
        @Query("arsId") arsId: String
    ): String
}
