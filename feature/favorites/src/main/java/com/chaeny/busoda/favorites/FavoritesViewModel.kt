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

    private val _favorites = MutableStateFlow<List<BusStop>>(emptyList())
    private val _favoritesStopNavigationEvent = MutableSharedFlow<String>()
    val favorites: StateFlow<List<BusStop>> = _favorites
    val favoritesStopNavigationEvent: SharedFlow<String> = _favoritesStopNavigationEvent

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _favorites.value = favoriteRepository.getFavorites()
        }
    }

    fun handleFavoriteStopClick(stopId: String) {
        viewModelScope.launch {
            _favoritesStopNavigationEvent.emit(stopId)
        }
    }
}
