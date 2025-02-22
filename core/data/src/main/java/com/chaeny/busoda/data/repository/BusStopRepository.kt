package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop

interface BusStopRepository {
    fun getBusStops(): List<BusStop>
}
