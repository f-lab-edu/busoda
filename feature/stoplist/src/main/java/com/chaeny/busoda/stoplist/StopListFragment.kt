package com.chaeny.busoda.stoplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chaeny.busoda.stoplist.databinding.FragmentStopListBinding

class StopListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentStopListBinding.inflate(inflater, container, false)
        return binding.root
    }

}