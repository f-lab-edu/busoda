package com.chaeny.busoda.stoplist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.BusStopRepository
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.testing.util.MainCoroutineScopeRule
import com.chaeny.busoda.testing.util.getOrAwaitValue
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StopListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private lateinit var viewModel: StopListViewModel
    private lateinit var busStopRepository: BusStopRepository
    private lateinit var busStopDetailRepository: BusStopDetailRepository
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        busStopRepository = mockk()
        busStopDetailRepository = mockk()
        savedStateHandle = SavedStateHandle()
    }

    private fun createViewModel(
        initialBusStops: List<BusStop>,
        nextStopNames: Map<String, String>
    ): StopListViewModel {
        stubBusStopRepository(initialBusStops)
        stubBusStopDetailRepository(nextStopNames)
        return StopListViewModel(busStopRepository, busStopDetailRepository, savedStateHandle)
    }

    private fun stubBusStopRepository(busStops: List<BusStop>) {
        coEvery { busStopRepository.getBusStops(any()) } returns busStops
    }

    private fun stubBusStopDetailRepository(nextStopNames: Map<String, String>) {
        nextStopNames.forEach { (stopId, nextStop) ->
            coEvery { busStopDetailRepository.getNextStopName(stopId) } returns nextStop
        }
    }

    private fun verifyGetNextStopNameCalls(expectedCalls: Int) {
        coVerify(exactly = expectedCalls) {
            busStopDetailRepository.getNextStopName(any())
        }
    }

    @Test
    fun `when initialized then updatedBusStops should include nextStopName`() = runTest {
        val initialBusStops = listOf(
            BusStop("16206", "화곡역4번출구"),
            BusStop("16146", "화곡본동시장")
        )
        val nextStopNames = mapOf(
            "16206" to "화곡본동시장",
            "16146" to "한국폴리텍1.서울강서대학교"
        )
        viewModel = createViewModel(initialBusStops, nextStopNames)
        viewModel.setKeyWord(TEST_KEYWORD)
        advanceUntilIdle()
        val updatedBusStops = viewModel.busStops.getOrAwaitValue()
        verifyGetNextStopNameCalls(initialBusStops.size)
        val expectedBusStops = listOf(
            BusStop("16206", "화곡역4번출구", "화곡본동시장"),
            BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교")
        )
        assertEquals(expectedBusStops, updatedBusStops)
    }

    @Test
    fun `when data loading completes then isLoading should be false`() {
        viewModel = createViewModel(TEST_BUS_STOPS, TEST_NEXT_STOP_NAMES)
        viewModel.setKeyWord(TEST_KEYWORD)
        viewModel.busStops.getOrAwaitValue()
        coVerify {
            busStopRepository.getBusStops(any())
            busStopDetailRepository.getNextStopName(any())
        }
        val isLoading = viewModel.isLoading.getOrAwaitValue()
        assertFalse(isLoading)
    }

    @Test
    fun `when stop selected then should emit event with correct expected value`() {
        viewModel = createViewModel(TEST_BUS_STOPS, TEST_NEXT_STOP_NAMES)
        viewModel.handleBusStopClick(EXPECTED_STOP_ID)
        val clickEvent = viewModel.busStopClicked.getOrAwaitValue()
        assertEquals(EXPECTED_STOP_ID, clickEvent.getContentIfNotHandled())
    }

    companion object {
        private const val EXPECTED_STOP_ID = "16206"
        private val TEST_BUS_STOPS = listOf(
            BusStop("16206", "화곡역4번출구"),
            BusStop("16146", "화곡본동시장")
        )
        private val TEST_NEXT_STOP_NAMES = mapOf(
            "16206" to "화곡본동시장",
            "16146" to "한국폴리텍1.서울강서대학교"
        )
        private const val TEST_KEYWORD = "화곡역"
    }
}
