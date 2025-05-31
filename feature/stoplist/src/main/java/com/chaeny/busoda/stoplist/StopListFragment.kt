package com.chaeny.busoda.stoplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.ui.MessageHelper
import com.chaeny.busoda.ui.event.EventObserver
import dagger.hilt.android.AndroidEntryPoint
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
                    }
                }
            }
        }
    }

    private fun subscribeStopClickEvent() {
        viewModel.busStopClicked.observe(viewLifecycleOwner, EventObserver { stopId ->
            navigateToStopDetail(stopId)
        })
    }

    private fun subscribeStopSpecificEvent() {
        viewModel.isNoResult.observe(viewLifecycleOwner, EventObserver { isNoResult ->
            if (isNoResult) {
                showMessage(R.string.no_result)
            }
        })

        viewModel.isNoInternet.observe(viewLifecycleOwner, EventObserver { isNoInternet ->
            if (isNoInternet) {
                showMessage(R.string.no_internet)
            }
        })

        viewModel.isNetworkError.observe(viewLifecycleOwner, EventObserver { isNetworkError ->
            if (isNetworkError) {
                showMessage(R.string.network_error)
            }
        })

        viewModel.isKeywordTooShort.observe(viewLifecycleOwner, EventObserver { isKeywordTooShort ->
            if (isKeywordTooShort) {
                showMessage(R.string.short_keyword)
            }
        })
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
        var keyword by rememberSaveable { mutableStateOf("") }
        SearchBar(keyword, onKeywordChange = {
            keyword = it
            viewModel.setKeyWord(it)
        })
    }

    @Composable
    private fun StopListContent() {
        val stops by viewModel.busStops.observeAsState(initial = emptyList())
        val isLoading by viewModel.isLoading.observeAsState(initial = false)
        StopList(stops, isLoading, onClickItem = { stopId -> viewModel.handleBusStopClick(stopId) })
    }

    @Composable
    private fun SearchBar(
        keyword: String,
        onKeywordChange: (String) -> Unit,
        modifier: Modifier = Modifier
    ) {
        val focusRequester = remember { FocusRequester() }

        TextField(
            value = keyword,
            onValueChange = onKeywordChange,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFFADAEB0)
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
                .padding(bottom = 15.dp)
                .clickable { onClick(stop.stopId) },
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(
                    text = stop.stopName,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp)
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stop.stopId,
                        color = Color(0xFF5D5E64),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(0.3f)
                    )
                    Text(
                        text = stop.nextStopName,
                        color = Color(0xFF5D5E64),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.weight(0.7f)
                    )
                }
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
                    color = Color(0xFF80AF81)
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
