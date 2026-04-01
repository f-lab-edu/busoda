package com.chaeny.busoda.data.repository

interface NearbyBusStopsRepository {
    suspend fun getNearbyBusStops(latitude: Double, longitude: Double, radius: Int): GetNearbyBusStopsResult
}
