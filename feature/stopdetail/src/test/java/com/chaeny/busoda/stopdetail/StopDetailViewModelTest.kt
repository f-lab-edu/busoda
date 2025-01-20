package com.chaeny.busoda.stopdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.chaeny.busoda.stopdetail.util.MainCoroutineScopeRule
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class StopDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @Test
    fun whenInitialized_busInfosIsNull() {
        val viewModel = StopDetailViewModel(SavedStateHandle())
        assertNull(viewModel.busInfos.value)
    }
}
