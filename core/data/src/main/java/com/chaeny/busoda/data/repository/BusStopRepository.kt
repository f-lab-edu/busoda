package com.chaeny.busoda.data.repository

interface BusStopRepository {
    suspend fun getBusStops(stopName: String): BusStopResult
}
