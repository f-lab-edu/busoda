package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop

interface BusStopRepository {
    suspend fun getBusStops(stopName: String): List<BusStop>
}
