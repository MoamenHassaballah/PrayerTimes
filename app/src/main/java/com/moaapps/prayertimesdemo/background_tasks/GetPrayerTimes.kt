package com.moaapps.prayertimesdemo.background_tasks

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.moaapps.prayertimesdemo.R
import com.moaapps.prayertimesdemo.modules.PrayerTimes
import com.moaapps.prayertimesdemo.utils.Constants
import com.moaapps.prayertimesdemo.utils.TinyDB
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.util.concurrent.CancellationException
import kotlin.coroutines.resumeWithException

class GetPrayerTimes(val context: Context) {

    companion object{private const val TAG = "GetPrayerTimes"}

    private val tinyDb: TinyDB = TinyDB(context)
    private var errorMessage = ""

    fun getError() : String {
        return errorMessage
    }

    suspend fun getPrayerTimes(lat: Double, long: Double): PrayerTimes? {
        errorMessage = ""
        Log.d(TAG, "getPrayerTimes: ")
        val url = "http://api.aladhan.com/v1/timings?latitude=$lat&longitude=$long"

        val response = AndroidNetworking.get(url)
            .build()
            .executeForJSONObject()

        if (response.isSuccess){
            Log.d(TAG, "getPrayerTimes: ${response.result}")
            val timings = (response.result as JSONObject).getJSONObject("data").getJSONObject("timings")
            val prayer = PrayerTimes(timings.getString("Fajr"),
            timings.getString("Dhuhr"), timings.getString("Asr"),
            timings.getString("Maghrib"), timings.getString("Isha"))
            tinyDb.putString(Constants.TIMINGS, Gson().toJson(prayer))

            return prayer
        }else{
            val anError = response.error
            Log.d(TAG, "getPrayerTimes: $anError")
            if (anError != null && !anError.message.isNullOrEmpty()) {
                errorMessage = anError.message!!
            }else{
                errorMessage = context.getString(R.string.error_occured)
            }
            return null
        }
    }



}