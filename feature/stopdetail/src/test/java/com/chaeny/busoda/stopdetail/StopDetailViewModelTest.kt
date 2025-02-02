package com.chaeny.busoda.stopdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.chaeny.busoda.stopdetail.util.MainCoroutineScopeRule
import com.chaeny.busoda.stopdetail.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class StopDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private lateinit var viewModel: StopDetailViewModel

    @Before
    fun initViewModel() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle.set(STOP_ID_KEY, TEST_STOP_ID)
        viewModel = StopDetailViewModel(savedStateHandle)
    }

    @Test
    fun `when initialized then stopDetail should be null`() {
        val stopDetail = viewModel.stopDetail.value
        assertNull(stopDetail)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when asyncDataLoad called then stopName should equal expected value`() = runTest {
        advanceUntilIdle()
        val stopName = viewModel.stopDetail.getOrAwaitValue().stopName
        assertTrue("stopName should not be empty", stopName.isNotEmpty())
        assertEquals(EXPECTED_STOP_NAME, stopName)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when stopId invalid then busInfos should be empty`() = runTest {
        val invalidStopId = "0"
        val savedStateHandle = SavedStateHandle()
        savedStateHandle.set(STOP_ID_KEY, invalidStopId)
        viewModel = StopDetailViewModel(savedStateHandle)

        advanceUntilIdle()
        val busInfos = viewModel.stopDetail.getOrAwaitValue().busInfos
        assertTrue(busInfos.isEmpty())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when bus arrival info loaded then should contain expected values`() = runTest {
        advanceUntilIdle()
        val busInfos = viewModel.stopDetail.getOrAwaitValue().busInfos
        assertTrue("BusInfos should not be empty", busInfos.isNotEmpty())

        val arrivalInfo = busInfos.first().arrivalInfos.first()
        assertTrue(EXPECTED_TIME_UNIT in arrivalInfo.arrivalTime)
        assertTrue(EXPECTED_POSITION_UNIT in arrivalInfo.position)
        assertTrue(arrivalInfo.congestion in CONGESTION_VALUES)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `when data loading starts and completes then isLoading should be true then false`() = runTest {
        var isLoading = viewModel.isLoading.getOrAwaitValue()
        assertTrue(isLoading)
        advanceUntilIdle()
        isLoading = viewModel.isLoading.getOrAwaitValue()
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
    }
}
