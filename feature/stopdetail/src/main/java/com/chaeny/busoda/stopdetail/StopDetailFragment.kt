package com.chaeny.busoda.stopdetail

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.chaeny.busoda.stopdetail.databinding.FragmentStopDetailBinding
import com.chaeny.busoda.ui.event.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
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
        setupRefreshButton()
        subscribeCountdownTimer()
        subscribeRefreshEvent()
        return binding.root
    }

    private fun subscribeUi(adapter: StopDetailAdapter) {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.busListLoadingBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.stopDetail.observe(viewLifecycleOwner) { stopDetail ->
            with(binding) {
                textBusStopId.visibility = View.VISIBLE
                textBusStopName.text = stopDetail.stopName.ifEmpty {
                    requireContext().getString(R.string.no_info)
                }
            }
            adapter.submitList(stopDetail.busInfos)
        }
    }

    private fun bindReceivedData() {
        viewModel.stopId.observe(viewLifecycleOwner) { stopId ->
            binding.textBusStopId.text = stopId
        }
    }

    private fun subscribeCountdownTimer() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.timer.collect { countdownValue ->
                    updateBusAnimation(countdownValue)
                }
            }
        }
    }

    private fun updateBusAnimation(countdownValue: Int) {
        val maxCount = 15
        val moveStep = maxCount - countdownValue
        val totalDistance = binding.textStopEmoji.left - binding.textBusEmoji.left
        val stepDistance = (totalDistance / maxCount).toFloat()
        val translationValue = stepDistance * moveStep

        ObjectAnimator.ofFloat(binding.textBusEmoji, "translationX", translationValue).apply {
            duration = 1000
            start()
        }
    }

    private fun subscribeRefreshEvent() {
        viewModel.refreshEvent.observe(viewLifecycleOwner, EventObserver { isRefresh ->
            if (isRefresh) {
                startRotateAnimation(binding.refreshButton)
            }
        })
    }

    private fun setupRefreshButton() {
        binding.refreshButton.setOnClickListener {
            viewModel.refreshData()
        }
    }

    private fun startRotateAnimation(view: View) {
        ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 180f).apply {
            duration = 500
            start()
        }
    }
}
