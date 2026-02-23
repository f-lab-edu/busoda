package com.chaeny.busoda.favorites

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.BusStopDetailRepository
import com.chaeny.busoda.data.repository.FavoriteBusRepository
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.model.BusStop
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
        val results = coroutineScope {
            currentState.busFavorites.map { bus ->
                async {
                    val busStopDetail = busStopDetailRepository.getBusStopDetail(bus.stopId)
                    Log.d("FavoritesViewModel", "정류소 ${bus.stopId} 데이터: ${busStopDetail.busInfos.size}개 버스")
                }
            }.awaitAll()
        }
        Log.d("FavoritesViewModel", "총 ${results.size}개 정류소 데이터 받음")
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
