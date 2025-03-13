package com.chaeny.busoda.data.repository

import com.chaeny.busoda.data.model.StopDetailBody
import com.chaeny.busoda.data.model.StopDetailItem
import com.chaeny.busoda.data.model.StopDetailResponse
import com.chaeny.busoda.data.network.BusApiService
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.model.CongestionLevel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ApiBusStopDetailRepositoryTest {

    private lateinit var busApiService: BusApiService
    private lateinit var repository: ApiBusStopDetailRepository

    @Before
    fun setup() {
        busApiService = mockk()
        repository = ApiBusStopDetailRepository(busApiService)
    }

    @Test
    fun `when getBusStopDetail and getNextStopName called then results should match expected values`() = runTest {
        val expectedStopDetail = BusStopDetail(
            "화곡역4번출구", listOf(
                BusInfo(
                    "604", "화곡본동시장", listOf(
                        BusArrivalInfo("2분 38초후", "2번째 전", CongestionLevel.LOW),
                        BusArrivalInfo("3분 38초후", "3번째 전", CongestionLevel.MEDIUM)
                    )
                )
            )
        )
        val mockResponse = StopDetailResponse(
            StopDetailBody(
                listOf(
                    StopDetailItem(
                        "604",
                        "화곡역4번출구",
                        "화곡본동시장",
                        "2분 38초후[2번째 전]",
                        "3분 38초후[3번째 전]",
                        "3",
                        "4"
                    )
                )
            )
        )
        coEvery { busApiService.getStationByUid(stopId = TEST_STOP_ID) } returns mockResponse
        val stopDetail = repository.getBusStopDetail(TEST_STOP_ID)
        assertEquals(expectedStopDetail, stopDetail)

        val nextStopName = repository.getNextStopName(TEST_STOP_ID)
        assertEquals("화곡본동시장", nextStopName)
    }

    @Test
    fun `when getBusStopDetail throws IOException then empty BusStopDetail should be returned`() = runTest {
        coEvery { busApiService.getStationByUid(stopId = TEST_STOP_ID) } throws IOException("Failed to connect")
        val stopDetail = repository.getBusStopDetail(TEST_STOP_ID)
        assertEquals(EMPTY_STOP_DETAIL, stopDetail)
    }

    @Test
    fun `when StopDetailResponse has valid congestion values then correct levels should be returned`() = runTest {
        val stopDetail = getParsedStopDetailWithMock(createMockResponseWithCongestion("3", "5"))

        val firstCongestionLevel = stopDetail.busInfos.first().arrivalInfos[0].congestion
        assertEquals(CONGESTION_VALUE_THREE, firstCongestionLevel)

        val secondCongestionLevel = stopDetail.busInfos.first().arrivalInfos[1].congestion
        assertEquals(CONGESTION_VALUE_FIVE, secondCongestionLevel)
    }

    @Test
    fun `when StopDetailResponse has unknown congestion values then UNKNOWN level should be returned`() = runTest {
        val stopDetail = getParsedStopDetailWithMock(createMockResponseWithCongestion("1", null))

        val firstCongestionLevel = stopDetail.busInfos.first().arrivalInfos[0].congestion
        assertEquals(CONGESTION_VALUE_UNKNOWN, firstCongestionLevel)

        val secondCongestionLevel = stopDetail.busInfos.first().arrivalInfos[1].congestion
        assertEquals(CONGESTION_VALUE_UNKNOWN, secondCongestionLevel)
    }

    @Test
    fun `when StopDetailResponse has arrival messages then both should be parsed correctly`() = runTest {
        val stopDetail = getParsedStopDetailWithMock(createMockResponseWithArrMsg("1분 30초후[1번째 전]", "[막차]1분 30초후[1번째 전]"))

        val firstArrivalTime = stopDetail.busInfos.first().arrivalInfos[0].arrivalTime
        val firstPosition = stopDetail.busInfos.first().arrivalInfos[0].position
        assertEquals("1분 30초후", firstArrivalTime)
        assertEquals("1번째 전", firstPosition)

        val secondArrivalTime = stopDetail.busInfos.first().arrivalInfos[1].arrivalTime
        val secondPosition = stopDetail.busInfos.first().arrivalInfos[1].position
        assertEquals("1분 30초후", secondArrivalTime)
        assertEquals("1번째 전", secondPosition)
    }

    @Test
    fun `when StopDetailResponse has special arrival messages then they should be mapped correctly`() = runTest {
        val stopDetail = getParsedStopDetailWithMock(createMockResponseWithArrMsg("운행종료", "곧 도착"))

        val firstArrivalTime = stopDetail.busInfos.first().arrivalInfos[0].arrivalTime
        val firstPosition = stopDetail.busInfos.first().arrivalInfos[0].position
        assertEquals("", firstArrivalTime)
        assertEquals("운행종료", firstPosition)

        val secondArrivalTime = stopDetail.busInfos.first().arrivalInfos[1].arrivalTime
        val secondPosition = stopDetail.busInfos.first().arrivalInfos[1].position
        assertEquals("", secondArrivalTime)
        assertEquals("곧 도착", secondPosition)
    }

    @Test
    fun `when StopDetailResponse has no arrival messages then arrivalInfos should be null`() = runTest {
        val stopDetail = getParsedStopDetailWithMock(createMockResponseWithArrMsg(null, null))

        val firstArrivalInfo = stopDetail.busInfos.first().arrivalInfos.getOrNull(0)
        assertNull(firstArrivalInfo)

        val secondArrivalInfo = stopDetail.busInfos.first().arrivalInfos.getOrNull(1)
        assertNull(secondArrivalInfo)
    }

    private fun createMockResponseWithCongestion(firstBusCongestion: String?, secondBusCongestion: String?): StopDetailResponse {
        val busInfo = StopDetailItem(
            "604",
            "화곡역4번출구",
            "화곡본동시장",
            "2분 38초후[2번째 전]",
            "3분 38초후[3번째 전]",
            firstBusCongestion,
            secondBusCongestion
        )
        return StopDetailResponse(StopDetailBody(listOf(busInfo)))
    }

    private fun createMockResponseWithArrMsg(firstArrMsg: String?, secondArrMsg: String?): StopDetailResponse {
        val busInfo = StopDetailItem(
            "604",
            "화곡역4번출구",
            "화곡본동시장",
            firstArrMsg,
            secondArrMsg,
            "3",
            "4"
        )
        return StopDetailResponse(StopDetailBody(listOf(busInfo)))
    }

    private suspend fun getParsedStopDetailWithMock(mockResponse: StopDetailResponse): BusStopDetail {
        coEvery { busApiService.getStationByUid(stopId = TEST_STOP_ID) } returns mockResponse
        return repository.getBusStopDetail(TEST_STOP_ID)
    }

    companion object {
        private const val TEST_STOP_ID = "16206"
        private val EMPTY_STOP_DETAIL = BusStopDetail("", emptyList())
        private val CONGESTION_VALUE_THREE = CongestionLevel.LOW
        private val CONGESTION_VALUE_FIVE = CongestionLevel.HIGH
        private val CONGESTION_VALUE_UNKNOWN = CongestionLevel.UNKNOWN
    }
}
