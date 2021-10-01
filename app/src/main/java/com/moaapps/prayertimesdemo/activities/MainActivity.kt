package com.moaapps.prayertimesdemo.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.AndroidNetworking
import com.google.android.material.snackbar.Snackbar
import com.moaapps.prayertimesdemo.databinding.ActivityMainBinding
import com.moaapps.prayertimesdemo.modules.PrayerTimes
import com.moaapps.prayertimesdemo.utils.Constants.CITY
import com.moaapps.prayertimesdemo.utils.Constants.COUNTRY
import com.moaapps.prayertimesdemo.utils.Constants.STATE
import com.moaapps.prayertimesdemo.utils.LoadingDialog
import com.moaapps.prayertimesdemo.utils.Status.*
import com.moaapps.prayertimesdemo.utils.TinyDB
import com.moaapps.prayertimesdemo.viewmodel.PrayerTimesViewModel
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object{
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, MainActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }

        private const val TAG = "MainActivity"
    }

    private lateinit var binding:ActivityMainBinding
    private lateinit var tinyDB: TinyDB
    private lateinit var prayerTimesViewModel: PrayerTimesViewModel
    private lateinit var loadingDialog: LoadingDialog
    private var countRemainingTime:Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AndroidNetworking.initialize(applicationContext);
        prayerTimesViewModel = ViewModelProvider(this)[PrayerTimesViewModel::class.java]
        tinyDB = TinyDB(this)
        loadingDialog = LoadingDialog(this, 0)

        prayerTimesViewModel.prayerTimes.observe(this, {
            when (it.status) {
                LOADING -> loadingDialog.show()
                SUCCESSFUL -> {
                    loadingDialog.dismiss()
                    setData(it.data!!)
                }
                FAIL -> {
                    loadingDialog.dismiss()
                    Snackbar.make(binding.root, it.message, Snackbar.LENGTH_SHORT).show()
                }
            }
        })


        binding.settings.setOnClickListener { SettingsActivity.start(this) }
        binding.locationCard.setOnClickListener { LocationActivity.start(this) }


        prayerTimesViewModel.getPrayerTimes(this)
    }

    private fun setData(prayerTimes: PrayerTimes) {
        val city = tinyDB.getString(CITY)
        val state = tinyDB.getString(STATE)
        val country = tinyDB.getString(COUNTRY)

        binding.city.text = if (city.isNullOrEmpty()) state else city
        binding.stateCountry.text = "$state, $country"
        binding.fajr.text = prayerTimes.fajr
        binding.dhuhr.text = prayerTimes.dhuhr
        binding.asr.text = prayerTimes.asr
        binding.maghrib.text = prayerTimes.maghrib
        binding.isha.text = prayerTimes.isha


        setTimeDifference(prayerTimes)

        Thread{
            while (countRemainingTime){
                Thread.sleep(60000)
                runOnUiThread {setTimeDifference(prayerTimes)}
            }
        }.start()
    }

    private fun setTimeDifference(prayerTimes: PrayerTimes) {
        var timeDifference: Long = 0;

        val now = Calendar.getInstance().timeInMillis

        if (calculateTimeDifference(prayerTimes.isha, now) > 0) {

            val differenceList = listOf(
                calculateTimeDifference(prayerTimes.fajr, now),
                calculateTimeDifference(prayerTimes.dhuhr, now),
                calculateTimeDifference(prayerTimes.asr, now),
                calculateTimeDifference(prayerTimes.maghrib, now),
                calculateTimeDifference(prayerTimes.isha, now)
            )


            for (dif in differenceList) {
                if (dif > 0) {
                    if (timeDifference > 0 && dif < timeDifference) {
                        timeDifference = dif
                    } else if (timeDifference <= 0) {
                        timeDifference = dif
                    }

                }
            }
        } else {
            timeDifference = calculateTimeDifference(prayerTimes.fajr, now, true)
        }


        val minutes = (timeDifference / (1000 * 60) % 60)
        val hours = (timeDifference / (1000 * 60 * 60) % 24)

        binding.remainingTime.text = "${hours}h ${minutes}m"
    }

    private fun calculateTimeDifference(time: String, now: Long, forNextDay: Boolean = false) : Long {
        val timeParts = time.split(":")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        calendar.set(Calendar.MINUTE, timeParts[1].toInt())
        if (forNextDay) calendar.add(Calendar.DAY_OF_MONTH, 1)

        val difference = calendar.timeInMillis - now
        return if (difference < 0 ) 0 else difference
    }


    override fun onDestroy() {
        countRemainingTime = false
        super.onDestroy()
    }

}