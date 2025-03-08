package com.chaeny.busoda.data.repository

import com.chaeny.busoda.data.model.StopDetailBody
import com.chaeny.busoda.data.model.StopDetailItem
import com.chaeny.busoda.data.model.StopDetailResponse
import com.chaeny.busoda.data.network.BusApiService
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.model.CongestionLevel
import kotlinx.coroutines.test.runTest
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals

class ApiBusStopDetailRepositoryTest {

    private lateinit var busApiService: BusApiService
    private lateinit var repository: ApiBusStopDetailRepository

    @Before
    fun setup() {
        busApiService = mockk()
        repository = ApiBusStopDetailRepository(busApiService)
    }

    @Test
    fun `when getBusStopDetail called then returned BusStopDetail should equal expected value`() = runTest {
        val stopId = "16206"
        val expected = BusStopDetail(
            "화곡역4번출구", listOf(
                BusInfo(
                    "604", "화곡본동시장", listOf(
                        BusArrivalInfo("2분 38초 ", "2번째 전", CongestionLevel.LOW),
                        BusArrivalInfo("3분 38초 ", "3번째 전", CongestionLevel.MEDIUM)
                    )
                )
            )
        )
        val mockResponse = StopDetailResponse(
            StopDetailBody(
                busInfos = listOf(
                    StopDetailItem(
                        "604",
                        "화곡역4번출구",
                        "화곡본동시장",
                        "2분 38초 [2번째 전]",
                        "3분 38초 [3번째 전]",
                        "3",
                        "4"
                    )
                )
            )
        )
        coEvery { busApiService.getStationByUid(stopId = stopId) } returns mockResponse
        val result = repository.getBusStopDetail(stopId)
        assertEquals(expected, result)
    }
}
