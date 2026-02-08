package com.chaeny.busoda.nearbystops

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.chaeny.busoda.data.repository.NearbyBusStopsRepository
import com.chaeny.busoda.model.BusStopPosition
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
                loadNearbyBusStops(intent.location.latitude, intent.location.longitude, source = "GPS")
            }
            is NearbystopsIntent.LoadNearbyStops -> {
                loadNearbyBusStops(intent.location.latitude, intent.location.longitude, source = "Camera")
            }
            is NearbystopsIntent.ClickBusStop -> {
                postSideEffect(NearbystopsEffect.NavigateToStopDetail(intent.stopId))
            }
        }
    }

    private fun loadNearbyBusStops(latitude: Double, longitude: Double, source: String) {
        viewModelScope.launch {
            val isSeoulCityHall = latitude in 37.5665..37.5666 && longitude in 126.9779..126.9781
            val location = if (isSeoulCityHall) "서울시청" else "현재위치"

            Log.d(TAG, "[$source] API 호출: $location")

            val busStops = nearbyBusStopsRepository.getNearbyBusStops(latitude, longitude, DEFAULT_RADIUS)
            setState { copy(busStops = busStops) }

            Log.d(TAG, "[$source] API 완료: 정류소 ${busStops.size}개")
        }
    }

    companion object {
        private const val TAG = "NearbystopsViewModel"
        private const val DEFAULT_RADIUS = 500
    }
}

data class NearbystopsUiState(
    val hasLocationPermission: Boolean = false,
    val currentLocation: LatLng? = null,
    val busStops: List<BusStopPosition> = emptyList()
) : UiState

sealed class NearbystopsIntent : UiIntent {
    data class UpdatePermission(val granted: Boolean) : NearbystopsIntent()
    data class UpdateLocation(val location: LatLng) : NearbystopsIntent()
    data class LoadNearbyStops(val location: LatLng) : NearbystopsIntent()
    data class ClickBusStop(val stopId: String) : NearbystopsIntent()
}

sealed class NearbystopsEffect : SideEffect {
    data class NavigateToStopDetail(val stopId: String) : NearbystopsEffect()
}
