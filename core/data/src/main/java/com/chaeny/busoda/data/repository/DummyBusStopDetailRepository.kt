package com.chaeny.busoda.data.repository

import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.model.CongestionLevel
import kotlinx.coroutines.delay
import javax.inject.Inject

class DummyBusStopDetailRepository @Inject constructor() : BusStopDetailRepository {

    private val now = System.currentTimeMillis() / 1000

    private val dummyData: Map<String, BusStopDetail> = mapOf(
        "16206" to BusStopDetail(
            "화곡역4번출구", listOf(
                BusInfo(
                    "604", "화곡본동시장", listOf(
                        BusArrivalInfo(now + 158, "2번째 전", CongestionLevel.MEDIUM),
                        BusArrivalInfo(now + 978, "9번째 전", CongestionLevel.HIGH)
                    )
                ),
                BusInfo(
                    "5712", "화곡본동시장", listOf(
                        BusArrivalInfo(now + 228, "3번째 전", CongestionLevel.LOW),
                        BusArrivalInfo(now + 1039, "10번째 전", CongestionLevel.HIGH)
                    )
                ),
                BusInfo(
                    "652", "화곡역1번출구", listOf(
                        BusArrivalInfo(now + 298, "4번째 전", CongestionLevel.MEDIUM),
                        BusArrivalInfo(now + 1100, "11번째 전", CongestionLevel.VERY_HIGH)
                    )
                )
            )
        ),
        "16146" to BusStopDetail(
            "화곡본동시장", listOf(
                BusInfo(
                    "604", "한국폴리텍1.서울강서대학교", listOf(
                        BusArrivalInfo(now + 278, "2번째 전", CongestionLevel.MEDIUM),
                        BusArrivalInfo(now + 978, "9번째 전", CongestionLevel.HIGH)
                    )
                ),
                BusInfo(
                    "5712", "한국폴리텍1.서울강서대학교", listOf(
                        BusArrivalInfo(now + 228, "3번째 전", CongestionLevel.LOW),
                        BusArrivalInfo(now + 1039, "10번째 전", CongestionLevel.HIGH)
                    )
                )
            )
        ),
        "16143" to BusStopDetail(
            "한국폴리텍1.서울강서대학교", listOf(
                BusInfo(
                    "604", "우장초등학교", listOf(
                        BusArrivalInfo(now + 158, "2번째 전", CongestionLevel.MEDIUM),
                        BusArrivalInfo(now + 978, "9번째 전", CongestionLevel.HIGH)
                    )
                ),
                BusInfo(
                    "5712", "우장초등학교", listOf(
                        BusArrivalInfo(now + 228, "3번째 전", CongestionLevel.LOW),
                        BusArrivalInfo(now + 1039, "10번째 전", CongestionLevel.HIGH)
                    )
                )
            )
        ),
        "16142" to BusStopDetail(
            "우장초등학교", listOf(
                BusInfo(
                    "604", "강서구청.한국건강관리협회", listOf(
                        BusArrivalInfo(now + 158, "2번째 전", CongestionLevel.MEDIUM),
                        BusArrivalInfo(now + 978, "9번째 전", CongestionLevel.HIGH)
                    )
                ),
                BusInfo(
                    "5712", "강서구청.한국건강관리협회", listOf(
                        BusArrivalInfo(now + 228, "3번째 전", CongestionLevel.LOW),
                        BusArrivalInfo(now + 1039, "10번째 전", CongestionLevel.HIGH)
                    )
                )
            )
        ),
        "16139" to BusStopDetail(
            "강서구청.한국건강관리협회", listOf(
                BusInfo(
                    "604", "강서구청사거리.서울디지털대학교", listOf(
                        BusArrivalInfo(now + 158, "2번째 전", CongestionLevel.MEDIUM),
                        BusArrivalInfo(now + 978, "9번째 전", CongestionLevel.HIGH)
                    )
                ),
                BusInfo(
                    "5712", "강서구청사거리.서울디지털대학교", listOf(
                        BusArrivalInfo(now + 228, "3번째 전", CongestionLevel.LOW),
                        BusArrivalInfo(now + 1039, "10번째 전", CongestionLevel.HIGH)
                    )
                )
            )
        ),
        "16008" to BusStopDetail(
            "강서구청사거리.서울디지털대학교", listOf(
                BusInfo(
                    "604", "등촌중학교.백석초등학교", listOf(
                        BusArrivalInfo(now + 158, "2번째 전", CongestionLevel.MEDIUM),
                        BusArrivalInfo(now + 978, "9번째 전", CongestionLevel.HIGH)
                    )
                ),
                BusInfo(
                    "5712", "등촌중학교.백석초등학교", listOf(
                        BusArrivalInfo(now + 228, "3번째 전", CongestionLevel.LOW),
                        BusArrivalInfo(now + 1039, "10번째 전", CongestionLevel.HIGH)
                    )
                )
            )
        )
    )

    override suspend fun getBusStopDetail(stopId: String): BusStopDetail {
        delay(3000)
        return dummyData[stopId] ?: BusStopDetail("", emptyList())
    }

    override suspend fun getNextStopName(stopId: String): String {
        delay(3000)
        val firstBusInfo = dummyData[stopId]?.busInfos?.firstOrNull()
        return firstBusInfo?.nextStopName ?: ""
    }
}
