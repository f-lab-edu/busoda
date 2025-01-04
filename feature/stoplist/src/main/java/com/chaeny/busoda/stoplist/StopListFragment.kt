package com.chaeny.busoda.stoplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.chaeny.busoda.stoplist.databinding.FragmentStopListBinding
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController

class StopListFragment : Fragment() {

    private lateinit var binding: FragmentStopListBinding
    private val viewModel: StopListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopListBinding.inflate(inflater, container, false)
        val adapter = StopListAdapter()
        binding.stopList.adapter = adapter
        subscribeUi(adapter)
        initButtonAction()
        return binding.root
    }

    private fun subscribeUi(adapter: StopListAdapter) {
        viewModel.busStops.observe(viewLifecycleOwner) { stops ->
            adapter.submitList(stops)
        }
    }

    private fun initButtonAction() {
        binding.removeStopButton.setOnClickListener {
            viewModel.removeLastStop()
        }

        binding.moveStopButton.setOnClickListener {
            navigateToStopDetail()
        }
    }

    private fun navigateToStopDetail() {
        val uri = "android-app://com.chaeny.busoda/fragment_stop_detail"
        val request = NavDeepLinkRequest.Builder
            .fromUri(uri.toUri())
            .build()
        findNavController().navigate(request)
    }
}
