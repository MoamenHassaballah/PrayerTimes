package com.moaapps.prayertimesdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.moaapps.prayertimesdemo.utils.Constants.LATITUDE
import com.moaapps.prayertimesdemo.utils.TinyDB

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            if (TinyDB(this).getDouble(LATITUDE) != 0.0){
                MainActivity.start(this)
            }else{
                LocationActivity.start(this)
            }
            finish()
        }, 2000)
    }
}