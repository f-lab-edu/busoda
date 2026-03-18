package com.chaeny.busoda.stopdetail

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.ui.component.BusArrivalInfoList
import com.chaeny.busoda.ui.component.LocalCurrentTime
import com.chaeny.busoda.ui.component.RefreshButton
import com.chaeny.busoda.ui.theme.DarkGreen
import com.chaeny.busoda.ui.theme.White

@Composable
fun StopDetailScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: StopDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var rotation by remember { mutableFloatStateOf(0f) }

    CollectEffects(
        viewModel = viewModel,
        onRotate = { rotation += 180f }
    )

    CompositionLocalProvider(LocalCurrentTime provides uiState.currentTime) {
        StopDetailContent(
            stopId = uiState.stopId,
            stopDetail = uiState.stopDetail,
            isLoading = uiState.isLoading,
            timer = uiState.timer,
            rotation = rotation,
            isFavorite = uiState.isFavorite,
            favoriteBusNumbers = uiState.favoriteBusNumbers,
            onRefresh = { viewModel.onIntent(StopDetailIntent.RefreshData) },
            onToggleFavorite = { viewModel.onIntent(StopDetailIntent.ToggleFavorite) },
            onToggleFavoriteBus = { busNumber ->
                viewModel.onIntent(StopDetailIntent.ToggleFavoriteBus(busNumber))
            },
            modifier = modifier
        )
    }

    if (uiState.popup is Popup.DeleteStop) {
        DeletePopup(
            onDismiss = { viewModel.onIntent(StopDetailIntent.CancelDeleteFavorite) },
            onConfirm = { viewModel.onIntent(StopDetailIntent.ConfirmDeleteFavorite) }
        )
    }
}

@Composable
private fun CollectEffects(
    viewModel: StopDetailViewModel,
    onRotate: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is StopDetailEffect.RotateRefreshBtn -> onRotate()
                is StopDetailEffect.ShowFavoriteAdded -> {
                    Toast.makeText(
                        context, context.getString(R.string.favorite_added), Toast.LENGTH_SHORT
                    ).show()
                }
                is StopDetailEffect.ShowFavoriteRemoved -> {
                    Toast.makeText(
                        context, context.getString(R.string.favorite_removed), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

@Composable
private fun StopDetailContent(
    stopId: String,
    stopDetail: BusStopDetail,
    isLoading: Boolean,
    timer: Int,
    rotation: Float,
    isFavorite: Boolean,
    favoriteBusNumbers: Set<String>,
    onRefresh: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleFavoriteBus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(top = 20.dp)
    ) {
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 30.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkAdd,
                contentDescription = stringResource(R.string.bookmark),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            StopId(stopId)
            StopName(stopDetail.stopName)
            Row {
                BusEmoji(timer = timer)
                StopEmoji()
            }
            BusList(
                busInfos = stopDetail.busInfos,
                isLoading = isLoading,
                favoriteBusNumbers = favoriteBusNumbers,
                onToggleFavoriteBus = onToggleFavoriteBus
            )
        }
        RefreshButton(
            rotation = rotation,
            onClick = onRefresh,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun StopId(
    stopId: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = stopId,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun StopName(
    stopName: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = stopName.ifEmpty { stringResource(R.string.no_info) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun BusEmoji(
    timer: Int,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints {
        val startPadding = 35.dp
        val endPadding = 30.dp
        val totalDistance = maxWidth - startPadding - endPadding * 2
        val progress = (15 - timer) / 15f
        val moveAnimation by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        )
        val translationValue = totalDistance * moveAnimation

        Text(
            text = stringResource(R.string.bus_emoji),
            modifier = modifier
                .padding(start = startPadding)
                .graphicsLayer {
                    rotationY = 180f
                    this.translationX = translationValue.toPx()
                },
            fontSize = 30.dp.toSp()
        )
    }
}

@Composable
private fun StopEmoji(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.stop_emoji),
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 30.dp),
        fontSize = 30.dp.toSp(),
        textAlign = TextAlign.End
    )
}

@Composable
private fun BusInfoHeader(
    busNumber: String,
    nextStopName: String,
    isFavoriteBus: Boolean,
    onToggleFavoriteBus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        IconButton(
            onClick = onToggleFavoriteBus,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isFavoriteBus) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = stringResource(R.string.bus_favorite),
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
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
private fun BusItem(
    busInfo: BusInfo,
    isFavoriteBus: Boolean,
    onToggleFavoriteBus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 30.dp)
            .padding(bottom = 15.dp),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        BusInfoHeader(
            busNumber = busInfo.busNumber,
            nextStopName = busInfo.nextStopName,
            isFavoriteBus = isFavoriteBus,
            onToggleFavoriteBus = { onToggleFavoriteBus(busInfo.busNumber) },
            modifier = Modifier
                .padding(start = 10.dp, end = 20.dp)
                .padding(top = 15.dp)
        )
        BusArrivalInfoList(busInfo = busInfo)
    }
}

@Composable
private fun BusList(
    busInfos: List<BusInfo>,
    isLoading: Boolean,
    favoriteBusNumbers: Set<String>,
    onToggleFavoriteBus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn {
            itemsIndexed(
                items = busInfos,
                key = { index, busInfo -> "$index-${busInfo.busNumber}" }
            ) { index, busInfo ->
                BusItem(
                    busInfo = busInfo,
                    isFavoriteBus = favoriteBusNumbers.contains(busInfo.busNumber),
                    onToggleFavoriteBus = onToggleFavoriteBus
                )
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = DarkGreen
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeletePopup(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = White
    ) {
        Column(modifier = Modifier.padding(bottom = 10.dp)) {
            Text(
                text = stringResource(R.string.delete_favorite_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .padding(top = 15.dp, bottom = 5.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.delete_favorite_bus_message),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .padding(top = 5.dp, bottom = 20.dp),
                textAlign = TextAlign.Center
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
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StopIdPreview() {
    MaterialTheme { StopId("16206") }
}

@Preview(showBackground = true)
@Composable
private fun StopNamePreview() {
    MaterialTheme { StopName("화곡역4번출구") }
}
