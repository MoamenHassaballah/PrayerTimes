package com.moaapps.prayertimesdemo.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.moaapps.prayertimesdemo.databinding.ActivitySettingsBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class SettingsActivity : AppCompatActivity() {

    companion object{
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, SettingsActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setLocation.setOnClickListener { LocationActivity.start(this) }
    }
}