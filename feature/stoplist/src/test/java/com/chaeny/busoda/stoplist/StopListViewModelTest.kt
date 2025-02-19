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

    private fun initViewModel() {
        coEvery { busStopRepository.getBusStops() } returns TEST_BUS_STOPS
        coEvery { busStopDetailRepository.getNextStopName("16206") } returns "화곡본동시장"
        coEvery { busStopDetailRepository.getNextStopName("16146") } returns "한국폴리텍1.서울강서대학교"
        viewModel = StopListViewModel(busStopRepository, busStopDetailRepository)
    }

    @Test
    fun `when initialized then busStops should include nextStopName`() {
        initViewModel()
        val busStops = viewModel.busStops.getOrAwaitValue()
        assertEquals(EXPECTED_BUS_STOPS, busStops)
    }

    @Test
    fun `when data loading completes then isLoading should be false`() {
        initViewModel()
        coVerify {
            busStopRepository.getBusStops()
            busStopDetailRepository.getNextStopName(any())
        }

        val isLoading = viewModel.isLoading.getOrAwaitValue()
        assertFalse(isLoading)
    }

    @Test
    fun `when stop selected then should emit event with correct expected value`() {
        initViewModel()
        viewModel.handleBusStopClick(EXPECTED_STOP_ID)
        val clickEvent = viewModel.busStopClicked.getOrAwaitValue()
        assertEquals(EXPECTED_STOP_ID, clickEvent.getContentIfNotHandled())
    }

    @Test
    fun `when last stop removed then should decrease size and emit success`() {
        initViewModel()
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
        private val EXPECTED_BUS_STOPS = listOf(
            BusStop("16206", "화곡역4번출구", "화곡본동시장"),
            BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교")
        )
    }
}
