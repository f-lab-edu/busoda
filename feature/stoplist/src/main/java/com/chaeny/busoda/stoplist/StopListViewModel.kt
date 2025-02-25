package com.chaeny.busoda.stoplist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.BusStopRepository
import com.chaeny.busoda.model.BusStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StopListViewModel @Inject constructor(
    private val busStopRepository: BusStopRepository,
    private val busStopDetailRepository: BusStopDetailRepository
) : ViewModel() {

    private val dummyData = MutableLiveData<List<BusStop>>()
    private val _removeCompleted = MutableLiveData<Event<RemoveResult>>()
    private val _busStopClicked = MutableLiveData<Event<String>>()
    private val _isLoading = MutableLiveData<Boolean>()
    val busStops: LiveData<List<BusStop>> = dummyData
    val removeCompleted: LiveData<Event<RemoveResult>> = _removeCompleted
    val busStopClicked: LiveData<Event<String>> = _busStopClicked
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadBusStops()
    }

    private fun loadBusStops() {
        _isLoading.value = true
        val stops = busStopRepository.getBusStops()
        viewModelScope.launch {
            val updatedStops = stops.map { stop ->
                async {
                    stop.copy(nextStopName = busStopDetailRepository.getNextStopName(stop.stopId))
                }
            }.awaitAll()
            dummyData.value = updatedStops
            _isLoading.value = false
        }
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
