package com.chaeny.busoda.favorites

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.ui.component.BusArrivalInfoList
import com.chaeny.busoda.ui.component.LocalCurrentTime
import com.chaeny.busoda.ui.component.MainSearchBar
import com.chaeny.busoda.ui.component.MainTab
import com.chaeny.busoda.ui.component.MainTabRow
import com.chaeny.busoda.ui.component.RefreshButton
import com.chaeny.busoda.ui.component.StopInfo
import com.chaeny.busoda.ui.theme.DarkGreen
import com.chaeny.busoda.ui.theme.Gray60
import com.chaeny.busoda.ui.theme.White

@Composable
fun FavoritesScreen(
    navigateToStopList: () -> Unit,
    navigateToStopDetail: (String) -> Unit,
    navigateToNearbyStops: () -> Unit
) {
    val viewModel: FavoritesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var rotation by remember { mutableFloatStateOf(0f) }

    CollectEffect(
        viewModel = viewModel,
        navigateToStopDetail = navigateToStopDetail,
        onRotate = { rotation += 180f }
    )

    CompositionLocalProvider(LocalCurrentTime provides uiState.currentTime) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                MainSearchBar(onSearchClick = navigateToStopList)
                MainTabRow(
                    selectedTab = MainTab.HOME,
                    onHomeClick = { },
                    onNearbyStopsClick = navigateToNearbyStops
                )
                if (uiState.favoriteStops.isEmpty()) {
                    FavoritesGuide()
                } else {
                    FavoritesList(
                        favoriteStops = uiState.favoriteStops,
                        favoriteBusInfo = uiState.favoriteBusInfo,
                        isLoading = uiState.isLoading,
                        onClickItem = { viewModel.onIntent(FavoritesIntent.NavigateToDetail(it)) },
                        onLongClickItem = { viewModel.onIntent(FavoritesIntent.RequestDeleteFavorite(it)) }
                    )
                }
            }

            if (uiState.favoriteBusInfo.isNotEmpty()) {
                RefreshButton(
                    rotation = rotation,
                    onClick = { viewModel.onIntent(FavoritesIntent.RefreshData) },
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }

    val popup = uiState.popup
    if (popup is Popup.Delete) {
        DeletePopup(
            stopName = popup.stop.stopName,
            hasFavoriteBuses = uiState.hasFavoriteBuses,
            onDismiss = {
                viewModel.onIntent(FavoritesIntent.CancelDeleteFavorite)
            },
            onConfirm = {
                viewModel.onIntent(FavoritesIntent.ConfirmDeleteFavorite)
            }
        )
    }
}

@Composable
private fun CollectEffect(
    viewModel: FavoritesViewModel,
    navigateToStopDetail: (String) -> Unit,
    onRotate: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is FavoritesEffect.NavigateToStopDetail -> {
                    navigateToStopDetail(effect.stopId)
                }
                is FavoritesEffect.ShowDeleteSuccess -> showToast(context, R.string.delete_success)
                is FavoritesEffect.RotateRefreshBtn -> onRotate()
                is FavoritesEffect.ShowNoInternet -> showToast(context, R.string.no_internet)
                is FavoritesEffect.ShowNetworkError -> showToast(context, R.string.network_error)
            }
        }
    }
}

@Composable
private fun FavoritesGuide(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.guide),
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun FavoritesList(
    favoriteStops: List<BusStop>,
    favoriteBusInfo: Map<String, List<BusInfo>>,
    isLoading: Boolean,
    onClickItem: (String) -> Unit,
    onLongClickItem: (BusStop) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        if (!isLoading) {
            LazyColumn(contentPadding = PaddingValues(bottom = 90.dp)) {
                items(
                    items = favoriteStops,
                    key = { stop -> stop.stopId }
                ) { stop ->
                    val busInfos = favoriteBusInfo[stop.stopId]

                    if (busInfos != null) {
                        StopWithBusesCard(
                            stop = stop,
                            busInfos = busInfos,
                            onClick = onClickItem,
                            onLongClick = { onLongClickItem(stop) }
                        )
                    } else {
                        StopItem(
                            stop = stop,
                            onClick = onClickItem,
                            onLongClick = { onLongClickItem(stop) }
                        )
                    }
                }
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = DarkGreen
            )
        }
    }
}

@Composable
private fun StopWithBusesCard(
    stop: BusStop,
    busInfos: List<BusInfo>,
    onClick: (String) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavoriteCard(
        onClick = { onClick(stop.stopId) },
        onLongClick = onLongClick,
        modifier = modifier
    ) {
        StopInfo(stop)

        busInfos.forEach { busInfo ->
            FavoriteBusContent(busInfo = busInfo)
        }
    }
}

@Composable
private fun StopItem(
    stop: BusStop,
    onClick: (String) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavoriteCard(
        onClick = { onClick(stop.stopId) },
        onLongClick = onLongClick,
        modifier = modifier
    ) {
        StopInfo(stop)
    }
}

@Composable
private fun FavoriteCard(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 30.dp)
            .padding(bottom = 15.dp)
            .clip(RoundedCornerShape(15.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = content
    )
}

@Composable
private fun FavoriteBusContent(
    busInfo: BusInfo
) {
    HorizontalDivider(
        color = Gray60.copy(alpha = 0.3f)
    )

    BusInfoHeader(
        busNumber = busInfo.busNumber,
        nextStopName = busInfo.nextStopName
    )

    BusArrivalInfoList(busInfo = busInfo)
}

@Composable
private fun BusInfoHeader(
    busNumber: String,
    nextStopName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 15.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = busNumber,
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = nextStopName,
            modifier = Modifier
                .weight(0.45f)
                .padding(end = 5.dp),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(R.string.way),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeletePopup(
    stopName: String,
    hasFavoriteBuses: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(modifier = Modifier.padding(bottom = 10.dp)) {
            Text(
                text = stringResource(R.string.delete_confirmation, stopName),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .padding(top = 15.dp, bottom = 5.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (hasFavoriteBuses) {
                Text(
                    text = stringResource(R.string.delete_favorite_bus_message),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp)
                        .padding(top = 5.dp),
                    textAlign = TextAlign.Center
                )
            }
            Row(modifier = Modifier.padding(top = 20.dp)) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}

private fun showToast(context: Context, messageResId: Int) {
    Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
}

@Preview(showBackground = true)
@Composable
private fun FavoritesGuidePreview() {
    FavoritesGuide()
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    FavoritesScreen(
        navigateToStopList = {},
        navigateToStopDetail = {},
        navigateToNearbyStops = {}
    )
}
