package com.moaapps.prayertimesdemo.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.moaapps.prayertimesdemo.background_tasks.PrayerReminder
import com.moaapps.prayertimesdemo.databinding.ActivitySettingsBinding
import com.moaapps.prayertimesdemo.modules.PrayerTimes
import com.moaapps.prayertimesdemo.utils.Constants
import com.moaapps.prayertimesdemo.utils.Constants.PRAYER_REMINDER
import com.moaapps.prayertimesdemo.utils.TinyDB
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

        val tinyDb = TinyDB(this)

        val prayerString = tinyDb.getString(Constants.TIMINGS)
        val type = object : TypeToken<PrayerTimes>() {}.type!!
        val prayerTimes = Gson().fromJson<PrayerTimes>(prayerString, type)
        val prayerReminder = PrayerReminder(this, prayerTimes)

        binding.reminder.setOnToggledListener { _, isOn ->
            tinyDb.putBoolean(PRAYER_REMINDER, isOn)
            if (isOn) {
                prayerReminder.setAlarms()
            }else{
                prayerReminder.cancelAlarms()
            }
        }
        binding.setLocation.setOnClickListener { LocationActivity.start(this) }

        val isReminderEnabled = tinyDb.getBoolean(PRAYER_REMINDER)
        binding.reminder.isOn = isReminderEnabled
        if (isReminderEnabled) {
            prayerReminder.setAlarms()
        }else{
            prayerReminder.cancelAlarms()
        }
    }
}