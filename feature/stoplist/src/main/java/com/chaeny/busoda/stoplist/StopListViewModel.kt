package com.chaeny.busoda.stoplist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.BusStopRepository
import com.chaeny.busoda.model.BusStop
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class StopListViewModel @Inject constructor(
    private val busStopRepository: BusStopRepository,
    private val busStopDetailRepository: BusStopDetailRepository
) : ViewModel() {

    private val dummyData = MutableLiveData<List<BusStop>>()
    private val _removeCompleted = MutableLiveData<Event<RemoveResult>>()
    private val _busStopClicked = MutableLiveData<Event<String>>()
    val busStops: LiveData<List<BusStop>> = dummyData
    val removeCompleted: LiveData<Event<RemoveResult>> = _removeCompleted
    val busStopClicked: LiveData<Event<String>> = _busStopClicked

    init {
        loadBusStops()
    }

    private fun loadBusStops() {
        val stops = busStopRepository.getBusStops()
        stops.forEach { stop ->
            stop.nextStopName = busStopDetailRepository.getNextStopName(stop.stopId)
        }
        dummyData.value = stops
    }

    fun removeLastStop() {
        val currentStops = dummyData.value
        if (currentStops.isNullOrEmpty()) {
            return
        }
        dummyData.value = currentStops.dropLast(1)
        _removeCompleted.value = Event(RemoveResult.SUCCESS)
    }

    fun handleBusStopClick(stopId: String) {
        _busStopClicked.value = Event(stopId)
    }
}
