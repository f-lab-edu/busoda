package com.chaeny.busoda.favorites

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaeny.busoda.model.BusStop
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
        if (uiState.favorites.isEmpty()) {
            FavoritesGuide()
        } else {
            FavoritesStopList(
                stops = uiState.favorites,
                onClickItem = { stopId ->
                    viewModel.onIntent(FavoritesIntent.NavigateToDetail(stopId))
                },
                onLongClickItem = { stop ->
                    viewModel.onIntent(FavoritesIntent.RequestDeleteFavorite(stop))
                }
            )
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
private fun FavoritesStopList(
    stops: List<BusStop>,
    onClickItem: (String) -> Unit,
    onLongClickItem: (BusStop) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier
        .fillMaxSize()
        .padding(top = 20.dp)
    ) {
        LazyColumn {
            items(
                items = stops,
                key = { stop -> stop.stopId }
            ) { stop ->
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
        Text(
            text = stop.stopName,
            color = Color.Black,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 15.dp)
                .padding(top = 15.dp, bottom = 5.dp)
        )
        Row(
            modifier = Modifier
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

@Preview(showBackground = true)
@Composable
private fun FavoritesStopListPreview() {
    FavoritesStopList(
        stops = dummyData, onClickItem = {}, onLongClickItem = {}
    )
}

private val dummyData = listOf(
    BusStop("02218", "남대문경찰서.서울역10번출구", "숭례문"),
    BusStop("03119", "신용산역3번출구", "신용산지하차도"),
    BusStop("19114", "영등포역", "신길역5호선"),
    BusStop("19113", "영등포역.패어필드호텔", "경방타임스퀘어.신세계백화점")
)
