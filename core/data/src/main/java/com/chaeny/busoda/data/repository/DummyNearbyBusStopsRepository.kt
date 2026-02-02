package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStopMarker
import kotlinx.coroutines.delay
import javax.inject.Inject

class DummyNearbyBusStopsRepository @Inject constructor() : NearbyBusStopsRepository {

    private val dummyData: List<BusStopMarker> = listOf(
        BusStopMarker("02503", "시청역", 37.566031, 126.97701),
        BusStopMarker("02662", "시청.덕수궁", 37.566254, 126.976921),
        BusStopMarker("02902", "덕수궁", 37.566106, 126.976925),
        BusStopMarker("02286", "시청앞.덕수궁", 37.5662122834, 126.9768355729)
    )

    override suspend fun getNearbyBusStops(latitude: Double, longitude: Double, radius: Int): List<BusStopMarker> {
        delay(3000)
        return dummyData
    }
}
