package com.chaeny.busoda.nearbystops

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.chaeny.busoda.model.BusStopPosition
import com.chaeny.busoda.ui.component.MainSearchBar
import com.chaeny.busoda.ui.component.MainTab
import com.chaeny.busoda.ui.component.MainTabRow
import com.chaeny.busoda.ui.theme.MainGreen
import com.chaeny.busoda.ui.theme.SkyBlue
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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
    ShowMarkerInfoSheet(uiState, viewModel)

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
            val busIcon = context.createCustomMarkerIcon(R.drawable.ic_bus_marker)

            uiState.busStops.forEach { stop ->
                Marker(
                    state = MarkerState(position = LatLng(stop.latitude, stop.longitude)),
                    title = stop.stopName,
                    snippet = stop.stopId,
                    icon = busIcon,
                    onClick = {
                        viewModel.onIntent(NearbystopsIntent.ShowMarkerInfo(stop))
                        true
                    }
                )
            }
        }
    }
}

@Composable
private fun ShowMarkerInfoSheet(
    uiState: NearbystopsUiState,
    viewModel: NearbystopsViewModel
) {
    uiState.selectedMarkerInfo?.let { stop ->
        MarkerInfoBottomSheet(
            stop = stop,
            onDismiss = {
                viewModel.onIntent(NearbystopsIntent.HideMarkerInfo)
            },
            onNavigateToDetail = {
                viewModel.onIntent(NearbystopsIntent.ClickBusStop(stop.stopId))
            }
        )
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
        snapshotFlow { cameraPositionState.position }
            .debounce(1000)
            .collect { position ->
                if (position.zoom >= MIN_ZOOM_LEVEL) {
                    viewModel.onIntent(
                        NearbystopsIntent.LoadNearbyStops(
                            LatLng(position.target.latitude, position.target.longitude)
                        )
                    )
                } else {
                    viewModel.onIntent(NearbystopsIntent.ClearBusStops)
                }
            }
    }
}

fun Context.createCustomMarkerIcon(@DrawableRes id: Int): BitmapDescriptor {
    val size = 100
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        color = SkyBlue.toArgb()
        isAntiAlias = true
    }
    val rect = RectF(10f, 10f, (size - 10).toFloat(), (size - 10).toFloat())
    canvas.drawRoundRect(rect, 15f, 15f, paint)

    val iconSize = (size * 0.6f).toInt()
    val iconDrawable = ContextCompat.getDrawable(this, id)
        ?: return BitmapDescriptorFactory.defaultMarker()

    val left = (size - iconSize) / 2
    val top = (size - iconSize) / 2
    iconDrawable.setBounds(left, top, left + iconSize, top + iconSize)
    iconDrawable.setTint(Color.WHITE)
    iconDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarkerInfoBottomSheet(
    stop: BusStopPosition,
    onDismiss: () -> Unit,
    onNavigateToDetail: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MainGreen
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToDetail() }
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.stop_name_label, stop.stopName),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 25.dp, vertical = 5.dp)
            )
            Text(
                text = stringResource(R.string.stop_id_label, stop.stopId),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 25.dp, vertical = 5.dp)
            )
        }
    }
}

private const val DEFAULT_ZOOM_LEVEL = 15f
private const val MIN_ZOOM_LEVEL = 13f
private val DEFAULT_LOCATION = LatLng(37.5665, 126.9780)
private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)
