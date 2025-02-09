package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStopDetail

interface BusStopDetailRepository {
    fun getBusStopDetail(stopId: String): BusStopDetail
}
