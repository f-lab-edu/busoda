package com.chaeny.busoda.stoplist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chaeny.busoda.stoplist.util.getOrAwaitValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StopListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: StopListViewModel

    @Before
    fun initViewModel() {
        viewModel = StopListViewModel()
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
    }
}
