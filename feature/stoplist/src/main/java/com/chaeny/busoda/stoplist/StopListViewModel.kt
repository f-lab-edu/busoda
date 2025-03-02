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
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
internal class StopListViewModel @Inject constructor(
    private val busStopRepository: BusStopRepository,
    private val busStopDetailRepository: BusStopDetailRepository
) : ViewModel() {

    private val _busStops = MutableLiveData<List<BusStop>>()
    private val _busStopClicked = MutableLiveData<Event<String>>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val keyWord: MutableStateFlow<String> = MutableStateFlow(EMPTY_KEYWORD)
    val busStops: LiveData<List<BusStop>> = _busStops
    val busStopClicked: LiveData<Event<String>> = _busStopClicked
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            keyWord.collect { newKeyWord ->
                if (newKeyWord != EMPTY_KEYWORD) {
                    loadBusStops(newKeyWord)
                }
            }
        }
    }

    private fun loadBusStops(stopName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val stops = busStopRepository.getBusStops(stopName)
            val updatedStops = stops.map { stop ->
                async {
                    stop.copy(nextStopName = busStopDetailRepository.getNextStopName(stop.stopId))
                }
            }.awaitAll()
            _busStops.value = updatedStops
            _isLoading.value = false
        }
    }

    fun handleBusStopClick(stopId: String) {
        _busStopClicked.value = Event(stopId)
    }

    fun setKeyWord(word: String) {
        keyWord.value = word
    }

    companion object {
        private const val EMPTY_KEYWORD = ""
    }
}
