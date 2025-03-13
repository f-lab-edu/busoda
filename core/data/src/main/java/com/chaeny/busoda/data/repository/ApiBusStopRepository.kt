package com.chaeny.busoda.data.repository

import com.chaeny.busoda.data.model.StopListResponse
import com.chaeny.busoda.data.network.BusApiService
import com.chaeny.busoda.model.BusStop
import java.io.IOException
import javax.inject.Inject

class ApiBusStopRepository @Inject constructor(
    private val busApiService: BusApiService
) : BusStopRepository {

    override suspend fun getBusStops(stopName: String): GetBusStopResult {
        return try {
            val response = busApiService.getStationByName(stopName = stopName)
            val busStops = response.toBusStopList()
            when {
                busStops.isNotEmpty() -> GetBusStopResult.Success(busStops)
                else -> GetBusStopResult.NoResult
            }
        } catch (e: IOException) {
            GetBusStopResult.NetworkError
        } catch (e: Exception) {
            GetBusStopResult.NoResult
        }
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
