package com.chaeny.busoda.stopdetail

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaeny.busoda.model.BusArrivalInfo
import com.chaeny.busoda.model.BusInfo
import com.chaeny.busoda.model.BusStopDetail
import com.chaeny.busoda.ui.theme.DarkGreen
import kotlinx.coroutines.flow.SharedFlow

private val LocalCurrentTime = compositionLocalOf<Long> { 0L }

@Composable
fun StopDetailScreen(
    modifier: Modifier = Modifier
) {
    val viewModel: StopDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalCurrentTime provides uiState.currentTime) {
        StopDetailContent(
            stopId = uiState.stopId,
            stopDetail = uiState.stopDetail,
            isLoading = uiState.isLoading,
            timer = uiState.timer,
            refreshEvent = viewModel.refreshEvent,
            onRefresh = viewModel::refreshData,
            onAddToFavorites = viewModel::addToFavorites,
            modifier = modifier
        )
    }
}

@Composable
private fun StopDetailContent(
    stopId: String,
    stopDetail: BusStopDetail,
    isLoading: Boolean,
    timer: Int,
    refreshEvent: SharedFlow<Unit>,
    onRefresh: () -> Unit,
    onAddToFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        IconButton(
            onClick = onAddToFavorites,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 30.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.BookmarkAdd,
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
                isLoading = isLoading
            )
        }
        RefreshButton(
            refreshEvent = refreshEvent,
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
private fun RefreshButton(
    refreshEvent: SharedFlow<Unit>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rotation by remember { mutableFloatStateOf(0f) }

    val animRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        )
    )

    LaunchedEffect(Unit) {
        refreshEvent.collect {
            rotation += 180f
        }
    }

    FloatingActionButton(
        onClick = onClick,
        containerColor = DarkGreen,
        shape = CircleShape,
        modifier = modifier
            .padding(25.dp)
            .graphicsLayer {
                rotationZ = animRotation
            }
    ) {
        Icon(
            imageVector = Icons.Filled.Autorenew,
            contentDescription = stringResource(R.string.refresh),
            tint = Color.Black
        )
    }
}

@Composable
private fun BusInfoHeader(
    busNumber: String,
    nextStopName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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
                .weight(0.55f)
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
private fun ArrivalTimeText(
    arrivalTime: Long?,
    modifier: Modifier = Modifier
) {
    val currentTime = LocalCurrentTime.current
    var displayTime by rememberSaveable { mutableStateOf("") }

    if (arrivalTime != null) {
        displayTime = setTextRemainingTime(arrivalTime, currentTime)
    }

    Text(
        text = displayTime,
        modifier = modifier,
        textAlign = TextAlign.End,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
private fun ArrivalInfo(
    arrivalInfo: BusArrivalInfo?,
    position: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.nth_bus, position + 1),
            modifier = Modifier.weight(2f),
            style = MaterialTheme.typography.bodyMedium
        )
        ArrivalTimeText(
            arrivalTime = arrivalInfo?.arrivalTime,
            modifier = Modifier.weight(2.5f)
        )
        Text(
            text = arrivalInfo?.position ?: stringResource(R.string.no_data),
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = arrivalInfo?.getCongestionText() ?: stringResource(R.string.no_data),
            color = arrivalInfo?.getCongestionColor() ?: colorResource(R.color.congestion_unknown),
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun BusItem(
    busInfo: BusInfo,
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
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(top = 15.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        ArrivalInfo(
            arrivalInfo = busInfo.arrivalInfos.getOrNull(0),
            position = 0,
            modifier = Modifier
                .padding(horizontal = 20.dp)
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
}

@Composable
private fun BusList(
    busInfos: List<BusInfo>,
    isLoading: Boolean,
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
                BusItem(busInfo)
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
