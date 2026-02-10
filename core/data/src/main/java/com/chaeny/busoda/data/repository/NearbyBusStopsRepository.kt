package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStopPosition

interface NearbyBusStopsRepository {
    suspend fun getNearbyBusStops(latitude: Double, longitude: Double, radius: Int): List<BusStopPosition>
}
