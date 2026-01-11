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

    private val _uiState = MutableStateFlow(StopListUiState())
    private val _searchEvent = MutableSharedFlow<SearchEvent>()
    private val _busStopClicked = MutableSharedFlow<String>()
    private val keyWord: MutableStateFlow<String> =
        MutableStateFlow(savedStateHandle.get(KEYWORD_SAVED_STATE_KEY) ?: EMPTY_KEYWORD)
    val uiState: StateFlow<StopListUiState> = _uiState
    val searchEvent : SharedFlow<SearchEvent> = _searchEvent
    val busStopClicked: SharedFlow<String> = _busStopClicked

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val busStops: StateFlow<List<BusStop>> = keyWord
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
        collectKeyWord()
        collectBusStops()
    }

    private fun collectKeyWord() {
        viewModelScope.launch {
            keyWord.collect { newKeyWord ->
                savedStateHandle.set(KEYWORD_SAVED_STATE_KEY, newKeyWord)
            }
        }
    }

    private fun collectBusStops() {
        viewModelScope.launch {
            busStops.collect { stops ->
                _uiState.value = _uiState.value.copy(busStops = stops)
            }
        }
    }

    private suspend fun loadBusStops(stopName: String): List<BusStop> {
        _uiState.value = _uiState.value.copy(isLoading = true)
        val result = busStopRepository.getBusStops(stopName)
        val updatedStops = when (result) {
            is GetBusStopResult.Success -> getUpdatedStops(result.data)
            is GetBusStopResult.NoResult -> {
                _searchEvent.emit(SearchEvent.NoResult)
                emptyList()
            }
            is GetBusStopResult.NoInternet -> {
                _searchEvent.emit(SearchEvent.NoInternet)
                emptyList()
            }
            is GetBusStopResult.NetworkError -> {
                _searchEvent.emit(SearchEvent.NetworkError)
                emptyList()
            }
        }
        _uiState.value = _uiState.value.copy(isLoading = false)
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
            _searchEvent.emit(SearchEvent.ShortKeyword)
        }
        return emptyList()
    }

    fun handleBusStopClick(stopId: String) {
        viewModelScope.launch {
            _busStopClicked.emit(stopId)
        }
    }

    fun handleIntent(intent: StopListIntent) {
        when (intent) {
            is StopListIntent.SetKeyWord -> {
                keyWord.value = intent.word.replace(" ", "")
            }
        }
    }

    companion object {
        private const val EMPTY_KEYWORD = ""
        private const val KEYWORD_SAVED_STATE_KEY = "KEYWORD_SAVED_STATE_KEY"
    }
}

data class StopListUiState(
    val busStops: List<BusStop> = emptyList(),
    val isLoading: Boolean = false
)

sealed class StopListIntent {
    data class SetKeyWord(val word: String) : StopListIntent()
}
