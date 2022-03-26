package com.moaapps.prayertimesdemo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.activities.SplashActivity
import com.moaapps.prayertimesdemo.background_tasks.GetPrayerTimes
import com.moaapps.prayertimesdemo.modules.PrayerTimes
import com.moaapps.prayertimesdemo.utils.Constants
import com.moaapps.prayertimesdemo.utils.Constants.TIMINGS
import com.moaapps.prayertimesdemo.utils.TinyDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "PrayerWidget"
@ExperimentalCoroutinesApi
class PrayerWidget : AppWidgetProvider() {


    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: ")
        val tinyDb = TinyDB(context)
        val getPrayerTimes = GetPrayerTimes(context)
        CoroutineScope(Dispatchers.IO).launch {
            val lat = tinyDb.getDouble(Constants.LATITUDE)
            val lng = tinyDb.getDouble(Constants.LONGITUDE)

            if (lat != 0.0 && lng != 0.0){
                getPrayerTimes.getPrayerTimes(lat, lng)
            }
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }


        }


    }


}

@ExperimentalCoroutinesApi
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {


    val views = RemoteViews(context.packageName, R.layout.prayer_widget)
    val appIntent = Intent(context, SplashActivity::class.java)
    val intent = PendingIntent.getActivity(context, 11, appIntent, PendingIntent.FLAG_IMMUTABLE or 0)
    views.setOnClickPendingIntent(R.id.container, intent)
    val tinyDb = TinyDB(context)
    val prayerString = tinyDb.getString(TIMINGS)
    if (prayerString.isNullOrEmpty()){
        Log.d(TAG, "updateAppWidget: empty")
        views.setViewVisibility(R.id.no_data, View.VISIBLE)
    }else{
        Log.d(TAG, "updateAppWidget: $prayerString")
        views.setViewVisibility(R.id.no_data, View.GONE)
        val type = object : TypeToken<PrayerTimes>() {}.type!!
        val prayerTimes = Gson().fromJson<PrayerTimes>(prayerString, type)
        // Construct the RemoteViews object
        setData(context, prayerTimes, views, tinyDb)
    }


    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}


private fun setData(context:Context, prayerTimes: PrayerTimes, views: RemoteViews, tinyDB: TinyDB) {

    if (tinyDB.getBoolean(Constants.TWELVE_TIME_FORMAT)){
        convertTimeFormat(prayerTimes, views)
    }else{
        setOriginalTime(prayerTimes, views)
    }


    setTimeDifference(context, prayerTimes, views)
}

private fun setTimeDifference(context:Context, prayerTimes: PrayerTimes, views: RemoteViews) {
    var timeDifference: Long = 0
    var nextPrayer = 0

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
                if (timeDifference <= 0 || (timeDifference > 0 && dif < timeDifference)) {
                    timeDifference = dif
                    nextPrayer = differenceList.indexOf(dif)
                }

            }
        }
    } else {
        nextPrayer = 0
    }


    resetColor(context, views)
    when(nextPrayer){
        0 -> {
            views.setTextColor(R.id.fajr, ContextCompat.getColor(context, R.color.light_blue_900))
            views.setTextColor(R.id.fajr_text, ContextCompat.getColor(context, R.color.light_blue_900))
        }

        1 -> {
            views.setTextColor(R.id.dhuhr, ContextCompat.getColor(context, R.color.light_blue_900))
            views.setTextColor(R.id.dhuhr_text, ContextCompat.getColor(context, R.color.light_blue_900))
        }

        2 -> {
            views.setTextColor(R.id.asr, ContextCompat.getColor(context, R.color.light_blue_900))
            views.setTextColor(R.id.asr_text, ContextCompat.getColor(context, R.color.light_blue_900))
        }

        3 -> {
            views.setTextColor(R.id.maghrib, ContextCompat.getColor(context, R.color.light_blue_900))
            views.setTextColor(R.id.maghrib_text, ContextCompat.getColor(context, R.color.light_blue_900))
        }

        4 -> {
            views.setTextColor(R.id.isha, ContextCompat.getColor(context, R.color.light_blue_900))
            views.setTextColor(R.id.isha_text, ContextCompat.getColor(context, R.color.light_blue_900))
        }
    }


}

private fun resetColor(context: Context, views: RemoteViews){
    views.setTextColor(R.id.fajr, ContextCompat.getColor(context, R.color.black))
    views.setTextColor(R.id.fajr_text, ContextCompat.getColor(context, R.color.black))
    views.setTextColor(R.id.dhuhr, ContextCompat.getColor(context, R.color.black))
    views.setTextColor(R.id.dhuhr_text, ContextCompat.getColor(context, R.color.black))
    views.setTextColor(R.id.asr, ContextCompat.getColor(context, R.color.black))
    views.setTextColor(R.id.asr_text, ContextCompat.getColor(context, R.color.black))
    views.setTextColor(R.id.maghrib, ContextCompat.getColor(context, R.color.black))
    views.setTextColor(R.id.maghrib_text, ContextCompat.getColor(context, R.color.black))
    views.setTextColor(R.id.isha, ContextCompat.getColor(context, R.color.black))
    views.setTextColor(R.id.isha_text, ContextCompat.getColor(context, R.color.black))
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


private fun setOriginalTime(prayerTimes: PrayerTimes, views: RemoteViews){

    views.setTextViewText(R.id.fajr, prayerTimes.fajr)
    views.setTextViewText(R.id.dhuhr, prayerTimes.dhuhr)
    views.setTextViewText(R.id.asr, prayerTimes.asr)
    views.setTextViewText(R.id.maghrib, prayerTimes.maghrib)
    views.setTextViewText(R.id.isha, prayerTimes.isha)
}

private fun convertTimeFormat(prayerTimes: PrayerTimes, views: RemoteViews){
    views.setTextViewText(R.id.fajr, timeFormatConverter(prayerTimes.fajr))
    views.setTextViewText(R.id.dhuhr, timeFormatConverter(prayerTimes.dhuhr))
    views.setTextViewText(R.id.asr, timeFormatConverter(prayerTimes.asr))
    views.setTextViewText(R.id.maghrib, timeFormatConverter(prayerTimes.maghrib))
    views.setTextViewText(R.id.isha, timeFormatConverter(prayerTimes.isha))
}

private fun timeFormatConverter(time:String) : String{
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val date = dateFormat.parse(time)

    val newFormat = SimpleDateFormat("hh:mm aaa", Locale.getDefault())
    return newFormat.format(date!!)
}