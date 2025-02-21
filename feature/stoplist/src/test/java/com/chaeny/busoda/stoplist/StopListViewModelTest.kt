package com.chaeny.busoda.stoplist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.BusStopRepository
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.testing.util.getOrAwaitValue
import com.chaeny.busoda.testing.util.MainCoroutineScopeRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StopListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private lateinit var viewModel: StopListViewModel
    private lateinit var busStopRepository: BusStopRepository
    private lateinit var busStopDetailRepository: BusStopDetailRepository

    @Before
    fun setup() {
        busStopRepository = mockk()
        busStopDetailRepository = mockk()
    }

    private fun initViewModel(
        busStops: List<BusStop>, nextStopNames: Map<String, String>
    ): StopListViewModel {
        coEvery { busStopRepository.getBusStops() } returns busStops
        nextStopNames.forEach { (stopId, nextStop) ->
            coEvery { busStopDetailRepository.getNextStopName(stopId) } returns nextStop
        }
        return StopListViewModel(busStopRepository, busStopDetailRepository)
    }

    @Test
    fun `when initialized then busStops should include nextStopName`() {
        val inputBusStops = listOf(
            BusStop("16206", "화곡역4번출구"),
            BusStop("16146", "화곡본동시장")
        )
        val nextStopNames = mapOf(
            "16206" to "화곡본동시장",
            "16146" to "한국폴리텍1.서울강서대학교"
        )
        viewModel = initViewModel(inputBusStops, nextStopNames)
        coVerify(exactly = inputBusStops.size) {
            busStopDetailRepository.getNextStopName(any())
        }

        val busStops = viewModel.busStops.getOrAwaitValue()
        val expectedBusStops = listOf(
            BusStop("16206", "화곡역4번출구", "화곡본동시장"),
            BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교")
        )
        assertEquals(expectedBusStops, busStops)
    }

    @Test
    fun `when data loading completes then isLoading should be false`() {
        initViewModel(TEST_BUS_STOPS, TEST_NEXT_STOP_NAMES)
        coVerify {
            busStopRepository.getBusStops()
            busStopDetailRepository.getNextStopName(any())
        }
        val isLoading = viewModel.isLoading.getOrAwaitValue()
        assertFalse(isLoading)
    }

    @Test
    fun `when stop selected then should emit event with correct expected value`() {
        initViewModel(TEST_BUS_STOPS, TEST_NEXT_STOP_NAMES)
        viewModel.handleBusStopClick(EXPECTED_STOP_ID)
        val clickEvent = viewModel.busStopClicked.getOrAwaitValue()
        assertEquals(EXPECTED_STOP_ID, clickEvent.getContentIfNotHandled())
    }

    @Test
    fun `when last stop removed then should decrease size and emit success`() {
        initViewModel(TEST_BUS_STOPS, TEST_NEXT_STOP_NAMES)
        val expectedSize = viewModel.busStops.getOrAwaitValue().size - 1
        viewModel.removeLastStop()
        val busStopsSize = viewModel.busStops.getOrAwaitValue().size
        assertEquals(expectedSize, busStopsSize)

        val removeEvent = viewModel.removeCompleted.getOrAwaitValue()
        assertEquals(RemoveResult.SUCCESS, removeEvent.getContentIfNotHandled())
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
    }
}
