package com.chaeny.busoda.stoplist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.chaeny.busoda.model.BusStop
import com.chaeny.busoda.stoplist.databinding.FragmentStopListBinding
import com.chaeny.busoda.ui.MessageHelper
import com.chaeny.busoda.ui.event.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StopListFragment : Fragment() {

    private lateinit var binding: FragmentStopListBinding
    private val viewModel: StopListViewModel by viewModels()
    @Inject
    lateinit var messageHelper: MessageHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopListBinding.inflate(inflater, container, false)
        val adapter = StopListAdapter(viewModel::handleBusStopClick)
        binding.stopList.adapter = adapter
        subscribeStopListUpdate(adapter)
        subscribeStopSpecificEvent()
        subscribeStopClickEvent()
        setupSearchView()
        return binding.root
    }

    private fun subscribeStopListUpdate(adapter: StopListAdapter) {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.stopListLoadingBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.busStops.observe(viewLifecycleOwner) { stops ->
            adapter.submitList(stops)
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

    private fun setupSearchView() {
        binding.composeSearchView.setContent {
            MaterialTheme {
                SearchBar(viewModel = viewModel)
            }
        }

        showSoftKeyboard(binding.composeSearchView)
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = requireContext().getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
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
                Log.d("SearchBar", "keyword=$keyword, it=$it")
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
                .padding(horizontal = 30.dp, vertical = 20.dp)
        )
    }

    @Composable
    fun StopItem(
        modifier: Modifier = Modifier,
        stop: BusStop
    ) {
        Column(modifier.fillMaxWidth().padding(15.dp)) {
            Text(text = stop.stopName)
            Spacer(modifier = Modifier.height(5.dp))
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

}
