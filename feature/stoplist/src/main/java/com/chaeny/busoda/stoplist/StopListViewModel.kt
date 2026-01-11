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
    private val _effect = MutableSharedFlow<StopListEffect>()
    private val keyWord: MutableStateFlow<String> =
        MutableStateFlow(savedStateHandle.get(KEYWORD_SAVED_STATE_KEY) ?: EMPTY_KEYWORD)
    val uiState: StateFlow<StopListUiState> = _uiState
    val effect: SharedFlow<StopListEffect> = _effect

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
                _effect.emit(StopListEffect.ShowNoResult)
                emptyList()
            }
            is GetBusStopResult.NoInternet -> {
                _effect.emit(StopListEffect.ShowNoInternet)
                emptyList()
            }
            is GetBusStopResult.NetworkError -> {
                _effect.emit(StopListEffect.ShowNetworkError)
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
            _effect.emit(StopListEffect.ShowShortKeyword)
        }
        return emptyList()
    }

    fun handleIntent(intent: StopListIntent) {
        when (intent) {
            is StopListIntent.SetKeyWord -> {
                keyWord.value = intent.word.replace(" ", "")
            }
            is StopListIntent.ClickBusStop -> {
                viewModelScope.launch {
                    _effect.emit(StopListEffect.NavigateToStopDetail(intent.stopId))
                }
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
    data class ClickBusStop(val stopId: String) : StopListIntent()
}

sealed class StopListEffect {
    data class NavigateToStopDetail(val stopId: String) : StopListEffect()
    data object ShowNoResult : StopListEffect()
    data object ShowNoInternet : StopListEffect()
    data object ShowNetworkError : StopListEffect()
    data object ShowShortKeyword : StopListEffect()
}
