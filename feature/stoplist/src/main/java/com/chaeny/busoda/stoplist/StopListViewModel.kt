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

    private val _busStopClicked = MutableLiveData<Event<String>>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _isNoResult = MutableLiveData<Boolean>()
    private val _isNetworkError = MutableLiveData<Boolean>()
    private val keyWord: MutableStateFlow<String> =
        MutableStateFlow(savedStateHandle.get(KEYWORD_SAVED_STATE_KEY) ?: EMPTY_KEYWORD)
    val busStopClicked: LiveData<Event<String>> = _busStopClicked
    val isLoading: LiveData<Boolean> = _isLoading
    val isNoResult: LiveData<Boolean> = _isNoResult
    val isNetworkError: LiveData<Boolean> = _isNetworkError

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val busStops: LiveData<List<BusStop>> = keyWord
        .debounce(1000)
        .mapLatest { word ->
            when {
                word.length > 2 -> loadBusStops(word)
                else -> emptyList()
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

        when {
            result.isNetworkError -> {
                _isNetworkError.value = true
                _isLoading.value = false
                return emptyList()
            }

            result.isNoResult -> {
                _isNoResult.value = true
                _isLoading.value = false
                return emptyList()
            }

            else -> {
                val updatedStops = coroutineScope {
                    result.data.map { stop ->
                        async {
                            stop.copy(nextStopName = busStopDetailRepository.getNextStopName(stop.stopId))
                        }
                    }.awaitAll()
                }
                _isLoading.value = false
                return updatedStops
            }
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
        private const val KEYWORD_SAVED_STATE_KEY = "KEYWORD_SAVED_STATE_KEY"
    }
}
