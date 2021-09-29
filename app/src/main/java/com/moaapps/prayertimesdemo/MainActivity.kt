package com.moaapps.prayertimesdemo

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    companion object{
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, MainActivity::class.java)
            context.startActivity(starter)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}