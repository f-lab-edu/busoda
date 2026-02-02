package com.chaeny.busoda.nearbystops

import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.NearbyBusStopsRepository
import com.chaeny.busoda.model.BusStopMarker
import com.chaeny.busoda.mvi.BaseViewModel
import com.chaeny.busoda.mvi.SideEffect
import com.chaeny.busoda.mvi.UiIntent
import com.chaeny.busoda.mvi.UiState
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NearbystopsViewModel @Inject constructor(
    private val nearbyBusStopsRepository: NearbyBusStopsRepository
) : BaseViewModel<NearbystopsIntent, NearbystopsUiState, NearbystopsEffect>(
        initialState = NearbystopsUiState()
    ) {

    override fun onIntent(intent: NearbystopsIntent) {
        when (intent) {
            is NearbystopsIntent.UpdatePermission -> {
                setState { copy(hasLocationPermission = intent.granted) }
            }
            is NearbystopsIntent.UpdateLocation -> {
                setState { copy(currentLocation = intent.location) }
                loadNearbyBusStops(intent.location.latitude, intent.location.longitude)
            }
        }
    }

    private fun loadNearbyBusStops(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val busStops = nearbyBusStopsRepository.getNearbyBusStops(latitude, longitude, DEFAULT_RADIUS)
            setState { copy(busStops = busStops) }
        }
    }

    companion object {
        private const val DEFAULT_RADIUS = 500
    }
}

data class NearbystopsUiState(
    val hasLocationPermission: Boolean = false,
    val currentLocation: LatLng? = null,
    val busStops: List<BusStopMarker> = emptyList()
) : UiState

sealed class NearbystopsIntent : UiIntent {
    data class UpdatePermission(val granted: Boolean) : NearbystopsIntent()
    data class UpdateLocation(val location: LatLng) : NearbystopsIntent()
}

sealed class NearbystopsEffect : SideEffect
