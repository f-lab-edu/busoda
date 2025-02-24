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
            val busArrivalInfos = listOfNotNull(
                parseArrivalInfo(busInfo.firstBusArrMsg, busInfo.firstBusCongestion),
                parseArrivalInfo(busInfo.secondBusArrMsg, busInfo.secondBusCongestion)
            )
            BusInfo(
                busInfo.busNumber.orEmpty(),
                busInfo.nextStopName.orEmpty(),
                busArrivalInfos
            )
        }.orEmpty()

        return BusStopDetail(
            response.msgBody?.busInfos?.firstOrNull()?.stopName.orEmpty(),
            busInfos
        )
    }

    private fun parseArrivalInfo(arrMsg: String?, congestion: String?): BusArrivalInfo? {
        val arrivalMsg = arrMsg ?: return null

        val arrivalInfo = if (arrivalMsg.startsWith("[")) {
            arrivalMsg.substringAfter("]")
        } else {
            arrivalMsg
        }

        if ('[' !in arrivalInfo) {
            return BusArrivalInfo(
                "",
                arrivalInfo,
                congestion.orEmpty()
            )
        }

        val arrivalTime = arrivalInfo.substringBeforeLast("[")
        val position = arrivalInfo.substringAfterLast("[").substringBefore("]")

        return BusArrivalInfo(
            arrivalTime,
            position,
            congestion.orEmpty()
        )
    }

    override suspend fun getNextStopName(stopId: String): String {
        val response = busApiService.getStationByUid(arsId = stopId)
        return response.msgBody?.busInfos?.firstOrNull()?.nextStopName ?: ""
    }
}
