package com.chaeny.busoda.data.repository

import com.chaeny.busoda.data.model.StopDetailResponse
import com.chaeny.busoda.data.network.BusApiService
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.model.CongestionLevel
import javax.inject.Inject

class ApiBusStopDetailRepository @Inject constructor(
    private val busApiService: BusApiService
) : BusStopDetailRepository {

    override suspend fun getBusStopDetail(stopId: String): BusStopDetail {
        val response = busApiService.getStationByUid(arsId = stopId)
        return response.toBusStopDetail()
    }

    override suspend fun getNextStopName(stopId: String): String {
        val response = busApiService.getStationByUid(arsId = stopId)
        return response.toNextStopName()
    }

    private fun StopDetailResponse.toBusStopDetail(): BusStopDetail {
        val busInfos = msgBody?.busInfos
        val stopName = busInfos?.firstOrNull()?.stopName

        val mappedBusInfos = busInfos?.map { busInfo ->
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
            stopName.orEmpty(),
            mappedBusInfos
        )
    }

    private fun StopDetailResponse.toNextStopName(): String {
        val busInfos = msgBody?.busInfos
        val nextStopName = busInfos?.firstOrNull()?.nextStopName
        return nextStopName.orEmpty()
    }

    private fun StopDetailResponse.parseArrivalInfo(
        arrMsg: String?, congestion: String?
    ): BusArrivalInfo? {
        val arrivalMsg = arrMsg ?: return null
        val congestionLevel = getCongestionLevel(congestion)

        val arrivalInfo = if (arrivalMsg.startsWith("[")) {
            arrivalMsg.substringAfter("]")
        } else {
            arrivalMsg
        }

        if ('[' !in arrivalInfo) {
            val noArrivalTimeInfo = BusArrivalInfo(
                "",
                arrivalInfo,
                congestionLevel
            )
            return noArrivalTimeInfo
        }

        val arrivalTime = arrivalInfo.substringBeforeLast("[")
        val position = arrivalInfo.substringAfterLast("[").substringBefore("]")

        return BusArrivalInfo(
            arrivalTime,
            position,
            congestionLevel
        )
    }

    private fun StopDetailResponse.getCongestionLevel(congestion: String?): CongestionLevel {
        return when (congestion) {
            "6" -> CongestionLevel.VERY_HIGH
            "5" -> CongestionLevel.HIGH
            "4" -> CongestionLevel.MEDIUM
            "3" -> CongestionLevel.LOW
            else -> CongestionLevel.UNKNOWN
        }
    }
}
