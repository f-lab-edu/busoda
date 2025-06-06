package com.chaeny.busoda.data.repository

import com.chaeny.busoda.data.model.StopDetailResponse
import com.chaeny.busoda.data.network.BusApiService
import com.chaeny.busoda.data.util.replaceStopNameEntities
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.model.CongestionLevel
import java.io.IOException
import javax.inject.Inject

class ApiBusStopDetailRepository @Inject constructor(
    private val busApiService: BusApiService
) : BusStopDetailRepository {

    override suspend fun getBusStopDetail(stopId: String): BusStopDetail {
        return try {
            val response = busApiService.getStationByUid(stopId = stopId)
            response.toBusStopDetail()
        } catch (e: IOException) {
            BusStopDetail("", emptyList())
        } catch (e: Exception) {
            BusStopDetail("", emptyList())
        }
    }

    override suspend fun getNextStopName(stopId: String): String {
        return try {
            val response = busApiService.getStationByUid(stopId = stopId)
            response.toNextStopName()
        } catch (e: IOException) {
            ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun StopDetailResponse.toBusStopDetail(): BusStopDetail {
        val busInfos = msgBody?.busInfos
        val stopName = busInfos?.firstOrNull()?.stopName

        val mappedBusInfos = busInfos?.map { busInfo ->
            val busArrivalInfos = listOfNotNull(
                parseArrivalInfo(
                    busInfo.firstBusArrMsg,
                    busInfo.firstBusCongestion,
                    busInfo.firstBusArrTime
                ),
                parseArrivalInfo(
                    busInfo.secondBusArrMsg,
                    busInfo.secondBusCongestion,
                    busInfo.secondBusArrTime
                )
            )
            BusInfo(
                busInfo.busNumber.orEmpty(),
                busInfo.nextStopName.orEmpty().replaceStopNameEntities(),
                busArrivalInfos
            )
        }.orEmpty()

        return BusStopDetail(
            stopName.orEmpty().replaceStopNameEntities(),
            mappedBusInfos
        )
    }

    private fun StopDetailResponse.toNextStopName(): String {
        val busInfos = msgBody?.busInfos
        val nextStopName = busInfos?.firstOrNull()?.nextStopName
        return nextStopName.orEmpty().replaceStopNameEntities()
    }

    private fun StopDetailResponse.parseArrivalInfo(
        arrMsg: String?, congestion: String?, arrTime: String?
    ): BusArrivalInfo? {
        val arrivalMsg = arrMsg ?: return null
        val congestionLevel = getCongestionLevel(congestion)
        val remainingTime = arrTime?.toLongOrNull() ?: 0L
        val now = System.currentTimeMillis() / 1000
        val arrivalTime: Long
        val position: String

        val arrivalInfo = if (arrivalMsg.startsWith("[")) {
            arrivalMsg.substringAfter("]")
        } else {
            arrivalMsg
        }

        if ("[" !in arrivalInfo) {
            arrivalTime = 0L
            position = arrivalInfo
        } else {
            arrivalTime = now + remainingTime
            position = arrivalInfo.substringAfterLast("[").substringBefore("]")
        }

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
