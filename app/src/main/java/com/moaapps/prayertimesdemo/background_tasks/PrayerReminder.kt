package com.moaapps.prayertimesdemo.background_tasks

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.modules.PrayerTimes
import com.moaapps.prayertimesdemo.utils.Constants.PRAYER_REMINDER
import com.moaapps.prayertimesdemo.utils.TinyDB
import java.util.*

class PrayerReminder(val context: Context, val prayerTimes: PrayerTimes) {
    companion object{private const val TAG = "PrayerReminder"}
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun setAlarms(){
        val isReminderEnabled = TinyDB(context).getBoolean(PRAYER_REMINDER)
        if (isReminderEnabled){
            Log.d(TAG, "setReminder: ")
            createAlarm(1, prayerTimes.fajr, context.getString(R.string.fajr))
            createAlarm(2, prayerTimes.dhuhr, context.getString(R.string.dhuhr))
            createAlarm(3, prayerTimes.asr, context.getString(R.string.asr))
            createAlarm(4, prayerTimes.maghrib, context.getString(R.string.maghrib))
            createAlarm(5, prayerTimes.isha, context.getString(R.string.isha))
        }else{
            cancelAlarms()
        }

    }

    private fun createAlarm(id: Int, time:String, prayerName: String){
        val now = Calendar.getInstance().timeInMillis
        val timeParts = time.split(":")
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
        calendar.set(Calendar.MINUTE, timeParts[1].toInt())
        if (calendar.timeInMillis < now){
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("title", context.getString(R.string.prayer_notification_title, prayerName))
        intent.putExtra("message", context.getString(R.string.prayer_notification_message, prayerName))
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm(id: Int, prayerName: String){
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("title", context.getString(R.string.prayer_notification_title, prayerName))
        intent.putExtra("message", context.getString(R.string.prayer_notification_message, prayerName))
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)
        alarmManager.cancel(pendingIntent)
    }


    fun cancelAlarms(){
        val isReminderEnabled = TinyDB(context).getBoolean(PRAYER_REMINDER)
        if (!isReminderEnabled){
            Log.d(TAG, "cancelReminder: ")
            cancelAlarm(1, context.getString(R.string.fajr))
            cancelAlarm(2, context.getString(R.string.dhuhr))
            cancelAlarm(3, context.getString(R.string.asr))
            cancelAlarm(4, context.getString(R.string.maghrib))
            cancelAlarm(5, context.getString(R.string.isha))
        }else{
            setAlarms()
        }
    }

}