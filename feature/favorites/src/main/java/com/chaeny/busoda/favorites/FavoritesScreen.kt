package com.chaeny.busoda.favorites

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.model.FavoriteBusItem
import com.chaeny.busoda.ui.component.ArrivalInfo
import com.chaeny.busoda.ui.component.LocalCurrentTime
import com.chaeny.busoda.ui.component.MainSearchBar
import com.chaeny.busoda.ui.component.MainTab
import com.chaeny.busoda.ui.component.MainTabRow
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

    CollectEffect(viewModel, navigateToStopDetail)

    CompositionLocalProvider(LocalCurrentTime provides uiState.currentTime) {
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
                    favoriteBuses = uiState.favoriteBuses,
                    favoriteBusInfo = uiState.favoriteBusInfo,
                    onClickItem = { viewModel.onIntent(FavoritesIntent.NavigateToDetail(it)) },
                    onLongClickItem = { viewModel.onIntent(FavoritesIntent.RequestDeleteFavorite(it)) }
                )
            }
        }
    }

    val popup = uiState.popup
    if (popup is Popup.Delete) {
        DeletePopup(
            stopName = popup.stop.stopName,
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
    navigateToStopDetail: (String) -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is FavoritesEffect.NavigateToStopDetail -> {
                    navigateToStopDetail(effect.stopId)
                }
                is FavoritesEffect.ShowDeleteSuccess -> {
                    Toast.makeText(
                        context, context.getString(R.string.delete_success), Toast.LENGTH_SHORT
                    ).show()
                }
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
    favoriteBuses: Map<String, List<FavoriteBusItem>>,
    favoriteBusInfo: Map<String, BusStopDetail>,
    onClickItem: (String) -> Unit,
    onLongClickItem: (BusStop) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        items(
            items = favoriteStops,
            key = { stop -> stop.stopId }
        ) { stop ->
            val buses = favoriteBuses[stop.stopId]

            if (buses != null) {
                StopWithBusesCard(
                    stop = stop,
                    buses = buses,
                    busStopDetail = favoriteBusInfo[stop.stopId],
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
}

@Composable
private fun StopWithBusesCard(
    stop: BusStop,
    buses: List<FavoriteBusItem>,
    busStopDetail: BusStopDetail?,
    onClick: (String) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 30.dp)
            .padding(bottom = 15.dp)
            .clip(RoundedCornerShape(15.dp))
            .combinedClickable(
                onClick = { onClick(stop.stopId) },
                onLongClick = { onLongClick() }
            ),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        StopHeader(stop)

        buses.forEach { bus ->
            FavoriteBusContent(
                bus = bus,
                busStopDetail = busStopDetail
            )
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
    Card(
        modifier = modifier
            .padding(horizontal = 30.dp)
            .padding(bottom = 15.dp)
            .clip(RoundedCornerShape(15.dp))
            .combinedClickable(
                onClick = { onClick(stop.stopId) },
                onLongClick = { onLongClick() }
            ),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        StopHeader(stop)
    }
}

@Composable
private fun StopHeader(
    stop: BusStop,
    modifier: Modifier = Modifier
) {
    Text(
        text = stop.stopName,
        color = Color.Black,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .padding(horizontal = 15.dp)
            .padding(top = 15.dp, bottom = 5.dp)
    )
    Row(
        modifier = modifier
            .padding(horizontal = 15.dp)
            .padding(bottom = 15.dp)
    ) {
        Text(
            text = stop.stopId,
            color = Gray60,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = stop.nextStopName,
            color = Gray60,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Right,
            modifier = Modifier.weight(0.7f)
        )
    }
}

@Composable
private fun FavoriteBusContent(
    bus: FavoriteBusItem,
    busStopDetail: BusStopDetail?,
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        color = Gray60.copy(alpha = 0.3f)
    )

    BusInfoHeader(
        busNumber = bus.busNumber,
        nextStopName = bus.nextStopName
    )

    if (busStopDetail != null) {
        val busInfo = busStopDetail.busInfos.find { it.busNumber == bus.busNumber }
        if (busInfo != null) {
            BusArrivalInfoList(busInfo = busInfo)
        }
    }
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
            .padding(vertical = 15.dp),
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

@Composable
private fun BusArrivalInfoList(
    busInfo: BusInfo,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(8.dp))

    ArrivalInfo(
        arrivalInfo = busInfo.arrivalInfos.getOrNull(0),
        position = 0,
        modifier = Modifier.padding(horizontal = 20.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    ArrivalInfo(
        arrivalInfo = busInfo.arrivalInfos.getOrNull(1),
        position = 1,
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 15.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeletePopup(
    stopName: String,
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
                    .padding(horizontal = 25.dp, vertical = 15.dp)
            )
            Row {
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
