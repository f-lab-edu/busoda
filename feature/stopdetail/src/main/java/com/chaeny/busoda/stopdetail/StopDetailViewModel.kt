package com.chaeny.busoda.stopdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.FavoriteBusRepository
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.mvi.BaseViewModel
import com.chaeny.busoda.mvi.SideEffect
import com.chaeny.busoda.mvi.UiIntent
import com.chaeny.busoda.mvi.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StopDetailViewModel @Inject constructor(
    private val busStopDetailRepository: BusStopDetailRepository,
    private val favoriteRepository: FavoriteRepository,
    private val favoriteBusRepository: FavoriteBusRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<StopDetailIntent, StopDetailUiState, StopDetailEffect>(
    initialState = StopDetailUiState(stopId = savedStateHandle.get(BUS_STOP_ID) ?: "")
) {

    private var currentCount = 15

    init {
        asyncDataLoad()
        startTimer()
        collectIsFavorite()
        collectFavoriteBuses()
    }

    override fun onIntent(intent: StopDetailIntent) {
        when (intent) {
            is StopDetailIntent.RefreshData -> refreshData()
            is StopDetailIntent.ToggleFavorite -> toggleFavorite()
            is StopDetailIntent.ToggleBusFavorite -> {
                addToBusFavorites(intent.busNumber)
            }
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

    private fun collectIsFavorite() {
        viewModelScope.launch {
            favoriteRepository.isFavorite(currentState.stopId).collect { isFavorite ->
                setState { copy(isFavorite = isFavorite) }
            }
        }
    }

    private fun collectFavoriteBuses() {
        viewModelScope.launch {
            favoriteBusRepository.getFavoriteBuses().collect { busList ->
                val favoriteBusNumbers = busList
                    .filter { it.stopId == currentState.stopId }
                    .map { it.busNumber }
                    .toSet()
                setState { copy(favoriteBusNumbers = favoriteBusNumbers) }
            }
        }
    }

    private fun refreshData() {
        currentCount = 15
        postSideEffect(StopDetailEffect.RotateRefreshBtn)
        asyncDataLoad()
    }

    private fun toggleFavorite() {
        if (currentState.isFavorite) {
            removeFromFavorites()
        } else {
            addToFavorites()
        }
    }

    private fun addToFavorites() {
        viewModelScope.launch {
            favoriteRepository.addFavorite(
                BusStop(
                    currentState.stopId,
                    currentState.stopDetail.stopName,
                    currentState.stopDetail.busInfos.firstOrNull()?.nextStopName ?: ""
                )
            )
            postSideEffect(StopDetailEffect.ShowFavoriteAdded)
        }
    }

    private fun removeFromFavorites() {
        viewModelScope.launch {
            favoriteRepository.deleteFavorite(currentState.stopId)
            postSideEffect(StopDetailEffect.ShowFavoriteRemoved)
        }
    }

    private fun addToBusFavorites(busNumber: String) {
        viewModelScope.launch {
            favoriteBusRepository.addFavoriteBus(
                stopId = currentState.stopId,
                stopName = currentState.stopDetail.stopName,
                busNumber = busNumber,
                nextStopName = currentState.stopDetail.busInfos
                    .find { it.busNumber == busNumber }
                    ?.nextStopName ?: ""
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
    val currentTime: Long = 0L,
    val isFavorite: Boolean = false,
    val favoriteBusNumbers: Set<String> = emptySet()
) : UiState

sealed class StopDetailIntent : UiIntent {
    data object RefreshData : StopDetailIntent()
    data object ToggleFavorite : StopDetailIntent()
    data class ToggleBusFavorite(val busNumber: String) : StopDetailIntent()
}

sealed class StopDetailEffect : SideEffect {
    data object RotateRefreshBtn : StopDetailEffect()
    data object ShowFavoriteAdded : StopDetailEffect()
    data object ShowFavoriteRemoved : StopDetailEffect()
}
