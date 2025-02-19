package com.chaeny.busoda.stoplist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.BusStopRepository
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.stoplist.util.MainCoroutineScopeRule
import com.chaeny.busoda.stoplist.util.getOrAwaitValue
import io.mockk.coEvery
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
    fun initViewModel() {
        busStopRepository = mockk {
            coEvery { getBusStops() } returns TEST_BUS_STOPS
        }
        busStopDetailRepository = mockk {
            coEvery { getNextStopName("16206") } returns "화곡본동시장"
            coEvery { getNextStopName("16146") } returns "한국폴리텍1.서울강서대학교"
        }
        viewModel = StopListViewModel(busStopRepository, busStopDetailRepository)
    }

    @Test
    fun `when initialized then busStops should include nextStopName`() {
        val busStops = viewModel.busStops.getOrAwaitValue()
        assertEquals(EXPECTED_BUS_STOPS, busStops)
    }

    @Test
    fun `when data loading completes then isLoading should be false`() {
        val isLoading = viewModel.isLoading.getOrAwaitValue()
        assertFalse(isLoading)
    }

    @Test
    fun `when stop selected then should emit event with correct expected value`() {
        viewModel.handleBusStopClick(EXPECTED_STOP_ID)
        val clickEvent = viewModel.busStopClicked.getOrAwaitValue()
        assertEquals(EXPECTED_STOP_ID, clickEvent.getContentIfNotHandled())
    }

    @Test
    fun `when last stop removed then should decrease size and emit success`() {
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
