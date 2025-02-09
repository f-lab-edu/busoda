package com.chaeny.busoda.stoplist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chaeny.busoda.data.repository.BusStopRepository
import com.chaeny.busoda.model.BusStop
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class StopListViewModel @Inject constructor(busStopRepository: BusStopRepository) : ViewModel() {

    private val dummyData = MutableLiveData(busStopRepository.getBusStops())
    private val _removeCompleted = MutableLiveData<Event<RemoveResult>>()
    private val _busStopClicked = MutableLiveData<Event<String>>()
    val busStops: LiveData<List<BusStop>> = dummyData
    val removeCompleted: LiveData<Event<RemoveResult>> = _removeCompleted
    val busStopClicked: LiveData<Event<String>> = _busStopClicked

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
