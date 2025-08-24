package com.chaeny.busoda.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.ui.theme.Gray60

@Composable
fun FavoritesScreen(
    navigateToStopList: () -> Unit,
    navigateToStopDetail: (String) -> Unit = {}
) {
    val viewModel: FavoritesViewModel = hiltViewModel()
    val favorites by viewModel.favorites.collectAsState()

    Column {
        SearchBar(navigateToStopList = navigateToStopList)
        //FavoritesGuide()
        FavoritesStopList(
            stops = favorites,
            onClickItem = viewModel::handleFavoriteStopClick
        )
    }
    CollectFavoriteStopClickEvent(navigateToStopDetail, viewModel)
}

@Composable
private fun CollectFavoriteStopClickEvent(
    navigateToStopDetail: (String) -> Unit,
    viewModel: FavoritesViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.favoriteStopClicked.collect { stopId ->
            navigateToStopDetail(stopId)
        }
    }
}

@Composable
private fun SearchBar(
    navigateToStopList: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = TextFieldValue(""),
        onValueChange = {},
        enabled = false,
        colors = TextFieldDefaults.colors(
            disabledContainerColor = Color.Transparent
        ),
        placeholder = {
            Text(
                text = stringResource(R.string.stop_search),
                color = Color.Gray
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp)
            .padding(top = 20.dp)
            .clickable { navigateToStopList() }
    )
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
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun FavoritesStopList(
    stops: List<BusStop>,
    onClickItem: (String) -> Unit,
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
                StopItem(stop, onClickItem)
            }
        }
    }
}

@Composable
private fun StopItem(
    stop: BusStop,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 30.dp)
            .padding(bottom = 15.dp),
        onClick = { onClick(stop.stopId) },
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

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    SearchBar(navigateToStopList = {})
}

@Preview(showBackground = true)
@Composable
private fun FavoritesGuidePreview() {
    FavoritesGuide()
}

@Preview(showBackground = true)
@Composable
private fun FavoritesScreenPreview() {
    FavoritesScreen(navigateToStopList = {})
}

@Preview(showBackground = true)
@Composable
private fun FavoritesStopListPreview() {
    FavoritesStopList(stops = dummyData, onClickItem = {})
}

private val dummyData = listOf(
    BusStop("02218", "남대문경찰서.서울역10번출구", "숭례문"),
    BusStop("03119", "신용산역3번출구", "신용산지하차도"),
    BusStop("19114", "영등포역", "신길역5호선"),
    BusStop("19113", "영등포역.패어필드호텔", "경방타임스퀘어.신세계백화점")
)
