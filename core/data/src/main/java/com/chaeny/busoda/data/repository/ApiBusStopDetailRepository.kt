package com.chaeny.busoda.data.repository

import com.chaeny.busoda.data.network.BusApiService
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStopDetail
import javax.inject.Inject

class ApiBusStopDetailRepository @Inject constructor(
    private val busApiService: BusApiService
) : BusStopDetailRepository {

    override suspend fun getBusStopDetail(stopId: String): BusStopDetail {
        val response = busApiService.getStationByUid(arsId = stopId)

        val busInfos = response.msgBody?.busInfos?.map { busInfo ->
            val busArrivalInfos = mutableListOf<BusArrivalInfo>()

            if (!busInfo.arrivalMessage1.isNullOrEmpty()) {
                val time = busInfo.arrivalMessage1.substringBefore("[")
                val position = busInfo.arrivalMessage1.substringAfter("[").removeSuffix("]")
                val congestion = when (busInfo.congestion1) {
                    "3" -> "여유"
                    "4" -> "보통"
                    "5" -> "혼잡"
                    "6" -> "매우혼잡"
                    else -> ""
                }
                busArrivalInfos.add(BusArrivalInfo(time, position, congestion))
            }

            if (!busInfo.arrivalMessage2.isNullOrEmpty()) {
                val time = busInfo.arrivalMessage2.substringBefore("[")
                val position = busInfo.arrivalMessage2.substringAfter("[").removeSuffix("]")
                val congestion = when (busInfo.congestion2) {
                    "3" -> "여유"
                    "4" -> "보통"
                    "5" -> "혼잡"
                    "6" -> "매우혼잡"
                    else -> ""
                }
                busArrivalInfos.add(BusArrivalInfo(time, position, congestion))
            }

            BusInfo(
                busInfo.busNumber ?: "",
                busInfo.nextStopName ?: "",
                busArrivalInfos
            )
        } ?: emptyList()

        return BusStopDetail(
            response.msgBody?.busInfos?.firstOrNull()?.stopName ?: "",
            busInfos
        )
    }

    override suspend fun getNextStopName(stopId: String): String {
        val response = busApiService.getStationByUid(arsId = stopId)
        return response.msgBody?.busInfos?.firstOrNull()?.nextStopName ?: ""
    }
}
