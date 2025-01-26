package com.chaeny.busoda.stoplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.chaeny.busoda.stoplist.databinding.FragmentStopListBinding

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
        setupRemoveButton()
        subscribeRemoveEvent()
        return binding.root
    }

    private fun subscribeUi(adapter: StopListAdapter) {
        viewModel.busStops.observe(viewLifecycleOwner) { stops ->
            adapter.submitList(stops)
        }
    }

    private fun subscribeRemoveEvent() {
        viewModel.removeCompleted.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { resId ->
                val message = getString(resId)
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRemoveButton() {
        binding.removeStopButton.setOnClickListener {
            viewModel.removeLastStop()
        }
    }
}
