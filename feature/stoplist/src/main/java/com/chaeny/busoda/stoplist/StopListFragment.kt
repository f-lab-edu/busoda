package com.chaeny.busoda.stoplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                        SetupSearchView()
                        SetupStopList()
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

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = requireContext().getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    @Composable
    private fun SetupSearchView() {
        SearchBar(viewModel = viewModel)
    }

    @Composable
    private fun SetupStopList() {
        val stops by viewModel.busStops.observeAsState(initial = emptyList())
        val isLoading by viewModel.isLoading.observeAsState(initial = false)
        StopList(stops = stops, isLoading = isLoading)
    }

    @Composable
    private fun SearchBar(
        modifier: Modifier = Modifier,
        viewModel: StopListViewModel,
    ) {
        var keyword by rememberSaveable { mutableStateOf("") }

        TextField(
            value = keyword,
            onValueChange = {
                keyword = it
                viewModel.setKeyWord(it)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Gray,
            ),
            placeholder = {
                Text(
                    stringResource(R.string.stop_search), color = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp).padding(top = 20.dp)
        )
    }

    @Composable
    fun StopItem(
        modifier: Modifier = Modifier,
        stop: BusStop
    ) {
        Column(modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(10.dp)
        ) {
            Text(text = stop.stopName)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stop.stopId,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stop.nextStopName
                )
            }
        }
    }

    @Composable
    fun StopList(
        modifier: Modifier = Modifier,
        stops: List<BusStop>,
        isLoading: Boolean
    ) {
        Box(modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
        ) {
            LazyColumn {
                items(stops) { stop ->
                    StopItem(stop = stop)
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun SearchBarPreview() {
        SearchBar(viewModel = viewModel)
    }

    @Preview(showBackground = true)
    @Composable
    fun StopItemPreview() {
        StopItem(
            stop = BusStop("정류장ID", "정류장", "다음정류장")
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun StopListPreview() {
        StopList(stops = dummyData, isLoading = true)
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
