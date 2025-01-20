package com.chaeny.busoda.stopdetail

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chaeny.busoda.stopdetail.util.MainCoroutineScopeRule
import org.junit.Rule

class StopDetailViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()
}
