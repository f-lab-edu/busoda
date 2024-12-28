package com.chaeny.busoda.stoplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.chaeny.busoda.stoplist.databinding.FragmentStopListBinding

class StopListFragment : Fragment() {

    private lateinit var binding: FragmentStopListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopListBinding.inflate(inflater, container, false)
        initButtonAction()
        return binding.root
    }

    private fun initButtonAction() {
        binding.sendLeftButton.setOnClickListener {
            navigateToStopDetail("🚌")
        }

        binding.sendArriveButton.setOnClickListener {
            navigateToStopDetail("🚍")
        }
    }

    private fun navigateToStopDetail(data: String) {
        val uri = "android-app://com.chaeny.busoda/fragment_stop_detail?data=$data".toUri()
        val request = NavDeepLinkRequest.Builder
            .fromUri(uri)
            .build()
        findNavController().navigate(request)
    }

}