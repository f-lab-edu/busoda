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

            if (!busInfo.firstArrivalInfo.isNullOrEmpty()) {
                val time = busInfo.firstArrivalInfo.substringBefore("[")
                val position = busInfo.firstArrivalInfo.substringAfter("[").removeSuffix("]")
                val congestion = when (busInfo.firstBusCongestion) {
                    "3" -> "여유"
                    "4" -> "보통"
                    "5" -> "혼잡"
                    "6" -> "매우혼잡"
                    else -> ""
                }
                busArrivalInfos.add(BusArrivalInfo(time, position, congestion))
            }

            if (!busInfo.secondArrivalInfo.isNullOrEmpty()) {
                val time = busInfo.secondArrivalInfo.substringBefore("[")
                val position = busInfo.secondArrivalInfo.substringAfter("[").removeSuffix("]")
                val congestion = when (busInfo.secondBusCongestion) {
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
