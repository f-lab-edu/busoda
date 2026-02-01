package com.chaeny.busoda.nearbystops

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
    val currentLocation: LatLng? = null
) : UiState

sealed class NearbystopsIntent : UiIntent {
    data class UpdatePermission(val granted: Boolean) : NearbystopsIntent()
    data class UpdateLocation(val location: LatLng) : NearbystopsIntent()
}

sealed class NearbystopsEffect : SideEffect
