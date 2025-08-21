package com.chaeny.busoda.stoplist

import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.ui.theme.DarkGreen
import com.chaeny.busoda.ui.theme.Gray40
import com.chaeny.busoda.ui.theme.Gray60

@Composable
fun StopListScreen(
    onStopClick: (String) -> Unit = {}
) {
    val viewModel: StopListViewModel = hiltViewModel()
    val stops by viewModel.busStops.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    CollectStopSpecificEvent(viewModel)
    CollectStopClickEvent(onStopClick, viewModel)
    StopListContent(
        stops = stops,
        isLoading = isLoading,
        onKeywordChange = viewModel::setKeyWord,
        onStopClick = viewModel::handleBusStopClick
    )
}

@Composable
private fun CollectStopClickEvent(
    onStopClick: (String) -> Unit,
    viewModel: StopListViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.busStopClicked.collect { stopId ->
            onStopClick(stopId)
        }
    }
}

@Composable
private fun CollectStopSpecificEvent(viewModel: StopListViewModel) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.searchEvent.collect { event ->
            val messageResId = when (event) {
                SearchEvent.NoResult -> R.string.no_result
                SearchEvent.NoInternet -> R.string.no_internet
                SearchEvent.NetworkError -> R.string.network_error
                SearchEvent.ShortKeyword -> R.string.short_keyword
            }
            Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun StopListContent(
    stops: List<BusStop>,
    isLoading: Boolean,
    onKeywordChange: (String) -> Unit,
    onStopClick: (String) -> Unit
) {
    Column {
        SearchBarContent(onKeywordChange)
        StopList(stops, isLoading, onStopClick)
    }
}

@Composable
private fun SearchBarContent(
    setKeyWord: (String) -> Unit
) {
    var keyword by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    SearchBar(
        keyword,
        onKeywordChange = {
            keyword = it.copy(selection = TextRange(it.text.length))
            setKeyWord(it.text)
        }
    )
}

@Composable
private fun SearchBar(
    keyword: TextFieldValue,
    onKeywordChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp, start = 20.dp, end = 36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = stringResource(R.string.arrow_back),
                tint = Color.Gray
            )
        }

        TextField(
            value = keyword,
            onValueChange = onKeywordChange,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Gray40
            ),
            placeholder = {
                Text(
                    stringResource(R.string.stop_search), color = Color.Gray
                )
            },
            modifier = modifier
                .weight(1f)
                .focusRequester(focusRequester)
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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

@Composable
private fun StopList(
    stops: List<BusStop>,
    isLoading: Boolean,
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
private fun SearchBarPreview() {
    SearchBar(
        keyword = TextFieldValue(""),
        onKeywordChange = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun StopItemPreview() {
    StopItem(
        BusStop("정류장ID", "정류장", "다음정류장"),
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun StopListPreview() {
    StopList(stops = dummyData, isLoading = true, onClickItem = {})
}

private val dummyData = listOf(
    BusStop("16206", "화곡역4번출구", "화곡본동시장"),
    BusStop("16206", "화곡역4번출구", "화곡본동시장"),
    BusStop("16206", "화곡역4번출구", "화곡본동시장"),
    BusStop("16206", "화곡역4번출구", "화곡본동시장"),
    BusStop("16206", "화곡역4번출구", "화곡본동시장"),
    BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교"),
    BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교"),
    BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교"),
    BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교"),
    BusStop("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교")
)
