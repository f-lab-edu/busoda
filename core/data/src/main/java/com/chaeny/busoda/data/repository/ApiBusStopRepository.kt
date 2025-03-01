package com.chaeny.busoda.data.repository

import com.chaeny.busoda.data.model.StopListResponse
import com.chaeny.busoda.data.network.BusApiService
import com.chaeny.busoda.model.BusStop
import javax.inject.Inject

class ApiBusStopRepository @Inject constructor(
    private val busApiService: BusApiService
) : BusStopRepository {

    override suspend fun getBusStops(): List<BusStop> {
        val response = busApiService.getStationByName(stationName = "화곡역")
        return response.toBusStopList()
    }

    private fun StopListResponse.toBusStopList(): List<BusStop> {
        val busStops = msgBody?.busStops

        val mappedBusStops = busStops?.map { busStop ->
            BusStop(
                stopId = busStop.stopId.orEmpty(),
                stopName = busStop.stopName.orEmpty()
            )
        }.orEmpty()

        return mappedBusStops
    }
}
