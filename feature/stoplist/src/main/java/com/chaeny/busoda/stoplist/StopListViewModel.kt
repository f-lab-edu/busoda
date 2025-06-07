package com.chaeny.busoda.stoplist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.BusStopRepository
import com.chaeny.busoda.data.repository.GetBusStopResult
import com.chaeny.busoda.model.BusStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StopListViewModel @Inject constructor(
    private val busStopRepository: BusStopRepository,
    private val busStopDetailRepository: BusStopDetailRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isNoResult = MutableSharedFlow<Boolean>()
    private val _isNoInternet = MutableSharedFlow<Boolean>()
    private val _isNetworkError = MutableSharedFlow<Boolean>()
    private val _isKeywordTooShort = MutableSharedFlow<Boolean>()
    private val _busStopClicked = MutableSharedFlow<String>()
    private val keyWord: MutableStateFlow<String> =
        MutableStateFlow(savedStateHandle.get(KEYWORD_SAVED_STATE_KEY) ?: EMPTY_KEYWORD)
    val isLoading: StateFlow<Boolean> = _isLoading
    val isNoResult: SharedFlow<Boolean> = _isNoResult
    val isNoInternet: SharedFlow<Boolean> = _isNoInternet
    val isNetworkError: SharedFlow<Boolean> = _isNetworkError
    val isKeywordTooShort: SharedFlow<Boolean> = _isKeywordTooShort
    val busStopClicked: SharedFlow<String> = _busStopClicked

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val busStops: StateFlow<List<BusStop>> = keyWord
        .debounce(1000)
        .mapLatest { word ->
            when {
                word.length > 2 -> loadBusStops(word)
                word.isEmpty() -> emptyList()
                else -> handleShortKeyword()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
        val updatedStops = when (result) {
            is GetBusStopResult.Success -> getUpdatedStops(result.data)
            is GetBusStopResult.NoResult -> {
                _isNoResult.emit(true)
                emptyList()
            }
            is GetBusStopResult.NoInternet -> {
                _isNoInternet.emit(true)
                emptyList()
            }
            is GetBusStopResult.NetworkError -> {
                _isNetworkError.emit(true)
                emptyList()
            }
        }
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
        viewModelScope.launch {
            _isKeywordTooShort.emit(true)
        }
        return emptyList()
    }

    fun handleBusStopClick(stopId: String) {
        viewModelScope.launch {
            _busStopClicked.emit(stopId)
        }
    }

    fun setKeyWord(word: String) {
        keyWord.value = word.replace(" ", "")
    }

    companion object {
        private const val EMPTY_KEYWORD = ""
        private const val KEYWORD_SAVED_STATE_KEY = "KEYWORD_SAVED_STATE_KEY"
    }
}
