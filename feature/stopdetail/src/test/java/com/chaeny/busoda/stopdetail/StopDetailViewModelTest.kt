package com.chaeny.busoda.stopdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.stopdetail.util.MainCoroutineScopeRule
import com.chaeny.busoda.stopdetail.util.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StopDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private lateinit var viewModel: StopDetailViewModel
    private lateinit var repository: BusStopDetailRepository

    @Before
    fun initViewModel() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle.set(STOP_ID_KEY, TEST_STOP_ID)
        repository = mockk {
            coEvery { getBusStopDetail(TEST_STOP_ID) } returns TEST_STOP_DETAIL
        }
        viewModel = StopDetailViewModel(repository, savedStateHandle)
    }

    @Test
    fun `when asyncDataLoad called then stopName should equal expected value`() {
        val stopName = viewModel.stopDetail.getOrAwaitValue().stopName
        assertTrue("stopName should not be empty", stopName.isNotEmpty())
        assertEquals(EXPECTED_STOP_NAME, stopName)
    }

    @Test
    fun `when stopId invalid then busInfos should be empty`() {
        val invalidStopId = "0"
        val savedStateHandle = SavedStateHandle()
        savedStateHandle.set(STOP_ID_KEY, invalidStopId)
        repository = mockk {
            coEvery { getBusStopDetail(invalidStopId) } returns EMPTY_STOP_DETAIL
        }
        viewModel = StopDetailViewModel(repository, savedStateHandle)

        val busInfos = viewModel.stopDetail.getOrAwaitValue().busInfos
        assertTrue(busInfos.isEmpty())
    }

    @Test
    fun `when bus arrival info loaded then should contain expected values`() {
        val busInfos = viewModel.stopDetail.getOrAwaitValue().busInfos
        assertTrue("BusInfos should not be empty", busInfos.isNotEmpty())

        val arrivalInfo = busInfos.first().arrivalInfos.first()
        assertTrue(EXPECTED_TIME_UNIT in arrivalInfo.arrivalTime)
        assertTrue(EXPECTED_POSITION_UNIT in arrivalInfo.position)
        assertTrue(arrivalInfo.congestion in CONGESTION_VALUES)
    }

    @Test
    fun `when data loading completes then isLoading should be false`() {
        val isLoading = viewModel.isLoading.getOrAwaitValue()
        assertFalse(isLoading)
    }

    @Test
    fun `when initialized then stopId should match expected value`() {
        val stopId = viewModel.stopId.getOrAwaitValue()
        assertEquals(TEST_STOP_ID, stopId)
    }

    companion object {
        private const val STOP_ID_KEY = "stopId"
        private const val TEST_STOP_ID = "16206"
        private const val EXPECTED_STOP_NAME = "화곡역4번출구"
        private const val EXPECTED_TIME_UNIT = "분"
        private const val EXPECTED_POSITION_UNIT = "번째"
        private val CONGESTION_VALUES = listOf("여유", "보통", "혼잡", "매우혼잡")
        private val EMPTY_STOP_DETAIL = BusStopDetail("", emptyList())
        private val TEST_STOP_DETAIL = BusStopDetail(
            "화곡역4번출구", listOf(
                BusInfo(
                    "604", "화곡본동시장", listOf(
                        BusArrivalInfo("2분 38초", "2번째 전", "보통")
                    )
                )
            )
        )
    }
}
