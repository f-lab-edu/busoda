package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStopMarker

interface NearbyBusStopsRepository {
    suspend fun getNearbyBusStops(latitude: Double, longitude: Double, radius: Int): List<BusStopMarker>
}
