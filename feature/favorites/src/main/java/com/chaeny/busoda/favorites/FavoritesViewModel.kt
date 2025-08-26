package com.chaeny.busoda.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.FavoriteRepository
import com.chaeny.busoda.model.BusStop
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class FavoritesViewModel @Inject constructor(
    favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _favoritesStopNavigationEvent = MutableSharedFlow<String>()
    val favoritesStopNavigationEvent: SharedFlow<String> = _favoritesStopNavigationEvent
    val favorites: StateFlow<List<BusStop>> = favoriteRepository.getFavorites().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun handleFavoriteStopClick(stopId: String) {
        viewModelScope.launch {
            _favoritesStopNavigationEvent.emit(stopId)
        }
    }
}
