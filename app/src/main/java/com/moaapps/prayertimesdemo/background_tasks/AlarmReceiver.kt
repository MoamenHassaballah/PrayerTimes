package com.moaapps.prayertimesdemo.background_tasks

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.modules.PrayerTimes
import com.moaapps.prayertimesdemo.utils.Constants
import com.moaapps.prayertimesdemo.utils.TinyDB
import kotlin.random.Random

class AlarmReceiver: BroadcastReceiver() {

    companion object {private const val TAG = "AlarmReceiver"}


    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: ALARM!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        notifyAlarm(context!!, intent?.getStringExtra("title")!!, intent.getStringExtra("message")!!)

        val tinyDb = TinyDB(context)
        val prayerString = tinyDb.getString(Constants.TIMINGS)
        if (!prayerString.isNullOrEmpty()){
            val type = object : TypeToken<PrayerTimes>() {}.type!!
            val prayerTimes = Gson().fromJson<PrayerTimes>(prayerString, type)
            PrayerReminder(context, prayerTimes).setAlarms()
        }
    }


    private fun notifyAlarm(context: Context, title: String, message: String) {
        val notification = NotificationCompat.Builder(context, context.getString(R.string.notification_channel))
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()


        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(context.getString(R.string.notification_channel), context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(Random.nextInt(), notification)
    }
}