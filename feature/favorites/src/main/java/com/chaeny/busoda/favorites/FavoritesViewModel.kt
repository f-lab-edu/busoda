package com.chaeny.busoda.favorites

import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.GetBusStopDetailResult
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
    private var timerJob: Job? = null
    private var favoriteBuses: Map<String, List<FavoriteBusItem>> = emptyMap()

    init {
        collectFavoriteStops()
        collectFavoriteBuses()
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
                } else if (currentState.favoriteBusInfo.isNotEmpty()) {
                    stopTimer()
                    setState { copy(favoriteBusInfo = emptyMap()) }
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

        val results = coroutineScope {
            stopIds.map { stopId ->
                async { stopId to busStopDetailRepository.getBusStopDetail(stopId) }
            }.awaitAll()
        }
        handleError(results)

        return results.associate { (stopId, result) ->
            val busStopDetail = if (result is GetBusStopDetailResult.Success) {
                result.data
            } else {
                BusStopDetail("", emptyList())
            }
            stopId to busStopDetail
        }
    }

    private fun handleError(results: List<Pair<String, GetBusStopDetailResult>>) {
        val error = results.map { it.second }.firstOrNull { it !is GetBusStopDetailResult.Success }
        if (error == null) {
            startTimer()
            return
        }
        stopTimer()
        when (error) {
            is GetBusStopDetailResult.NoInternet -> postSideEffect(FavoritesEffect.ShowNoInternet)
            is GetBusStopDetailResult.NetworkError -> postSideEffect(FavoritesEffect.ShowNetworkError)
            else -> {}
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true || currentState.isEditMode) return
        timerJob = viewModelScope.launch {
            while (true) {
                setState { copy(currentTime = System.currentTimeMillis() / 1000) }
                delay(1000)
                currentCount--

                if (currentCount == 0) {
                    currentCount = REFRESH_INTERVAL_SECONDS
                    refreshBusInfo()
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        currentCount = REFRESH_INTERVAL_SECONDS
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
                    copy(popup = Popup.Delete(intent.stop, intent.stop.stopId in favoriteBuses))
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
            is FavoritesIntent.ReorderFavorites -> {
                viewModelScope.launch {
                    favoriteRepository.updateFavoriteOrders(intent.stops)
                }
            }
            is FavoritesIntent.DeleteFavoriteBus -> {
                viewModelScope.launch {
                    favoriteBusRepository.deleteFavoriteBus(intent.stopId, intent.busNumber)
                }
            }
            is FavoritesIntent.ToggleEditMode -> {
                setState { copy(isEditMode = !isEditMode) }
                if (currentState.isEditMode) stopTimer() else startTimer()
            }
        }
    }

    companion object {
        private const val REFRESH_INTERVAL_SECONDS = 15
    }
}

sealed class Popup {
    data class Delete(val stop: BusStop, val hasFavoriteBuses: Boolean) : Popup()
}

data class FavoritesUiState(
    val favoriteStops: List<BusStop> = emptyList(),
    val favoriteBusInfo: Map<String, List<BusInfo>> = emptyMap(),
    val currentTime: Long = 0L,
    val isLoading: Boolean = false,
    val popup: Popup? = null,
    val isEditMode: Boolean = false
) : UiState

sealed class FavoritesIntent : UiIntent {
    data class NavigateToDetail(val stopId: String) : FavoritesIntent()
    data class RequestDeleteFavorite(val stop: BusStop) : FavoritesIntent()
    data object CancelDeleteFavorite : FavoritesIntent()
    data object ConfirmDeleteFavorite : FavoritesIntent()
    data object RefreshData : FavoritesIntent()
    data class ReorderFavorites(val stops: List<BusStop>) : FavoritesIntent()
    data class DeleteFavoriteBus(val stopId: String, val busNumber: String) : FavoritesIntent()
    data object ToggleEditMode : FavoritesIntent()
}

sealed class FavoritesEffect : SideEffect {
    data class NavigateToStopDetail(val stopId: String) : FavoritesEffect()
    data object ShowDeleteSuccess : FavoritesEffect()
    data object RotateRefreshBtn : FavoritesEffect()
    data object ShowNoInternet : FavoritesEffect()
    data object ShowNetworkError : FavoritesEffect()
}
