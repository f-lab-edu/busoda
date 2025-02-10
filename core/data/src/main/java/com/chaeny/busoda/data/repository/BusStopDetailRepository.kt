package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusStopDetail

interface BusStopDetailRepository {
    suspend fun getBusStopDetail(stopId: String): BusStopDetail
}
