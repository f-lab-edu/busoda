package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStopDetail

sealed class GetBusStopDetailResult {
    data class Success(val data: BusStopDetail) : GetBusStopDetailResult()
    data object NoResult : GetBusStopDetailResult()
    data object NoInternet : GetBusStopDetailResult()
    data object NetworkError : GetBusStopDetailResult()
}
