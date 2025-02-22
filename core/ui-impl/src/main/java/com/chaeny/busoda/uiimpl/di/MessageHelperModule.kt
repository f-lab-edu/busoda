package com.chaeny.busoda.uiimpl.di

import com.chaeny.busoda.ui.MessageHelper
import com.chaeny.busoda.uiimpl.ToastMessageHelper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
abstract class MessageHelperModule {

    @Binds
    abstract fun bindMessageHelper(implementation: ToastMessageHelper): MessageHelper
}
