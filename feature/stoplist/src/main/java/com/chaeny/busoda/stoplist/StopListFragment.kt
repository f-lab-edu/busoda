package com.chaeny.busoda.stoplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
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
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    viewModel.setKeyWord(it)
                }
                return false
            }
        })

        showSoftKeyboard(binding.searchView)
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = requireContext().getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
