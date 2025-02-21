package com.chaeny.busoda.stopdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.testing.util.MainCoroutineScopeRule
import com.chaeny.busoda.testing.util.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
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
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        repository = mockk()
        savedStateHandle = SavedStateHandle()
    }

    private fun initViewModel(
        stopId: String = TEST_STOP_ID,
        stopDetail: BusStopDetail = TEST_STOP_DETAIL
    ): StopDetailViewModel {
        savedStateHandle.set("stopId", stopId)
        coEvery { repository.getBusStopDetail(stopId) } returns stopDetail
        return StopDetailViewModel(repository, savedStateHandle)
    }

    @Test
    fun `when asyncDataLoad called then stopDetail should equal expected value`() {
        viewModel = initViewModel()
        val stopDetail = viewModel.stopDetail.getOrAwaitValue()
        assertEquals(TEST_STOP_DETAIL, stopDetail)
    }

    @Test
    fun `when stopId invalid then stopDetail should equal expected value`() {
        val invalidStopId = "0"
        viewModel = initViewModel(stopId = invalidStopId, stopDetail = EMPTY_STOP_DETAIL)

        val stopDetail = viewModel.stopDetail.getOrAwaitValue()
        assertEquals(EMPTY_STOP_DETAIL, stopDetail)
    }

    @Test
    fun `when data loading completes then isLoading should be false`() {
        viewModel = initViewModel()
        coVerify {
            repository.getBusStopDetail(any())
        }

        val isLoading = viewModel.isLoading.getOrAwaitValue()
        assertFalse(isLoading)
    }

    @Test
    fun `when initialized then stopId should match expected value`() {
        viewModel = initViewModel()
        val stopId = viewModel.stopId.getOrAwaitValue()
        assertEquals(TEST_STOP_ID, stopId)
    }

    companion object {
        private const val TEST_STOP_ID = "16206"
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
