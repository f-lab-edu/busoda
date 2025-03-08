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
        val mockResponse = createStopDetailResponse()
        coEvery { busApiService.getStationByUid(stopId = TEST_STOP_ID) } returns mockResponse
        val stopDetail = repository.getBusStopDetail(TEST_STOP_ID)
        val nextStopName = repository.getNextStopName(TEST_STOP_ID)
        assertEquals(EXPECTED_STOP_DETAIL, stopDetail)
        assertEquals(EXPECTED_NEXT_STOP_NAME, nextStopName)
    }

    @Test
    fun `when getBusStopDetail throws IOException then empty BusStopDetail should be returned`() = runTest {
        coEvery { busApiService.getStationByUid(stopId = TEST_STOP_ID) } throws IOException("Failed to connect")
        val stopDetail = repository.getBusStopDetail(TEST_STOP_ID)
        assertEquals(EMPTY_STOP_DETAIL, stopDetail)
    }

    @Test
    fun `when StopDetailResponse has valid congestion values then correct levels should be returned`() = runTest {
        val mockResponse = createStopDetailResponseWithCongestion("3", "5")
        coEvery { busApiService.getStationByUid(stopId = TEST_STOP_ID) } returns mockResponse
        val stopDetail = repository.getBusStopDetail(TEST_STOP_ID)

        val firstCongestionLevel = stopDetail.busInfos.first().arrivalInfos[0].congestion
        assertEquals(CONGESTION_VALUE_THREE, firstCongestionLevel)

        val secondCongestionLevel = stopDetail.busInfos.first().arrivalInfos[1].congestion
        assertEquals(CONGESTION_VALUE_FIVE, secondCongestionLevel)
    }

    @Test
    fun `when StopDetailResponse has unknown congestion values then UNKNOWN level should be returned`() = runTest {
        val mockResponse = createStopDetailResponseWithCongestion("1", null)
        coEvery { busApiService.getStationByUid(stopId = TEST_STOP_ID) } returns mockResponse
        val stopDetail = repository.getBusStopDetail(TEST_STOP_ID)

        val firstCongestionLevel = stopDetail.busInfos.first().arrivalInfos[0].congestion
        assertEquals(CONGESTION_VALUE_UNKNOWN, firstCongestionLevel)

        val secondCongestionLevel = stopDetail.busInfos.first().arrivalInfos[1].congestion
        assertEquals(CONGESTION_VALUE_UNKNOWN, secondCongestionLevel)
    }

    private fun createStopDetailResponse(): StopDetailResponse {
        val busInfo = StopDetailItem(
            "604",
            "화곡역4번출구",
            "화곡본동시장",
            "2분 38초후[2번째 전]",
            "3분 38초후[3번째 전]",
            "3",
            "4"
        )
        return StopDetailResponse(StopDetailBody(listOf(busInfo)))
    }

    private fun createStopDetailResponseWithCongestion(firstBusCongestion: String?, secondBusCongestion: String?): StopDetailResponse {
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

    companion object {
        private const val TEST_STOP_ID = "16206"
        private val EXPECTED_NEXT_STOP_NAME = "화곡본동시장"
        val EXPECTED_STOP_DETAIL = BusStopDetail(
            "화곡역4번출구", listOf(
                BusInfo(
                    "604", "화곡본동시장", listOf(
                        BusArrivalInfo("2분 38초후", "2번째 전", CongestionLevel.LOW),
                        BusArrivalInfo("3분 38초후", "3번째 전", CongestionLevel.MEDIUM)
                    )
                )
            )
        )
        private val EMPTY_STOP_DETAIL = BusStopDetail("", emptyList())
        private val CONGESTION_VALUE_THREE = CongestionLevel.LOW
        private val CONGESTION_VALUE_FIVE = CongestionLevel.HIGH
        private val CONGESTION_VALUE_UNKNOWN = CongestionLevel.UNKNOWN
    }
}
