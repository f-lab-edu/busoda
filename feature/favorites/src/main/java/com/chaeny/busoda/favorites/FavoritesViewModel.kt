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

    private val _favoritesStopNavigationEvent = MutableSharedFlow<String>()
    val favoritesStopNavigationEvent: SharedFlow<String> = _favoritesStopNavigationEvent

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState

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
            is FavoritesIntent.ClickStop -> {
                viewModelScope.launch {
                    _favoritesStopNavigationEvent.emit(intent.stopId)
                }
            }
            is FavoritesIntent.DeleteStop -> {
                _uiState.value = _uiState.value.copy(selectedStop = intent.stop)
            }
            is FavoritesIntent.CancelDelete -> {
                _uiState.value = _uiState.value.copy(selectedStop = null)
            }
            is FavoritesIntent.ConfirmDelete -> {
                _uiState.value.selectedStop?.let { stop ->
                    viewModelScope.launch {
                        favoriteRepository.deleteFavorite(stop.stopId)
                        _uiState.value = _uiState.value.copy(selectedStop = null)
                    }
                }
            }
        }
    }
}

data class FavoritesUiState(
    val favorites: List<BusStop> = emptyList(),
    val selectedStop: BusStop? = null
)

sealed class FavoritesIntent {
    data class ClickStop(val stopId: String) : FavoritesIntent()
    data class DeleteStop(val stop: BusStop) : FavoritesIntent()
    data object CancelDelete : FavoritesIntent()
    data object ConfirmDelete : FavoritesIntent()
}
