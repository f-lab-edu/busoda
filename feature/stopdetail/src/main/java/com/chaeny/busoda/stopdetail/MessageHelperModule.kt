package com.chaeny.busoda.stopdetail

import com.chaeny.busoda.ui.MessageHelper
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
