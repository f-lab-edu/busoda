package com.chaeny.busoda.stoplist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chaeny.busoda.stoplist.databinding.FragmentStopListBinding

class StopListFragment : Fragment() {

    private lateinit var binding: FragmentStopListBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStopListBinding.inflate(inflater, container, false)
        val adapter = StopListAdapter()
        binding.stopList.adapter = adapter
        val dummyData = listOf(
            listOf("16206", "화곡역4번출구", "화곡본동시장"),
            listOf("16146", "화곡본동시장", "한국폴리텍1.서울강서대학교"),
            listOf("16143", "한국폴리텍1.서울강서대학교", "우장초등학교"),
            listOf("16142", "우장초등학교", "강서구청.한국건강관리협회"),
            listOf("16139", "강서구청.한국건강관리협회", "강서구청사거리.서울디지털대학교")
        )
        adapter.submitList(dummyData)
        return binding.root
    }
}
