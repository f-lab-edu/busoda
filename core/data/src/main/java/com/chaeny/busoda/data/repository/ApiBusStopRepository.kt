package com.chaeny.busoda.data.repository

import android.util.Log
import com.chaeny.busoda.data.model.StopListResponse
import com.chaeny.busoda.data.network.BusApiService
import com.chaeny.busoda.model.BusStop
import java.io.IOException
import javax.inject.Inject

class ApiBusStopRepository @Inject constructor(
    private val busApiService: BusApiService
) : BusStopRepository {

    override suspend fun getBusStops(stopName: String): List<BusStop> {
        return try {
            val response = busApiService.getStationByName(stopName = stopName)
            response.toBusStopList()
        } catch (e: IOException) {
            Log.e("Repository", "getBusStops(stopName=$stopName) - IOException: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            Log.e("Repository", "getBusStops(stopName=$stopName) - Exception: ${e.message}")
            emptyList()
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
