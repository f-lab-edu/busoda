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
        val adapter = StopDetailAdapter()
        binding.busList.adapter = adapter
        val dummyData = listOf(
            Bus(
                "604", "화곡본동시장", "2분 38초", "2번째 전", "보통",
                "16분 18초", "9번째 전", "혼잡"),
            Bus(
                "5712", "화곡본동시장", "3분 38초", "3번째 전", "보통",
                "17분 18초", "10번째 전", "혼잡"),
            Bus(
                "652", "화곡역1번출구", "4분 38초", "4번째 전", "보통",
                "18분 18초", "11번째 전", "혼잡")
        )
        adapter.submitList(dummyData)
        return binding.root
    }
}
