package com.chaeny.busoda.nearbystops

import com.chaeny.busoda.model.BusStopMarker
import com.chaeny.busoda.mvi.BaseViewModel
import com.chaeny.busoda.mvi.SideEffect
import com.chaeny.busoda.mvi.UiIntent
import com.chaeny.busoda.mvi.UiState
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class NearbystopsViewModel @Inject constructor() :
    BaseViewModel<NearbystopsIntent, NearbystopsUiState, NearbystopsEffect>(
        initialState = NearbystopsUiState()
    ) {

    override fun onIntent(intent: NearbystopsIntent) {
        when (intent) {
            is NearbystopsIntent.UpdatePermission -> {
                setState { copy(hasLocationPermission = intent.granted) }
            }
            is NearbystopsIntent.UpdateLocation -> {
                setState { copy(currentLocation = intent.location) }
            }
        }
    }
}

data class NearbystopsUiState(
    val hasLocationPermission: Boolean = false,
    val currentLocation: LatLng? = null,
    val busStops: List<BusStopMarker> = listOf(
        BusStopMarker("02503", "시청역", 37.566031, 126.97701),
        BusStopMarker("02662", "시청.덕수궁", 37.566254, 126.976921),
        BusStopMarker("02902", "덕수궁", 37.566106, 126.976925),
        BusStopMarker("02286", "시청앞.덕수궁", 37.5662122834, 126.9768355729)
    )
) : UiState

sealed class NearbystopsIntent : UiIntent {
    data class UpdatePermission(val granted: Boolean) : NearbystopsIntent()
    data class UpdateLocation(val location: LatLng) : NearbystopsIntent()
}

sealed class NearbystopsEffect : SideEffect
