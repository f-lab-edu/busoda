package com.chaeny.busoda.stoplist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.BusStopRepository
import com.chaeny.busoda.model.BusStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StopListViewModel @Inject constructor(
    private val busStopRepository: BusStopRepository,
    private val busStopDetailRepository: BusStopDetailRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    private val _isNoResult = MutableLiveData<Event<Boolean>>()
    private val _isNetworkError = MutableLiveData<Event<Boolean>>()
    private val _isKeywordTooShort = MutableLiveData<Event<Boolean>>()
    private val _busStopClicked = MutableLiveData<Event<String>>()
    private val keyWord: MutableStateFlow<String> =
        MutableStateFlow(savedStateHandle.get(KEYWORD_SAVED_STATE_KEY) ?: EMPTY_KEYWORD)
    val isLoading: LiveData<Boolean> = _isLoading
    val isNoResult: LiveData<Event<Boolean>> = _isNoResult
    val isNetworkError: LiveData<Event<Boolean>> = _isNetworkError
    val isKeywordTooShort: LiveData<Event<Boolean>> = _isKeywordTooShort
    val busStopClicked: LiveData<Event<String>> = _busStopClicked

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val busStops: LiveData<List<BusStop>> = keyWord
        .debounce(1000)
        .mapLatest { word ->
            when {
                word.length > 2 -> loadBusStops(word)
                word.isEmpty() -> emptyList()
                else -> handleShortKeyword()
            }
        }.asLiveData()

    init {
        viewModelScope.launch {
            keyWord.collect { newKeyWord ->
                savedStateHandle.set(KEYWORD_SAVED_STATE_KEY, newKeyWord)
            }
        }
    }

    private suspend fun loadBusStops(stopName: String): List<BusStop> {
        _isLoading.value = true
        val result = busStopRepository.getBusStops(stopName)

        if (result.isNetworkError) {
            _isNetworkError.value = Event(true)
            _isLoading.value = false
            return emptyList()
        }

        if (result.isNoResult) {
            _isNoResult.value = Event(true)
            _isLoading.value = false
            return emptyList()
        }

        val updatedStops = getUpdatedStops(result.data)
        _isLoading.value = false
        return updatedStops
    }

    private suspend fun getUpdatedStops(stops: List<BusStop>): List<BusStop> {
        return coroutineScope {
            stops.map { stop ->
                async {
                    stop.copy(nextStopName = busStopDetailRepository.getNextStopName(stop.stopId))
                }
            }.awaitAll()
        }
    }

    private fun handleShortKeyword(): List<BusStop> {
        _isKeywordTooShort.value = Event(true)
        return emptyList()
    }

    fun handleBusStopClick(stopId: String) {
        _busStopClicked.value = Event(stopId)
    }

    fun setKeyWord(word: String) {
        keyWord.value = word
    }

    companion object {
        private const val EMPTY_KEYWORD = ""
        private const val KEYWORD_SAVED_STATE_KEY = "KEYWORD_SAVED_STATE_KEY"
    }
}
