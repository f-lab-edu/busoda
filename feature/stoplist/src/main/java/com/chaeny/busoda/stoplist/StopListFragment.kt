package com.chaeny.busoda.stoplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.appcompat.widget.SearchView
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.chaeny.busoda.stoplist.databinding.FragmentStopListBinding
import com.chaeny.busoda.ui.MessageHelper
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
        subscribeUi(adapter)
        subscribeStopResultError()
        subscribeStopClickEvent()
        setupSearchView()
        return binding.root
    }

    private fun subscribeUi(adapter: StopListAdapter) {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.stopListLoadingBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.busStops.observe(viewLifecycleOwner) { stops ->
            adapter.submitList(stops)
        }
    }

    private fun subscribeStopClickEvent() {
        viewModel.busStopClicked.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { stopId ->
                navigateToStopDetail(stopId)
            }
        }
    }

    private fun subscribeStopResultError() {
        viewModel.isNoResult.observe(viewLifecycleOwner) { isNoResult ->
            if (isNoResult) {
                messageHelper.showMessage(
                    requireContext(),
                    requireContext().getString(R.string.no_result)
                )
            }
        }

        viewModel.isNetworkError.observe(viewLifecycleOwner) { isNetworkError ->
            if (isNetworkError) {
                messageHelper.showMessage(
                    requireContext(),
                    requireContext().getString(R.string.network_error)
                )
            }
        }
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
    }
}
