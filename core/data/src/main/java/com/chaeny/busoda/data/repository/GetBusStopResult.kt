package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStop

sealed class GetBusStopResult {
    data class Success(val data: List<BusStop>) : GetBusStopResult()
    data object NoResult : GetBusStopResult()
    data object NoInternet : GetBusStopResult()
    data object NetworkError : GetBusStopResult()
}
