package com.chaeny.busoda.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.model.BusStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState

    private val _effect = MutableSharedFlow<FavoritesEffect>()
    val effect: SharedFlow<FavoritesEffect> = _effect

    init {
        collectFavorites()
    }

    private fun collectFavorites() {
        viewModelScope.launch {
            favoriteRepository.getFavorites().collect { favorites ->
                _uiState.value = _uiState.value.copy(favorites = favorites)
            }
        }
    }

    fun handleIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.NavigateToDetail -> {
                viewModelScope.launch {
                    _effect.emit(FavoritesEffect.NavigateToStopDetail(intent.stopId))
                }
            }
            is FavoritesIntent.RequestDeleteFavorite -> {
                _uiState.value = _uiState.value.copy(popup = Popup.Delete(intent.stop))
            }
            is FavoritesIntent.CancelDeleteFavorite -> {
                _uiState.value = _uiState.value.copy(popup = null)
            }
            is FavoritesIntent.ConfirmDeleteFavorite -> {
                val popup = _uiState.value.popup
                if (popup is Popup.Delete) {
                    viewModelScope.launch {
                        favoriteRepository.deleteFavorite(popup.stop.stopId)
                        _uiState.value = _uiState.value.copy(popup = null)
                        _effect.emit(FavoritesEffect.ShowDeleteSuccess)
                    }
                }
            }
        }
    }
}

data class FavoritesUiState(
    val favorites: List<BusStop> = emptyList(),
    val popup: Popup? = null
)

sealed class Popup {
    data class Delete(val stop: BusStop) : Popup()
}

sealed class FavoritesIntent {
    data class NavigateToDetail(val stopId: String) : FavoritesIntent()
    data class RequestDeleteFavorite(val stop: BusStop) : FavoritesIntent()
    data object CancelDeleteFavorite : FavoritesIntent()
    data object ConfirmDeleteFavorite : FavoritesIntent()
}

sealed class FavoritesEffect {
    data class NavigateToStopDetail(val stopId: String) : FavoritesEffect()
    data object ShowDeleteSuccess : FavoritesEffect()
}
