package com.chaeny.busoda.stoplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
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
        setupRemoveButton()
        subscribeRemoveEvent()
        subscribeStopClickEvent()
        return binding.root
    }

    private fun subscribeUi(adapter: StopListAdapter) {
        viewModel.busStops.observe(viewLifecycleOwner) { stops ->
            adapter.submitList(stops)
        }
    }

    private fun subscribeRemoveEvent() {
        viewModel.removeCompleted.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                messageHelper.showMessage(requireContext(), result.getRemoveMessage())
            }
        }
    }

    private fun RemoveResult.getRemoveMessage(): String {
        return when (this) {
            RemoveResult.SUCCESS -> getString(R.string.remove_stop_completed)
        }
    }

    private fun subscribeStopClickEvent() {
        viewModel.busStopClicked.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { stopId ->
                navigateToStopDetail(stopId)
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

    private fun setupRemoveButton() {
        binding.removeStopButton.setOnClickListener {
            viewModel.removeLastStop()
        }
    }
}
