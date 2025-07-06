package com.chaeny.busoda

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.chaeny.busoda.stoplist.StopListScreen
import com.chaeny.busoda.ui.theme.BusodaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BusodaTheme {
                StopListScreen()
            }
        }
    }
}
