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

    fun handleIntent(intent: ClickFavoriteStopIntent) {
        viewModelScope.launch {
            _favoritesStopNavigationEvent.emit(intent.stopId)
        }
    }
}

data class FavoritesUiState(
    val favorites: List<BusStop> = emptyList()
)

data class ClickFavoriteStopIntent(
    val stopId: String
)
