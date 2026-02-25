package com.chaeny.busoda.favorites

import android.util.Log
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    init {
        collectFavorites()
        collectBusFavorites()
    }

    private fun collectFavorites() {
        viewModelScope.launch {
            favoriteRepository.getFavorites().collect { favorites ->
                setState { copy(favorites = favorites) }
            }
        }
    }

    private fun collectBusFavorites() {
        viewModelScope.launch {
            favoriteBusRepository.getFavoriteBuses().collect { busFavorites ->
                setState { copy(busFavorites = busFavorites) }
                if (busFavorites.isNotEmpty()) {
                    loadFavoriteBusInfo()
                }
            }
        }
    }

    private suspend fun loadFavoriteBusInfo() {
        val currentTime = System.currentTimeMillis() / 1000

        val busInfoMap = coroutineScope {
            currentState.busFavorites.map { favoriteBus ->
                async {
                    val busStopDetail = busStopDetailRepository.getBusStopDetail(favoriteBus.stopId)
                    val matchingBus = busStopDetail.busInfos.find { it.busNumber == favoriteBus.busNumber }

                    if (matchingBus != null && matchingBus.arrivalInfos.isNotEmpty()) {
                        val firstBus = matchingBus.arrivalInfos[0]
                        val remainingSeconds = firstBus.arrivalTime - currentTime
                        val minutes = (remainingSeconds / 60).toInt()
                        val seconds = (remainingSeconds % 60).toInt()

                        Log.d("FavoritesViewModel", "[${matchingBus.busNumber}] ${busStopDetail.stopName} → ${minutes}분 ${seconds}초 | ${firstBus.position}")
                    }
                    favoriteBus.stopId to busStopDetail
                }
            }.awaitAll().toMap()
        }
        setState { copy(favoriteBusInfo = busInfoMap) }
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
}

sealed class Popup {
    data class Delete(val stop: BusStop) : Popup()
}

data class FavoritesUiState(
    val favorites: List<BusStop> = emptyList(),
    val busFavorites: List<FavoriteBusItem> = emptyList(),
    val favoriteBusInfo: Map<String, BusStopDetail> = emptyMap(),
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
