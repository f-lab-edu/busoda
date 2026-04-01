package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStopPosition

sealed class GetNearbyBusStopsResult {
    data class Success(val data: List<BusStopPosition>) : GetNearbyBusStopsResult()
    data object NoInternet : GetNearbyBusStopsResult()
    data object NetworkError : GetNearbyBusStopsResult()
}
