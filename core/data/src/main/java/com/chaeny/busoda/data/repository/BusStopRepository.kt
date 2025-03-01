package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop

interface BusStopRepository {
    suspend fun getBusStops(): List<BusStop>
}
