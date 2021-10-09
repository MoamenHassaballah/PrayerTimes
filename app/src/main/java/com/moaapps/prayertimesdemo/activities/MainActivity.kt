package com.moaapps.prayertimesdemo.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.work.*
import com.androidnetworking.AndroidNetworking
import com.google.android.material.snackbar.Snackbar
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.background_tasks.GetLocation
import com.moaapps.prayertimesdemo.background_tasks.GetPrayerTimes
import com.moaapps.prayertimesdemo.background_tasks.RefreshWorker
import com.moaapps.prayertimesdemo.databinding.ActivityMainBinding
import com.moaapps.prayertimesdemo.modules.PrayerTimes
import com.moaapps.prayertimesdemo.utils.*
import com.moaapps.prayertimesdemo.utils.Constants.CITY
import com.moaapps.prayertimesdemo.utils.Constants.COUNTRY
import com.moaapps.prayertimesdemo.utils.Constants.STATE
import com.moaapps.prayertimesdemo.utils.Constants.TWELVE_TIME_FORMAT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    companion object{
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, MainActivity::class.java)
            starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(starter)
        }

    }

    private lateinit var binding:ActivityMainBinding
    private lateinit var tinyDB: TinyDB
    private lateinit var loadingDialog: LoadingDialog
    private var countRemainingTime:Boolean = true
    private lateinit var prayerTimes: PrayerTimes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AndroidNetworking.initialize(applicationContext)
        tinyDB = TinyDB(this)
        loadingDialog = LoadingDialog(this, 0)


        binding.timeFormatSwitch.setOnToggledListener { _, isOn ->
            if (isOn){
                setOriginalTime(prayerTimes)
            }else{
                convertTimeFormat(prayerTimes)
            }

            tinyDB.putBoolean(TWELVE_TIME_FORMAT, !isOn)
        }
        binding.settings.setOnClickListener { SettingsActivity.start(this) }
        binding.locationCard.setOnClickListener { LocationActivity.start(this) }


        val getLocation = GetLocation(this)
        val getPrayerTimes = GetPrayerTimes(this)
        CoroutineScope(Dispatchers.IO).launch {
            runOnUiThread { loadingDialog.show() }

            var lat = 0.0
            var lng = 0.0
            if (tinyDB.getString(Constants.LOCATION_METHOD) == Constants.LOCATION_METHOD_AUTO){
                val location = getLocation.getUserLocation()
                if (location != null){
                    lat = location.latitude
                    lng = location.longitude
                }else{
                    runOnUiThread { Snackbar.make(binding.root, getLocation.getError(), Snackbar.LENGTH_SHORT).show()}
                }

            }else {
                lat = tinyDB.getDouble(Constants.LATITUDE)
                lng = tinyDB.getDouble(Constants.LONGITUDE)
            }

            if (lat != 0.0 && lng != 0.0){
                val prayerTimes = getPrayerTimes.getPrayerTimes(lat, lng)
                runOnUiThread {
                    if (prayerTimes != null){
                        loadingDialog.dismiss()
                        setData(prayerTimes)
                    }else{
                        loadingDialog.dismiss()
                        Snackbar.make(binding.root, getPrayerTimes.getError(), Snackbar.LENGTH_SHORT).show()
                    }

                }
            }


        }
    }

    private fun setData(prayerTimes: PrayerTimes) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val refreshRequest =
            PeriodicWorkRequestBuilder<RefreshWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork("refresh", ExistingPeriodicWorkPolicy.KEEP, refreshRequest)

        this.prayerTimes = prayerTimes
        val city = tinyDB.getString(CITY)
        val state = tinyDB.getString(STATE)
        val country = tinyDB.getString(COUNTRY)

        binding.city.text = if (city.isNullOrEmpty()) state else city
        binding.stateCountry.text = "$state, $country"


        binding.timeFormatSwitch.isOn = !tinyDB.getBoolean(TWELVE_TIME_FORMAT)
        if (tinyDB.getBoolean(TWELVE_TIME_FORMAT)) {
            convertTimeFormat(prayerTimes)
        } else {
            setOriginalTime(prayerTimes)
        }


        setTimeDifference(prayerTimes)

        Thread {
            while (countRemainingTime) {
                Thread.sleep(60000)
                runOnUiThread { setTimeDifference(prayerTimes) }
            }
        }.start()
    }

    private fun setTimeDifference(prayerTimes: PrayerTimes) {
        var timeDifference: Long = 0
        var nextPrayer = ""

        val now = Calendar.getInstance().timeInMillis

        if (calculateTimeDifference(prayerTimes.isha, now) > 0) {

            val differenceList = listOf(
                calculateTimeDifference(prayerTimes.fajr, now),
                calculateTimeDifference(prayerTimes.dhuhr, now),
                calculateTimeDifference(prayerTimes.asr, now),
                calculateTimeDifference(prayerTimes.maghrib, now),
                calculateTimeDifference(prayerTimes.isha, now)
            )

            val prayersList = listOf(getString(R.string.fajr), getString(R.string.dhuhr),
                getString(R.string.asr), getString(R.string.maghrib), getString(R.string.isha))


            for (dif in differenceList) {
                if (dif > 0) {
                    if (timeDifference <= 0 || (timeDifference > 0 && dif < timeDifference)) {
                        timeDifference = dif
                        nextPrayer = prayersList[differenceList.indexOf(dif)]
                    }

                }
            }
        } else {
            timeDifference = calculateTimeDifference(prayerTimes.fajr, now, true)
            nextPrayer = getString(R.string.fajr)
        }


        val minutes = (timeDifference / (1000 * 60) % 60)
        val hours = (timeDifference / (1000 * 60 * 60) % 24)

        binding.remainingTime.text = getString(R.string.remaining_time_format, hours.toString(), minutes.toString())
        binding.remainingText.text = getString(R.string.remaining_for_the_next_prayer, nextPrayer)


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


    private fun setOriginalTime(prayerTimes: PrayerTimes){
        binding.fajr.text = prayerTimes.fajr
        binding.dhuhr.text = prayerTimes.dhuhr
        binding.asr.text = prayerTimes.asr
        binding.maghrib.text = prayerTimes.maghrib
        binding.isha.text = prayerTimes.isha
    }

    private fun convertTimeFormat(prayerTimes: PrayerTimes){
        binding.fajr.text = timeFormatConverter(prayerTimes.fajr)
        binding.dhuhr.text = timeFormatConverter(prayerTimes.dhuhr)
        binding.asr.text = timeFormatConverter(prayerTimes.asr)
        binding.maghrib.text = timeFormatConverter(prayerTimes.maghrib)
        binding.isha.text = timeFormatConverter(prayerTimes.isha)
    }

    private fun timeFormatConverter(time:String) : String{
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = dateFormat.parse(time)

        val newFormat = SimpleDateFormat("hh:mm aaa", Locale.getDefault())
        return newFormat.format(date!!)
    }


    override fun onDestroy() {
        countRemainingTime = false
        super.onDestroy()
    }

}