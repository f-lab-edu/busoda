package com.chaeny.busoda.favorites

import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.FavoriteBusRepository
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.model.FavoriteBusItem
import com.chaeny.busoda.mvi.BaseViewModel
import com.chaeny.busoda.mvi.SideEffect
import com.chaeny.busoda.mvi.UiIntent
import com.chaeny.busoda.mvi.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val favoriteBusRepository: FavoriteBusRepository,
    private val busStopDetailRepository: BusStopDetailRepository
) : BaseViewModel<FavoritesIntent, FavoritesUiState, FavoritesEffect>(
    initialState = FavoritesUiState()
) {

    private var currentCount = REFRESH_INTERVAL_SECONDS
    private var loadJob: Job? = null

    init {
        collectFavoriteStops()
        collectFavoriteBuses()
        startTimer()
    }

    private fun collectFavoriteStops() {
        viewModelScope.launch {
            favoriteRepository.getFavoriteStops().collect { favoriteStops ->
                setState { copy(favoriteStops = favoriteStops) }
            }
        }
    }

    private fun collectFavoriteBuses() {
        viewModelScope.launch {
            favoriteBusRepository.getFavoriteBuses().collect { favoriteBuses ->
                val favoriteBusesByStop = favoriteBuses.groupBy { it.stopId }
                setState { copy(favoriteBuses = favoriteBusesByStop) }

                if (favoriteBuses.isNotEmpty()) {
                    refreshBusInfo()
                }
            }
        }
    }

    private suspend fun loadFavoriteBusInfo() {
        val busInfoMap = getBusStopDetailsMap()
        setState {
            copy(
                favoriteBusInfo = busInfoMap,
                currentTime = System.currentTimeMillis() / 1000
            )
        }
    }

    private suspend fun getBusStopDetailsMap(): Map<String, BusStopDetail> {
        val stopIds = currentState.favoriteBuses.keys

        return coroutineScope {
            stopIds.map { stopId ->
                async {
                    val busStopDetail = busStopDetailRepository.getBusStopDetail(stopId)
                    stopId to busStopDetail
                }
            }.awaitAll().toMap()
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
                    currentCount = REFRESH_INTERVAL_SECONDS
                    if (currentState.favoriteBuses.isNotEmpty()) {
                        refreshBusInfo()
                    }
                }
            }
        }
    }

    private fun refreshBusInfo() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (currentState.favoriteBusInfo.isEmpty()) {
                setState { copy(isLoading = true) }
            }
            loadFavoriteBusInfo()
            setState { copy(isLoading = false) }
        }
    }

    override fun onIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.NavigateToDetail -> {
                postSideEffect(FavoritesEffect.NavigateToStopDetail(intent.stopId))
            }
            is FavoritesIntent.RequestDeleteFavorite -> {
                setState { copy(popup = Popup.Delete(intent.stop)) }
            }
            is FavoritesIntent.CancelDeleteFavorite -> {
                setState { copy(popup = null) }
            }
            is FavoritesIntent.ConfirmDeleteFavorite -> {
                val popup = currentState.popup
                if (popup is Popup.Delete) {
                    viewModelScope.launch {
                        favoriteRepository.deleteFavorite(popup.stop.stopId)
                        setState { copy(popup = null) }
                        postSideEffect(FavoritesEffect.ShowDeleteSuccess)
                    }
                }
            }
        }
    }

    companion object {
        internal const val REFRESH_INTERVAL_SECONDS = 15
    }
}

sealed class Popup {
    data class Delete(val stop: BusStop) : Popup()
}

data class FavoritesUiState(
    val favoriteStops: List<BusStop> = emptyList(),
    val favoriteBuses: Map<String, List<FavoriteBusItem>> = emptyMap(),
    val favoriteBusInfo: Map<String, BusStopDetail> = emptyMap(),
    val currentTime: Long = 0L,
    val timer: Int = FavoritesViewModel.REFRESH_INTERVAL_SECONDS,
    val isLoading: Boolean = false,
    val popup: Popup? = null
) : UiState

sealed class FavoritesIntent : UiIntent {
    data class NavigateToDetail(val stopId: String) : FavoritesIntent()
    data class RequestDeleteFavorite(val stop: BusStop) : FavoritesIntent()
    data object CancelDeleteFavorite : FavoritesIntent()
    data object ConfirmDeleteFavorite : FavoritesIntent()
}

sealed class FavoritesEffect : SideEffect {
    data class NavigateToStopDetail(val stopId: String) : FavoritesEffect()
    data object ShowDeleteSuccess : FavoritesEffect()
}
