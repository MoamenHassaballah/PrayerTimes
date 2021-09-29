package com.moaapps.prayertimesdemo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.moaapps.prayertimesdemo.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {
    companion object{
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, LocationActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var binding: ActivityLocationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setManually.setOnToggledListener { _, isOn ->
            if (isOn){
                binding.autoLocate.isOn = false
                binding.manualLocationLayout.visibility = View.VISIBLE
            }
        }

        binding.autoLocate.setOnToggledListener { _, isOn ->
            if (isOn){
                binding.setManually.isOn = false
                binding.manualLocationLayout.visibility = View.GONE
            }
        }

        binding.saveLocation.setOnClickListener { MainActivity.start(this) }


    }
}