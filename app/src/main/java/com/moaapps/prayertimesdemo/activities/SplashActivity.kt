package com.moaapps.prayertimesdemo.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.utils.Constants.LATITUDE
import com.moaapps.prayertimesdemo.utils.SetAppLocale.setAppLocale
import com.moaapps.prayertimesdemo.utils.TinyDB
import kotlinx.coroutines.ExperimentalCoroutinesApi

@SuppressLint("CustomSplashScreen")
@ExperimentalCoroutinesApi
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        setAppLocale(this)

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