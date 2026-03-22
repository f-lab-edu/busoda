package com.chaeny.busoda.favorites

import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.FavoriteBusRepository
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.domain.usecase.DeleteFavoriteStopUseCase
import com.chaeny.busoda.model.BusInfo
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
    private val busStopDetailRepository: BusStopDetailRepository,
    private val deleteFavoriteStopUseCase: DeleteFavoriteStopUseCase
) : BaseViewModel<FavoritesIntent, FavoritesUiState, FavoritesEffect>(
    initialState = FavoritesUiState()
) {

    private var currentCount = REFRESH_INTERVAL_SECONDS
    private var loadJob: Job? = null
    private var favoriteBuses: Map<String, List<FavoriteBusItem>> = emptyMap()

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
            favoriteBusRepository.getFavoriteBuses().collect { busList ->
                favoriteBuses = busList.groupBy { it.stopId }

                if (busList.isNotEmpty()) {
                    loadBusInfo()
                }
            }
        }
    }

    private suspend fun loadFavoriteBusInfo() {
        val busStopDetailsMap = getBusStopDetailsMap()

        val favoriteBusInfo = favoriteBuses.mapValues { (stopId, favoriteBusList) ->
            favoriteBusList.map { favoriteBus ->
                getFilteredFavoriteBus(busStopDetailsMap[stopId], favoriteBus)
            }
        }

        setState {
            copy(
                favoriteBusInfo = favoriteBusInfo,
                currentTime = System.currentTimeMillis() / 1000
            )
        }
    }

    private fun getFilteredFavoriteBus(stopDetail: BusStopDetail?, favoriteBus: FavoriteBusItem): BusInfo {
        return stopDetail?.busInfos?.find { it.busNumber == favoriteBus.busNumber }
            ?: BusInfo(
                busNumber = favoriteBus.busNumber,
                nextStopName = favoriteBus.nextStopName,
                arrivalInfos = emptyList()
            )
    }

    private suspend fun getBusStopDetailsMap(): Map<String, BusStopDetail> {
        val stopIds = favoriteBuses.keys

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
                if (favoriteBuses.isNotEmpty()) {
                    setState { copy(currentTime = System.currentTimeMillis() / 1000) }
                }
                delay(1000)
                currentCount--

                if (currentCount == 0) {
                    currentCount = REFRESH_INTERVAL_SECONDS
                    if (favoriteBuses.isNotEmpty()) {
                        refreshBusInfo()
                    }
                }
            }
        }
    }

    private fun refreshBusInfo() {
        postSideEffect(FavoritesEffect.RotateRefreshBtn)
        loadBusInfo()
    }

    private fun loadBusInfo() {
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
                setState {
                    copy(
                        popup = Popup.Delete(intent.stop),
                        hasFavoriteBuses = intent.stop.stopId in favoriteBuses
                    )
                }
            }
            is FavoritesIntent.CancelDeleteFavorite -> {
                setState { copy(popup = null) }
            }
            is FavoritesIntent.ConfirmDeleteFavorite -> {
                val popup = currentState.popup
                if (popup is Popup.Delete) {
                    viewModelScope.launch {
                        val stopId = popup.stop.stopId
                        if (favoriteBuses[stopId].isNullOrEmpty()) {
                            favoriteRepository.deleteFavorite(stopId)
                        } else {
                            deleteFavoriteStopUseCase(stopId)
                        }
                        setState { copy(popup = null) }
                        postSideEffect(FavoritesEffect.ShowDeleteSuccess)
                    }
                }
            }
            is FavoritesIntent.RefreshData -> {
                currentCount = REFRESH_INTERVAL_SECONDS
                if (favoriteBuses.isNotEmpty()) {
                    refreshBusInfo()
                }
            }
        }
    }

    companion object {
        private const val REFRESH_INTERVAL_SECONDS = 15
    }
}

sealed class Popup {
    data class Delete(val stop: BusStop) : Popup()
}

data class FavoritesUiState(
    val favoriteStops: List<BusStop> = emptyList(),
    val favoriteBusInfo: Map<String, List<BusInfo>> = emptyMap(),
    val currentTime: Long = 0L,
    val isLoading: Boolean = false,
    val hasFavoriteBuses: Boolean = false,
    val popup: Popup? = null
) : UiState

sealed class FavoritesIntent : UiIntent {
    data class NavigateToDetail(val stopId: String) : FavoritesIntent()
    data class RequestDeleteFavorite(val stop: BusStop) : FavoritesIntent()
    data object CancelDeleteFavorite : FavoritesIntent()
    data object ConfirmDeleteFavorite : FavoritesIntent()
    data object RefreshData : FavoritesIntent()
}

sealed class FavoritesEffect : SideEffect {
    data class NavigateToStopDetail(val stopId: String) : FavoritesEffect()
    data object ShowDeleteSuccess : FavoritesEffect()
    data object RotateRefreshBtn : FavoritesEffect()
}
