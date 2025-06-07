package com.chaeny.busoda.stoplist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.ui.MessageHelper
import com.chaeny.busoda.ui.theme.DarkGreen
import com.chaeny.busoda.ui.theme.Gray40
import com.chaeny.busoda.ui.theme.Gray60
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StopListFragment : Fragment() {
    private val viewModel: StopListViewModel by viewModels()
    @Inject
    lateinit var messageHelper: MessageHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        subscribeStopSpecificEvent()
        subscribeStopClickEvent()

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Column {
                        SearchBarContent()
                        StopListContent()
                        CollectCountFlows()
                    }
                }
            }
        }
    }

    private fun subscribeStopClickEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.busStopClicked.collect { stopId ->
                    navigateToStopDetail(stopId)
                }
            }
        }
    }

    private fun subscribeStopSpecificEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isNoResult.collect { isNoResult ->
                        if (isNoResult) showMessage(R.string.no_result)
                    }
                }
                launch {
                    viewModel.isNoInternet.collect { isNoInternet ->
                        if (isNoInternet) showMessage(R.string.no_internet)
                    }
                }
                launch {
                    viewModel.isNetworkError.collect { isNetworkError ->
                        if (isNetworkError) showMessage(R.string.network_error)
                    }
                }
                launch {
                    viewModel.isKeywordTooShort.collect { isKeywordTooShort ->
                        if (isKeywordTooShort) showMessage(R.string.short_keyword)
                    }
                }
            }
        }
    }

    private fun showMessage(stringResId: Int) {
        messageHelper.showMessage(requireContext(), requireContext().getString(stringResId))
    }

    private fun navigateToStopDetail(stopId: String) {
        val uri = "android-app://com.chaeny.busoda/fragment_stop_detail?stopId=$stopId"
        val request = NavDeepLinkRequest.Builder
            .fromUri(uri.toUri())
            .build()
        findNavController().navigate(request)
    }

    @Composable
    private fun SearchBarContent() {
        var keyword by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue(""))
        }
        SearchBar(keyword,
            onKeywordChange = {
                keyword = it.copy(selection = TextRange(it.text.length))
                viewModel.setKeyWord(it.text)
            }
        )
    }

    @Composable
    fun CollectCountFlows() {
        val startCollect = rememberSaveable { mutableStateOf(true) }

        if (startCollect.value) {
            val eagerly by viewModel.eagerlyFlow.collectAsState()
            val lazily by viewModel.lazilyFlow.collectAsState()
            val ws0 by viewModel.whileSubscribed0Flow.collectAsState()
            val ws5s by viewModel.whileSubscribed5sFlow.collectAsState()
            Log.e("CollectCountFlows", "Eagerly: $eagerly")
            Log.e("CollectCountFlows", "Lazily: $lazily")
            Log.e("CollectCountFlows", "WS0: $ws0")
            Log.e("CollectCountFlows", "WS5S: $ws5s")
        }

        LaunchedEffect(Unit) {
            delay(10000)
            startCollect.value = false
            Log.e("CollectCountFlows", "Collect stop")
        }
    }

    @Composable
    private fun StopListContent() {
        val stops by viewModel.busStops.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        StopList(stops, isLoading, onClickItem = { stopId -> viewModel.handleBusStopClick(stopId) })
    }

    @Composable
    private fun SearchBar(
        keyword: TextFieldValue,
        onKeywordChange: (TextFieldValue) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val focusRequester = remember { FocusRequester() }

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
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .padding(horizontal = 36.dp)
                .padding(top = 20.dp)
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }

    @Composable
    fun StopItem(
        stop: BusStop,
        onClick: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
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
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp)
                    .padding(top = 15.dp, bottom = 5.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
    fun StopList(
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
                items(stops) { stop ->
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
    fun SearchBarPreview() {
        SearchBarContent()
    }

    @Preview(showBackground = true)
    @Composable
    fun StopItemPreview() {
        StopItem(
            BusStop("정류장ID", "정류장", "다음정류장"),
            onClick = {}
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun StopListPreview() {
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

}
