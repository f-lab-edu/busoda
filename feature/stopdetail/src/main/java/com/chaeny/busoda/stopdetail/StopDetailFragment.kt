package com.chaeny.busoda.stopdetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chaeny.busoda.stopdetail.databinding.FragmentStopDetailBinding

class StopDetailFragment : Fragment() {

    private lateinit var binding: FragmentStopDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopDetailBinding.inflate(inflater, container, false)
        initDataReceiver()
        return binding.root
    }

    private fun initDataReceiver() {
        binding.stopDetailTextView.text = arguments?.getString("data") ?: "No Data"
    }

}