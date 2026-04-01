package com.chaeny.busoda.data.repository

import com.chaeny.busoda.data.model.StopPositionItem
import com.chaeny.busoda.data.model.StopPositionResponse
import com.chaeny.busoda.data.network.BusApiService
import com.chaeny.busoda.data.util.replaceStopNameEntities
import com.chaeny.busoda.model.BusStopPosition
import java.io.IOException
import javax.inject.Inject

class ApiNearbyBusStopsRepository @Inject constructor(
    private val busApiService: BusApiService
) : NearbyBusStopsRepository {

    override suspend fun getNearbyBusStops(latitude: Double, longitude: Double, radius: Int): GetNearbyBusStopsResult {
        return try {
            val response = busApiService.getStationByPos(
                longitude = longitude,
                latitude = latitude,
                radius = radius
            )
            GetNearbyBusStopsResult.Success(response.toBusStopPositionList())
        } catch (e: IOException) {
            GetNearbyBusStopsResult.NoInternet
        } catch (e: Exception) {
            GetNearbyBusStopsResult.NetworkError
        }
    }

    private fun StopPositionResponse.toBusStopPositionList(): List<BusStopPosition> {
        val busStops = msgBody?.busStops

        val mappedBusStops = busStops
            ?.filter { it.isValidStop() }
            ?.map { busStop ->
                BusStopPosition(
                    stopId = busStop.stopId.orEmpty(),
                    stopName = busStop.stopName.orEmpty().replaceStopNameEntities(),
                    latitude = busStop.gpsY ?: 0.0,
                    longitude = busStop.gpsX ?: 0.0
                )
            }.orEmpty()

        return mappedBusStops
    }

    private fun StopPositionItem.isValidStop(): Boolean {
        return stopId.orEmpty() != "0" && gpsX != null && gpsY != null
    }
}
