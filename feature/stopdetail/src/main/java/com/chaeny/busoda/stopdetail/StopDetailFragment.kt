package com.chaeny.busoda.stopdetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chaeny.busoda.stopdetail.databinding.FragmentStopDetailBinding
import androidx.fragment.app.viewModels

class StopDetailFragment : Fragment() {

    private lateinit var binding: FragmentStopDetailBinding
    private val viewModel: StopDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopDetailBinding.inflate(inflater, container, false)
        val adapter = StopDetailAdapter()
        binding.busList.adapter = adapter
        subscribeUi(adapter)
        bindReceivedData()
        return binding.root
    }

    private fun subscribeUi(adapter: StopDetailAdapter) {
        if(viewModel.isLoading()) {
            binding.listLoadingBar.visibility = View.VISIBLE
        }

        viewModel.busInfos.observe(viewLifecycleOwner) { buses ->
            adapter.submitList(buses)
            binding.listLoadingBar.visibility = View.GONE
        }
    }

    private fun bindReceivedData() {
        viewModel.stopName.observe(viewLifecycleOwner) { stopName ->
            binding.textBusStop.text = stopName
        }
    }
}
