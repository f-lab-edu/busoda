package com.chaeny.busoda.stopdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.mvi.BaseViewModel
import com.chaeny.busoda.mvi.SideEffect
import com.chaeny.busoda.mvi.UiIntent
import com.chaeny.busoda.mvi.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StopDetailViewModel @Inject constructor(
    private val busStopDetailRepository: BusStopDetailRepository,
    private val favoriteRepository: FavoriteRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<StopDetailIntent, StopDetailUiState, StopDetailEffect>(
    initialState = StopDetailUiState(stopId = savedStateHandle.get(BUS_STOP_ID) ?: "")
) {

    private var currentCount = 15
    private val _refreshEvent = MutableSharedFlow<Unit>()
    val refreshEvent: SharedFlow<Unit> = _refreshEvent

    init {
        asyncDataLoad()
        startTimer()
    }

    override fun onIntent(intent: StopDetailIntent) {
        when (intent) {
            is StopDetailIntent.RefreshData -> refreshData()
        }
    }

    private fun asyncDataLoad() {
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            val busStopDetail = busStopDetailRepository.getBusStopDetail(currentState.stopId)
            setState { copy(stopDetail = busStopDetail, isLoading = false) }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                setState {
                    copy(
                        timer = currentCount,
                        currentTime = System.currentTimeMillis() / 1000
                    )
                }
                delay(1000)
                currentCount--

                if (currentCount == 0) {
                    refreshData()
                }
            }
        }
    }

    private fun refreshData() {
        currentCount = 15
        viewModelScope.launch {
            _refreshEvent.emit(Unit)
        }
        asyncDataLoad()
    }

    fun addToFavorites() {
        viewModelScope.launch {
            favoriteRepository.addFavorite(
                BusStop(
                    currentState.stopId,
                    currentState.stopDetail.stopName,
                    currentState.stopDetail.busInfos.firstOrNull()?.nextStopName ?: ""
                )
            )
        }
    }

    companion object {
        private const val BUS_STOP_ID = "stopId"
    }
}

data class StopDetailUiState(
    val stopId: String = "",
    val stopDetail: BusStopDetail = BusStopDetail("", emptyList()),
    val isLoading: Boolean = false,
    val timer: Int = 15,
    val currentTime: Long = 0L
) : UiState

sealed class StopDetailIntent : UiIntent {
    data object RefreshData : StopDetailIntent()
}

sealed class StopDetailEffect : SideEffect
