package com.chaeny.busoda.uiimpl

import android.content.Context
import android.widget.Toast
import com.chaeny.busoda.ui.MessageHelper
import javax.inject.Inject

class ToastMessageHelper @Inject constructor() : MessageHelper {
    override fun showMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
