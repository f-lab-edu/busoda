package com.chaeny.busoda.data.repository

interface BusStopDetailRepository {

    suspend fun getBusStopDetail(stopId: String): GetBusStopDetailResult

    suspend fun getNextStopName(stopId: String): String
}
