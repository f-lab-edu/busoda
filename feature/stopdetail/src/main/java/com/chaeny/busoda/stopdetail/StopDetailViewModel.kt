package com.chaeny.busoda.stopdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.GetBusStopDetailResult
import com.chaeny.busoda.data.repository.FavoriteBusRepository
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.domain.usecase.AddFavoriteBusUseCase
import com.chaeny.busoda.domain.usecase.DeleteFavoriteStopUseCase
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.mvi.BaseViewModel
import com.chaeny.busoda.mvi.SideEffect
import com.chaeny.busoda.mvi.UiIntent
import com.chaeny.busoda.mvi.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StopDetailViewModel @Inject constructor(
    private val busStopDetailRepository: BusStopDetailRepository,
    private val favoriteRepository: FavoriteRepository,
    private val favoriteBusRepository: FavoriteBusRepository,
    private val addFavoriteBusUseCase: AddFavoriteBusUseCase,
    private val deleteFavoriteStopUseCase: DeleteFavoriteStopUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<StopDetailIntent, StopDetailUiState, StopDetailEffect>(
    initialState = StopDetailUiState(stopId = savedStateHandle.get(BUS_STOP_ID) ?: "")
) {

    private var currentCount = 15
    private var loadJob: Job? = null

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
            is StopDetailIntent.ToggleFavoriteBus -> toggleFavoriteBus(intent.busNumber)
            is StopDetailIntent.ConfirmDeleteFavorite -> confirmDeleteFavorite()
            is StopDetailIntent.CancelDeleteFavorite -> setState { copy(popup = null) }
        }
    }

    private fun asyncDataLoad() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = busStopDetailRepository.getBusStopDetail(currentState.stopId)
            if (result is GetBusStopDetailResult.Success) {
                val busStopDetail = result.data
                setState { copy(stopDetail = busStopDetail, isLoading = false) }
            } else {
                setState { copy(isLoading = false) }
            }
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
        if (currentState.favoriteBusNumbers.isNotEmpty()) {
            setState { copy(popup = Popup.DeleteStop) }
        } else {
            viewModelScope.launch {
                favoriteRepository.deleteFavorite(currentState.stopId)
                postSideEffect(StopDetailEffect.ShowFavoriteRemoved)
            }
        }
    }

    private fun confirmDeleteFavorite() {
        viewModelScope.launch {
            deleteFavoriteStopUseCase(currentState.stopId)
            setState { copy(popup = null) }
            postSideEffect(StopDetailEffect.ShowFavoriteRemoved)
        }
    }

    private fun toggleFavoriteBus(busNumber: String) {
        if (currentState.favoriteBusNumbers.contains(busNumber)) {
            removeFromFavoriteBus(busNumber)
        } else {
            addToFavoriteBus(busNumber)
        }
    }

    private fun addToFavoriteBus(busNumber: String) {
        viewModelScope.launch {
            addFavoriteBusUseCase(
                stopId = currentState.stopId,
                stopName = currentState.stopDetail.stopName,
                busNumber = busNumber,
                nextStopName = currentState.stopDetail.busInfos
                    .find { it.busNumber == busNumber }
                    ?.nextStopName ?: ""
            )
            postSideEffect(StopDetailEffect.ShowFavoriteAdded)
        }
    }

    private fun removeFromFavoriteBus(busNumber: String) {
        viewModelScope.launch {
            favoriteBusRepository.deleteFavoriteBus(currentState.stopId, busNumber)
            postSideEffect(StopDetailEffect.ShowFavoriteRemoved)
        }
    }

    companion object {
        private const val BUS_STOP_ID = "stopId"
    }
}

sealed class Popup {
    data object DeleteStop : Popup()
}

data class StopDetailUiState(
    val stopId: String = "",
    val stopDetail: BusStopDetail = BusStopDetail("", emptyList()),
    val isLoading: Boolean = false,
    val timer: Int = 15,
    val currentTime: Long = 0L,
    val isFavorite: Boolean = false,
    val favoriteBusNumbers: Set<String> = emptySet(),
    val popup: Popup? = null
) : UiState

sealed class StopDetailIntent : UiIntent {
    data object RefreshData : StopDetailIntent()
    data object ToggleFavorite : StopDetailIntent()
    data class ToggleFavoriteBus(val busNumber: String) : StopDetailIntent()
    data object ConfirmDeleteFavorite : StopDetailIntent()
    data object CancelDeleteFavorite : StopDetailIntent()
}

sealed class StopDetailEffect : SideEffect {
    data object RotateRefreshBtn : StopDetailEffect()
    data object ShowFavoriteAdded : StopDetailEffect()
    data object ShowFavoriteRemoved : StopDetailEffect()
}
