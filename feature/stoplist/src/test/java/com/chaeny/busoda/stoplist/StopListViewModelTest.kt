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

    private fun createViewModel(
        initialBusStops: List<BusStop>,
        nextStopNames: Map<String, String>
    ): StopListViewModel {
        stubBusStopRepository(initialBusStops)
        stubBusStopDetailRepository(nextStopNames)
        return StopListViewModel(busStopRepository, busStopDetailRepository)
    }

    private fun stubBusStopRepository(busStops: List<BusStop>) {
        coEvery { busStopRepository.getBusStops() } returns busStops
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
    fun `when initialized then updatedBusStops should include nextStopName`() {
        val initialBusStops = listOf(
            BusStop("16206", "화곡역4번출구"),
            BusStop("16146", "화곡본동시장")
        )
        val nextStopNames = mapOf(
            "16206" to "화곡본동시장",
            "16146" to "한국폴리텍1.서울강서대학교"
        )
        viewModel = createViewModel(initialBusStops, nextStopNames)
        verifyGetNextStopNameCalls(initialBusStops.size)

        val updatedBusStops = viewModel.busStops.getOrAwaitValue()
        val expectedBusStops = listOf(
            BusStop("16206", "화곡역4번출구", "화곡본동시장"),
            BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교")
        )
        assertEquals(expectedBusStops, updatedBusStops)
    }

    @Test
    fun `when data loading completes then isLoading should be false`() {
        viewModel = createViewModel(TEST_BUS_STOPS, TEST_NEXT_STOP_NAMES)
        coVerify {
            busStopRepository.getBusStops()
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
    }
}
