package com.moaapps.prayertimesdemo.background_tasks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.modules.PrayerTimes
import com.moaapps.prayertimesdemo.utils.Constants.TIMINGS
import com.moaapps.prayertimesdemo.utils.TinyDB


class BootReceiver:BroadcastReceiver() {
    private val TAG = "PrayerBootReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: Boot Completed")
        val tinyDb = TinyDB(context!!)
        val prayerString = tinyDb.getString(TIMINGS)
        if (prayerString.isNullOrEmpty()){
            Log.d(TAG, "on boot timings: empty")
        }else{
            Log.d(TAG, "on boot timings: $prayerString")
            val type = object : TypeToken<PrayerTimes>() {}.type!!
            val prayerTimes = Gson().fromJson<PrayerTimes>(prayerString, type)
            PrayerReminder(context, prayerTimes).setAlarms()
        }
    }
}