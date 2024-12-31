package com.chaeny.busoda.stoplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        adapter.submitList(viewModel.dummyData)
        return binding.root
    }
}
