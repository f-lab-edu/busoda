package com.chaeny.busoda

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chaeny.busoda.databinding.ActivityMainBinding
import com.chaeny.busoda.stopdetail.StopDetailFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(binding.container.id, StopDetailFragment())
            .commit()
    }

}