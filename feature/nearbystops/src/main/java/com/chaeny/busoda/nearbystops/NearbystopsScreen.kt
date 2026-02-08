package com.chaeny.busoda.nearbystops

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.chaeny.busoda.ui.component.MainSearchBar
import com.chaeny.busoda.ui.component.MainTab
import com.chaeny.busoda.ui.component.MainTabRow
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

@Composable
fun NearbystopsScreen(
    navigateToStopList: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToStopDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: NearbystopsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.onIntent(NearbystopsIntent.UpdatePermission(permissions.values.any { it }))
    }

    LaunchedEffect(Unit) {
        val hasPermission = LOCATION_PERMISSIONS.any { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (hasPermission) {
            viewModel.onIntent(NearbystopsIntent.UpdatePermission(true))
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    LaunchedEffect(uiState.hasLocationPermission) {
        if (uiState.hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    viewModel.onIntent(NearbystopsIntent.UpdateLocation(LatLng(it.latitude, it.longitude)))
                }
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(uiState.currentLocation ?: DEFAULT_LOCATION, DEFAULT_ZOOM_LEVEL)
    }

    MoveCameraToCurrentLocation(uiState.currentLocation, cameraPositionState)
    ReloadStopsOnCameraMove(cameraPositionState, viewModel)
    CollectEffects(navigateToStopDetail, viewModel)

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        MainSearchBar(onSearchClick = navigateToStopList)
        MainTabRow(
            selectedTab = MainTab.NEARBY_STOPS,
            onHomeClick = navigateToHome,
            onNearbyStopsClick = { }
        )

        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 5.dp)
                .padding(horizontal = 5.dp)
                .clip(RoundedCornerShape(15.dp)),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = uiState.hasLocationPermission)
        ) {
            Log.d(TAG, "[UI] 마커 렌더링 시작: ${uiState.busStops.size}개")
            uiState.busStops.forEachIndexed { index, stop ->
                val markerState = rememberMarkerState(position = LatLng(stop.latitude, stop.longitude))

                if (index == 0) {
                    Log.d(TAG, "[UI] 첫 번째 마커:")
                    Log.d(TAG, "  State 정류소: ${stop.stopName}")
                    Log.d(TAG, "  State 위치: (${stop.latitude}, ${stop.longitude})")
                    Log.d(TAG, "  MarkerState 실제 위치: ${markerState.position}")
                }

                Marker(
                    state = markerState,
                    title = stop.stopName,
                    snippet = stop.stopId,
                    onClick = {
                        viewModel.onIntent(NearbystopsIntent.ClickBusStop(stop.stopId))
                        true
                    }
                )
            }
        }
    }
}

@Composable
private fun CollectEffects(
    navigateToStopDetail: (String) -> Unit,
    viewModel: NearbystopsViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is NearbystopsEffect.NavigateToStopDetail -> navigateToStopDetail(effect.stopId)
            }
        }
    }
}

@Composable
private fun MoveCameraToCurrentLocation(
    currentLocation: LatLng?,
    cameraPositionState: CameraPositionState
) {
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, DEFAULT_ZOOM_LEVEL)
        }
    }
}

@OptIn(FlowPreview::class)
@Composable
private fun ReloadStopsOnCameraMove(
    cameraPositionState: CameraPositionState,
    viewModel: NearbystopsViewModel
) {
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position.target }
            .debounce(1000)
            .collect { latLng ->
                viewModel.onIntent(NearbystopsIntent.LoadNearbyStops(LatLng(latLng.latitude, latLng.longitude)))
            }
    }
}

private const val TAG = "NearbystopsScreen"
private const val DEFAULT_ZOOM_LEVEL = 15f
private val DEFAULT_LOCATION = LatLng(37.5665, 126.9780)
private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
